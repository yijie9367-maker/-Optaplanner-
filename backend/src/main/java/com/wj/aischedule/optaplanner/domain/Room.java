package com.wj.aischedule.optaplanner.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.optaplanner.core.api.domain.lookup.PlanningId;

/**
 * 教室 - 问题事实类
 * 这是 Optaplanner 内存计算对象，不再是数据库实体
 */
// 2. 【核心】删掉 @Entity
public class Room {

    @PlanningId
    // 3. 【核心】删掉 @Id 和 @GeneratedValue
    private Long id;

    private String name;          // 教室名称，如 "A101"
    private String building;      // 教学楼
    private Integer capacity;     // 容量
    private String roomType;      // 教室类型
    private Boolean hasProjector; // 是否有投影仪
    private Boolean hasComputer;  // 是否有电脑

    public Room() {
    }

    // ... 后面的构造函数、Getter、Setter 和业务方法全部保持不变 ...

    public Room(String name, String building, Integer capacity, String roomType) {
        this.name = name;
        this.building = building;
        this.capacity = capacity;
        this.roomType = roomType;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getBuilding() { return building; }
    public void setBuilding(String building) { this.building = building; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }
    public Boolean getHasProjector() { return hasProjector; }
    public void setHasProjector(Boolean hasProjector) { this.hasProjector = hasProjector; }
    public Boolean getHasComputer() { return hasComputer; }
    public void setHasComputer(Boolean hasComputer) { this.hasComputer = hasComputer; }

    @JsonIgnore
    public String getFullName() {
        return building + " " + name;
    }

    @JsonIgnore
    public boolean isSuitableForCourse(int studentCount, boolean needProjector, boolean needComputer) {
        if (capacity < studentCount) return false;
        if (needProjector && !Boolean.TRUE.equals(hasProjector)) return false;
        if (needComputer && !Boolean.TRUE.equals(hasComputer)) return false;
        return true;
    }

    @Override
    public String toString() {
        return getFullName() + " (容量:" + capacity + ")";
    }

}