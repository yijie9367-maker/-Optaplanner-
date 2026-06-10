package com.wj.aischedule.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "course")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 课程名称
    @Column(nullable = false, length = 100)
    private String name;

    // 课程编号
    @Column(name = "code", length = 20, unique = true)  // 添加 unique，课程编号应该唯一
    private String code;

    // 学分
    @Column(name = "credit")
    private Double credit;

    // 任课教师（外键）
    @Column(name = "teacher_id")
    private Long teacherId;

    // 最大课时
    @Column(name = "max_hours")
    private Integer maxHours;

    // 一学期需要上课的次数
    @Column(name = "total_classes_needed")
    private Integer totalClassesNeeded;

    // 课程类型（专业课/非专业课）
    @Column(name = "course_type", length = 50)
    private String courseType;

    // 课程描述
    @Column(name = "description", length = 500)
    private String description;

    // 创建时间
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 更新时间
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ===== 标准的 getter/setter =====
    // 注意：所有方法都使用标准的 Java Bean 命名规范

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

    public Integer getMaxHours() {
        return maxHours;
    }

    public void setMaxHours(Integer maxHours) {
        this.maxHours = maxHours;
    }

    public Integer getTotalClassesNeeded() {
        return totalClassesNeeded;
    }

    public void setTotalClassesNeeded(Integer totalClassesNeeded) {
        this.totalClassesNeeded = totalClassesNeeded;
    }

    public String getCourseType() {
        return courseType;
    }

    public void setCourseType(String courseType) {
        this.courseType = courseType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // 注意：setCreatedAt 通常不需要对外开放，因为由 @PrePersist 自动管理
    // 如果需要手动设置，可以保留
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // ===== 可选：添加 toString 方法便于调试 =====
    @Override
    public String toString() {
        return "Course{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", credit=" + credit +
                ", teacherId=" + teacherId +
                ", maxHours=" + maxHours +
                '}';
    }
}