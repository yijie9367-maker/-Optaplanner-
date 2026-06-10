package com.wj.aischedule.entity;

import javax.persistence.*;

@Entity
@Table(name = "class_group")
public class ClassGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 班级名称，如 计科2301
    private String name;

    // 年级，如 2023
    private String grade;

    // 专业
    private String major;

    // 班级人数
    @Column(name = "student_count")
    private Integer studentCount;

    // ===== getter / setter =====

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getGrade() {
        return grade;
    }

    public String getMajor() {
        return major;
    }

    public Integer getStudentCount() {
        return studentCount;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public void setStudentCount(Integer studentCount) {
        this.studentCount = studentCount;
    }
}
