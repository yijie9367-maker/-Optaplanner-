package com.wj.aischedule.optaplanner.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.lookup.PlanningId;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 课程安排 - 规划实体类
 * 这是 Optaplanner 内存计算的对象，不再是 JPA 数据库实体
 */
@PlanningEntity(difficultyWeightFactoryClass = LessonDifficultyWeightFactory.class)
@NoArgsConstructor // 无参构造函数
public class Lesson {

    // 自定义构造函数（8 个参数）
    public Lesson(String courseCode, String courseName, Integer credit,
                  Integer studentCount, Long teacherId, String teacherName,
                  Long classGroupId, String className) {
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.credit = credit;
        this.studentCount = studentCount;
        this.teacherId = teacherId;
        this.teacherName = teacherName;
        this.classGroupId = classGroupId;
        this.className = className;
        this.duration = 2; // 默认给个 2 节课的时长
    }
    
    // 完整构造函数（用于需要设置 timeslot 和 room 的场景）
    public Lesson(Long id, String courseCode, String courseName, Integer credit, Integer duration,
                  Integer studentCount, Long teacherId, String teacherName,
                  Long classGroupId, String className, Timeslot timeslot, Room room) {
        this.id = id;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.credit = credit;
        this.duration = duration;
        this.studentCount = studentCount;
        this.teacherId = teacherId;
        this.teacherName = teacherName;
        this.classGroupId = classGroupId;
        this.className = className;
        this.timeslot = timeslot;
        this.room = room;
    }

    @PlanningId
    // 2. 【删掉】 @Id 和 @GeneratedValue
    private Long id;

    // 课程基本信息
    private String courseCode;
    private String courseName;
    private Integer credit;
    private Integer studentCount;
    private Integer duration;

    private Long teacherId;
    private String teacherName;

    private Long classGroupId;
    private String className;

    private Boolean needProjector = false;
    private Boolean needComputer = false;
    private String requiredRoomType;
    
    // 周次分配提示（用于指导 OptaPlanner 排课）
    private Integer preferredStartWeek; // 建议开始周次
    private Integer preferredEndWeek;   // 建议结束周次
    private Integer maxWeek;            // 最晚要排到的周次

    // 3. 【重点】只保留 Optaplanner 注解，删掉 @ManyToOne
    @PlanningVariable(valueRangeProviderRefs = {"timeslotRange"})
    private Timeslot timeslot;

    @PlanningVariable(valueRangeProviderRefs = {"roomRange"})
    private Room room;

    private Boolean hasConflict = false;
    private String conflictReason;
    private List<Lesson> relatedLessons;

    /** 软约束权重表，由 SchedulingService 在排课前注入 */
    private Map<String, Integer> constraintWeights = new HashMap<>();

    public int getConstraintWeight(String key, int defaultVal) {
        return constraintWeights.getOrDefault(key, defaultVal);
    }
    public void setConstraintWeights(Map<String, Integer> constraintWeights) {
        this.constraintWeights = constraintWeights;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Lesson)) return false;
        Lesson lesson = (Lesson) o;
        // 只要 ID 相同，OptaPlanner 就认为它们是同一个规划实体
        return id != null && id.equals(lesson.id);
    }

    @Override
    public int hashCode() {
        // 使用 ID 的 hash
        return id != null ? id.hashCode() : 0;
    }

    // ... 其余构造函数和 Getter/Setter 保持不变 ...

    public void setId(Long id) {
        this.id = id;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public void setCredit(Integer credit) {
        this.credit = credit;
    }

    public void setStudentCount(Integer studentCount) {
        this.studentCount = studentCount;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public void setClassGroupId(Long classGroupId) {
        this.classGroupId = classGroupId;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setNeedProjector(Boolean needProjector) {
        this.needProjector = needProjector;
    }

    public void setNeedComputer(Boolean needComputer) {
        this.needComputer = needComputer;
    }

    public void setRequiredRoomType(String requiredRoomType) {
        this.requiredRoomType = requiredRoomType;
    }

    public void setPreferredStartWeek(Integer preferredStartWeek) {
        this.preferredStartWeek = preferredStartWeek;
    }

    public void setPreferredEndWeek(Integer preferredEndWeek) {
        this.preferredEndWeek = preferredEndWeek;
    }

    public void setMaxWeek(Integer maxWeek) {
        this.maxWeek = maxWeek;
    }

    public void setTimeslot(Timeslot timeslot) {
        this.timeslot = timeslot;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public void setHasConflict(Boolean hasConflict) {
        this.hasConflict = hasConflict;
    }

    public void setConflictReason(String conflictReason) {
        this.conflictReason = conflictReason;
    }

    public void setRelatedLessons(List<Lesson> relatedLessons) {
        this.relatedLessons = relatedLessons;
    }

    public Long getId() {
        return id;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public String getCourseName() {
        return courseName;
    }

    public Integer getCredit() {
        return credit;
    }

    public Integer getStudentCount() {
        return studentCount;
    }

    public Integer getDuration() {
        return duration;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public Long getClassGroupId() {
        return classGroupId;
    }

    public String getClassName() {
        return className;
    }

    public Boolean getNeedProjector() {
        return needProjector;
    }

    public Boolean getNeedComputer() {
        return needComputer;
    }

    public String getRequiredRoomType() {
        return requiredRoomType;
    }

    public Integer getPreferredStartWeek() {
        return preferredStartWeek;
    }

    public Integer getPreferredEndWeek() {
        return preferredEndWeek;
    }

    public Integer getMaxWeek() {
        return maxWeek;
    }

    public Timeslot getTimeslot() {
        return timeslot;
    }

    public Room getRoom() {
        return room;
    }

    public Boolean getHasConflict() {
        return hasConflict;
    }

    public String getConflictReason() {
        return conflictReason;
    }

    public List<Lesson> getRelatedLessons() {
        return relatedLessons;
    }

    public boolean isScheduled() {
        return timeslot != null && room != null;
    }
}