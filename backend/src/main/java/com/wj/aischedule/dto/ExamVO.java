package com.wj.aischedule.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 期末考试视图对象 - 携带课程名、班级名、教师名等关联信息
 */
public class ExamVO {

    private Long id;
    private Long courseId;
    private String courseName;
    private Long classGroupId;
    private String className;
    private Long teacherId;
    private String teacherName;
    private LocalDate examDate;
    private String startTime;
    private String endTime;
    private String location;

    // 学生查询时附带的自己的成绩（可为 null）
    private BigDecimal myScore;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public Long getClassGroupId() { return classGroupId; }
    public void setClassGroupId(Long classGroupId) { this.classGroupId = classGroupId; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }

    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

    public LocalDate getExamDate() { return examDate; }
    public void setExamDate(LocalDate examDate) { this.examDate = examDate; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public BigDecimal getMyScore() { return myScore; }
    public void setMyScore(BigDecimal myScore) { this.myScore = myScore; }
}
