package com.wj.aischedule.dto;

public class CourseDTO {
    private Long id;
    private String name;
    private String code;
    private Double credit;
    private Long teacherId;
    private String teacherName;
    private String courseType;

    public CourseDTO() {
    }

    public CourseDTO(Long id, String name, String code, Double credit, Long teacherId, String teacherName, String courseType) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.credit = credit;
        this.teacherId = teacherId;
        this.teacherName = teacherName;
        this.courseType = courseType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Double getCredit() {
        return credit;
    }

    public void setCredit(Double credit) {
        this.credit = credit;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public String getCourseType() {
        return courseType;
    }

    public void setCourseType(String courseType) {
        this.courseType = courseType;
    }
}
