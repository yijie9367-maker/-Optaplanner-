package com.wj.aischedule.optaplanner.domain;

import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.solution.ProblemFactProperty;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 排课问题 - 规划解决方案类
 * 这是 Optaplanner 内存计算的容器，不对应数据库表
 */
@PlanningSolution
// 1. 【必须删除】 @Entity 注解
public class TimeTable {

    // 2. 【必须删除】 @Id 和 @GeneratedValue 注解
    private Long id;

    private String problemId;
    private String semester;
    private String description;

    // 问题事实集合
    @ProblemFactCollectionProperty
    @ValueRangeProvider(id = "timeslotRange")
    // 3. 【必须删除】 @OneToMany 等所有 JPA 关联注解
    private List<Timeslot> timeslotList;

    // 问题事实集合
    @ProblemFactCollectionProperty
    @ValueRangeProvider(id = "roomRange")
    // 3. 【必须删除】 @OneToMany 等所有 JPA 关联注解
    private List<Room> roomList;

    // 规划实体集合
    @PlanningEntityCollectionProperty
    // 3. 【必须删除】 @OneToMany 等所有 JPA 关联注解
    private List<Lesson> lessonList;

    @PlanningScore
    private HardSoftScore score;

    /** 软约束权重表，key = constraintKey, value = weight（0表示禁用）*/
    @ProblemFactProperty
    private Map<String, Integer> constraintWeights = new HashMap<>();

    private Boolean solved = false;
    private Long solvingTime;
    private Integer iterationCount;

    public TimeTable() {
        this.timeslotList = new ArrayList<>();
        this.roomList = new ArrayList<>();
        this.lessonList = new ArrayList<>();
    }

    public TimeTable(String problemId, String semester, String description) {
        this();
        this.problemId = problemId;
        this.semester = semester;
        this.description = description;
    }

    // --- Getters and Setters (保持不变，但确保删除了字段上的 JPA 注解) ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getProblemId() { return problemId; }
    public void setProblemId(String problemId) { this.problemId = problemId; }
    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<Timeslot> getTimeslotList() { return timeslotList; }
    public void setTimeslotList(List<Timeslot> timeslotList) { this.timeslotList = timeslotList; }
    public List<Room> getRoomList() { return roomList; }
    public void setRoomList(List<Room> roomList) { this.roomList = roomList; }
    public List<Lesson> getLessonList() { return lessonList; }
    public void setLessonList(List<Lesson> lessonList) { this.lessonList = lessonList; }
    public HardSoftScore getScore() { return score; }
    public void setScore(HardSoftScore score) { this.score = score; }
    public Map<String, Integer> getConstraintWeights() { return constraintWeights; }
    public void setConstraintWeights(Map<String, Integer> constraintWeights) { this.constraintWeights = constraintWeights; }
    public int getConstraintWeight(String key, int defaultVal) {
        return constraintWeights.getOrDefault(key, defaultVal);
    }
    public Boolean getSolved() { return solved; }
    public void setSolved(Boolean solved) { this.solved = solved; }
    public Long getSolvingTime() { return solvingTime; }
    public void setSolvingTime(Long solvingTime) { this.solvingTime = solvingTime; }
    public Integer getIterationCount() { return iterationCount; }
    public void setIterationCount(Integer iterationCount) { this.iterationCount = iterationCount; }

    // --- 辅助方法 ---

    public void addTimeslot(Timeslot timeslot) { this.timeslotList.add(timeslot); }
    public void addRoom(Room room) { this.roomList.add(room); }
    public void addLesson(Lesson lesson) { this.lessonList.add(lesson); }

    public int getUnscheduledLessonCount() {
        return (int) lessonList.stream().filter(l -> !l.isScheduled()).count();
    }

    public int getConflictCount() {
        return (int) lessonList.stream().filter(l -> Boolean.TRUE.equals(l.getHasConflict())).count();
    }

    public List<Lesson> getScheduledLessons() {
        return lessonList.stream().filter(Lesson::isScheduled).collect(Collectors.toList());
    }

    public List<Lesson> getUnscheduledLessons() {
        return lessonList.stream().filter(l -> !l.isScheduled()).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        // 只有 3 个占位符，对应 3 个参数
        return String.format("TimeTable[%s]: %d个课程, 分数=%s",
                semester,
                lessonList == null ? 0 : lessonList.size(),
                score);
    }
    public Object getConflictedLessons() {
        return null;
    }
}