package com.wj.aischedule.dto;

import java.time.LocalDateTime;

/**
 * Schedule View Object - 用于前端展示的排课数据
 */
public class ScheduleVO {
    
    private Long id;
    
    // 课程信息
    private Long courseId;
    private String courseCode;
    private String courseName;
    
    // 班级信息
    private Long classGroupId;
    private String className;
    private Integer studentNum;  // 班级人数
    
    // 教室信息
    private Long classroomId;
    private String building;
    private String roomName;
    private Integer capacity;
    
    // 教师信息
    private Long teacherId;
    private String teacherName;
    private String title;  // 职称
    
    // 时间安排
    private String semester;
    private Integer weekNumber;
    private Integer weekDay;
    private Integer timeSlot;
    private Integer duration;
    
    // 状态
    private Boolean isConflict;
    private String conflictReason;
    private Integer score;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    
    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
    
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    
    public Long getClassGroupId() { return classGroupId; }
    public void setClassGroupId(Long classGroupId) { this.classGroupId = classGroupId; }
    
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    
    public Integer getStudentNum() { return studentNum; }
    public void setStudentNum(Integer studentNum) { this.studentNum = studentNum; }
    
    public Long getClassroomId() { return classroomId; }
    public void setClassroomId(Long classroomId) { this.classroomId = classroomId; }
    
    public String getBuilding() { return building; }
    public void setBuilding(String building) { this.building = building; }
    
    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }
    
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    
    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
    
    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
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
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
