package com.wj.aischedule.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "schedule")
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 课程信息
    @Column(name = "course_id", nullable = false)
    private Long courseId;
    
    // 班级信息
    @Column(name = "class_group_id", nullable = false)
    private Long classGroupId;
    
    // 教室信息
    @Column(name = "classroom_id", nullable = false)
    private Long classroomId;
    
    // 教师信息（通过课程关联）
    @Column(name = "teacher_id")
    private Long teacherId;
    
    // 时间安排
    @Column(name = "semester")  // 学期，如：2023-2024-1
    private String semester;
    
    @Column(name = "week_number")  // 周次 1-20
    private Integer weekNumber;
    
    @Column(name = "week_day", nullable = false)  // 星期几 1=周一 ... 7=周日
    private Integer weekDay;
    
    @Column(name = "time_slot", nullable = false)  // 节次 1=第1-2节, 2=第3-4节...
    private Integer timeSlot;
    
    @Column(name = "duration")  // 持续节数，默认2节
    private Integer duration = 2;
    
    // 状态标志
    @Column(name = "is_conflict")
    private Boolean isConflict = false;
    
    @Column(name = "conflict_reason")
    private String conflictReason;
    
    // 优化评分（Optaplanner使用）
    @Column(name = "score")
    private Integer score;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ===== getter / setter =====
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public Long getClassGroupId() { return classGroupId; }
    public void setClassGroupId(Long classGroupId) { this.classGroupId = classGroupId; }

    public Long getClassroomId() { return classroomId; }
    public void setClassroomId(Long classroomId) { this.classroomId = classroomId; }
    
    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
    
    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }
    
    public Integer getWeekNumber() { return weekNumber; }
    public void setWeekNumber(Integer weekNumber) { this.weekNumber = weekNumber; }

    public Integer getWeekDay() { return weekDay; }
    public void setWeekDay(Integer weekDay) { this.weekDay = weekDay; }

    public Integer getTimeSlot() { return timeSlot; }
    public void setTimeSlot(Integer timeSlot) { this.timeSlot = timeSlot; }
    
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
    
    public Boolean getIsConflict() { return isConflict; }
    public void setIsConflict(Boolean isConflict) { this.isConflict = isConflict; }
    
    public String getConflictReason() { return conflictReason; }
    public void setConflictReason(String conflictReason) { this.conflictReason = conflictReason; }
    
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    
    // 辅助方法
    public String getTimeDescription() {
        String[] weekDays = {"", "周一", "周二", "周三", "周四", "周五", "周六", "周日"};
        String[] timeSlots = {"", "1-2节", "3-4节", "5-6节", "7-8节", "9-10节", "11-12节"};
        
        String weekDayStr = weekDay >= 1 && weekDay <= 7 ? weekDays[weekDay] : "未知";
        String timeSlotStr = timeSlot >= 1 && timeSlot <= 6 ? timeSlots[timeSlot] : "未知";
        
        return String.format("%s 第%d周 %s %s", semester, weekNumber, weekDayStr, timeSlotStr);
    }
}
