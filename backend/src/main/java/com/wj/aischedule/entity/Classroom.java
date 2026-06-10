package com.wj.aischedule.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "classroom")
public class Classroom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 教室名称，如 A101
    @Column(nullable = false, length = 50)
    private String name;

    // 所在教学楼
    @Column(length = 50)
    private String building;

    // 容量
    private Integer capacity;

    // 教室类型（普通 / 实验室 / 多媒体）
    @Column(length = 50)
    private String type;

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

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getBuilding() {
        return building;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public String getType() {
        return type;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public void setType(String type) {
        this.type = type;
    }
}
