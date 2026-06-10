package com.wj.aischedule.optaplanner.solver;

import com.wj.aischedule.optaplanner.domain.Lesson;
import com.wj.aischedule.optaplanner.domain.Room;
import com.wj.aischedule.optaplanner.domain.TimeTable;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;
import org.optaplanner.core.api.score.stream.Joiners;
import java.time.Duration;

/**
 * 约束规则提供器（支持动态权重）
 */
public class TimeTableConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
            // 硬约束（不可关闭）
                        fullyAssigned(constraintFactory),
            roomCapacity(constraintFactory),
            teacherConflict(constraintFactory),
            roomConflict(constraintFactory),
            classGroupConflict(constraintFactory),
            // 软约束（动态权重）
            spreadCourseInstances(constraintFactory),
            preferWeekRange(constraintFactory),
            avoidEveningClasses(constraintFactory),
            teacherTimePreference(constraintFactory),
            roomTypePreference(constraintFactory),
            timeSlotPreference(constraintFactory),
            continuousScheduling(constraintFactory)
        };
    }

    // ========== 硬约束 ==========

        private Constraint fullyAssigned(ConstraintFactory cf) {
                return cf.forEach(Lesson.class)
                                .filter(l -> l.getTimeslot() == null || l.getRoom() == null)
                                .penalize(HardSoftScore.ONE_HARD)
                                .asConstraint("课程必须完成排课");
        }

    private Constraint roomCapacity(ConstraintFactory cf) {
        return cf.forEach(Lesson.class)
                .filter(l -> l.getRoom() != null && l.getStudentCount() > l.getRoom().getCapacity())
                .penalize(HardSoftScore.ONE_HARD,
                        l -> l.getStudentCount() - l.getRoom().getCapacity())
                .asConstraint("教室容量不足");
    }

    private Constraint teacherConflict(ConstraintFactory cf) {
        return cf.forEachUniquePair(Lesson.class,
                        Joiners.equal(Lesson::getTeacherId),
                        Joiners.equal(Lesson::getTimeslot))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("教师时间冲突");
    }

    private Constraint roomConflict(ConstraintFactory cf) {
        return cf.forEachUniquePair(Lesson.class,
                        Joiners.equal(Lesson::getRoom),
                        Joiners.equal(Lesson::getTimeslot))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("教室时间冲突");
    }

    private Constraint classGroupConflict(ConstraintFactory cf) {
        return cf.forEachUniquePair(Lesson.class,
                        Joiners.equal(Lesson::getClassGroupId),
                        Joiners.equal(Lesson::getTimeslot))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("班级时间冲突");
    }

    // ========== 软约束（动态权重）==========

    private Constraint spreadCourseInstances(ConstraintFactory cf) {
        // 同一班级同一门课，只要在同一周出现两次就惩罚（无论是否同天）
        // 权重设为100，强力引导 OptaPlanner 把每门课分散到不同周
        return cf.forEachUniquePair(Lesson.class,
                        Joiners.equal(Lesson::getCourseCode),
                        Joiners.equal(Lesson::getClassGroupId))
                .filter((l1, l2) -> l1.getTimeslot() != null && l2.getTimeslot() != null
                        && l1.getTimeslot().getWeekNumber().equals(l2.getTimeslot().getWeekNumber()))
                .penalize(HardSoftScore.ONE_SOFT,
                        (l1, l2) -> l1.getConstraintWeight("spreadCourseInstances", 100))
                .asConstraint("分散同一课程的多个实例");
    }

    private Constraint preferWeekRange(ConstraintFactory cf) {
        return cf.forEach(Lesson.class)
                .filter(l -> l.getTimeslot() != null && l.getMaxWeek() != null
                        && l.getTimeslot().getWeekNumber() > l.getMaxWeek())
                .penalize(HardSoftScore.ONE_SOFT,
                        l -> l.getConstraintWeight("preferWeekRange", 3))
                .asConstraint("周次范围优惠");
    }

    private Constraint avoidEveningClasses(ConstraintFactory cf) {
        return cf.forEach(Lesson.class)
                .filter(l -> l.getTimeslot() != null
                        && l.getTimeslot().getStartTime().getHour() >= 19
                        && l.getConstraintWeight("avoidEveningClasses", 2) > 0)
                .penalize(HardSoftScore.ONE_SOFT,
                        l -> l.getConstraintWeight("avoidEveningClasses", 2))
                .asConstraint("避免晚上排课");
    }

    private Constraint teacherTimePreference(ConstraintFactory cf) {
        return cf.forEach(Lesson.class)
                .filter(l -> l.getTimeslot() != null
                        && l.getTimeslot().getStartTime().getHour() >= 19
                        && l.getConstraintWeight("teacherTimePreference", 1) > 0)
                .penalize(HardSoftScore.ONE_SOFT,
                        l -> l.getConstraintWeight("teacherTimePreference", 1))
                .asConstraint("教师时间偏好");
    }

    private Constraint roomTypePreference(ConstraintFactory cf) {
        return cf.forEach(Lesson.class)
                .filter(l -> l.getRoom() != null
                        && l.getRequiredRoomType() != null
                        && !l.getRequiredRoomType().equals(l.getRoom().getRoomType())
                        && l.getConstraintWeight("roomTypePreference", 1) > 0)
                .penalize(HardSoftScore.ONE_SOFT,
                        l -> l.getConstraintWeight("roomTypePreference", 1))
                .asConstraint("教室类型不匹配");
    }

    private Constraint timeSlotPreference(ConstraintFactory cf) {
        return cf.forEach(Lesson.class)
                .filter(l -> l.getTimeslot() != null
                        && l.getConstraintWeight("timeSlotPreference", 1) > 0
                        && (l.getCourseName().toLowerCase().contains("实验")
                                || l.getCourseName().toLowerCase().contains("实训"))
                        && l.getTimeslot().getStartTime().getHour() < 14)
                .penalize(HardSoftScore.ONE_SOFT,
                        l -> l.getConstraintWeight("timeSlotPreference", 1))
                .asConstraint("时间槽偏好");
    }

    private Constraint continuousScheduling(ConstraintFactory cf) {
        return cf.forEachUniquePair(Lesson.class,
                        Joiners.equal(Lesson::getCourseCode),
                        Joiners.equal(l -> l.getTimeslot() != null ? l.getTimeslot().getDayOfWeek() : null))
                .filter((l1, l2) -> l1.getTimeslot() != null && l2.getTimeslot() != null
                        && Math.abs(l1.getTimeslot().getTimeSlotIndex() - l2.getTimeslot().getTimeSlotIndex()) > 1
                        && l1.getConstraintWeight("continuousScheduling", 1) > 0)
                .penalize(HardSoftScore.ONE_SOFT,
                        (l1, l2) -> {
                            int gap = Math.abs(l1.getTimeslot().getTimeSlotIndex() - l2.getTimeslot().getTimeSlotIndex()) - 1;
                            return gap * l1.getConstraintWeight("continuousScheduling", 1);
                        })
                .asConstraint("课程不连续");
    }
}

