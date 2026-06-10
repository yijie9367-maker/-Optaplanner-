package com.wj.aischedule.optaplanner.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.optaplanner.core.api.domain.lookup.PlanningId;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * 时间槽 - 问题事实类
 * 表示一个可用的上课时间段
 */
public class Timeslot {
    
    @PlanningId
    private Long id;
    
    private String semester;      // 学期
    private Integer weekNumber;   // 周次
    private DayOfWeek dayOfWeek;  // 星期几
    private LocalTime startTime;  // 开始时间
    private LocalTime endTime;    // 结束时间
    
    // 用于前端显示的字符串
    private String displayName;
    
    public Timeslot() {
    }
    
    public Timeslot(String semester, Integer weekNumber, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
        this.semester = semester;
        this.weekNumber = weekNumber;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.displayName = generateDisplayName();
    }
    
    private String generateDisplayName() {
        String[] weekDays = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
        int dayIndex = dayOfWeek.getValue() - 1;
        return String.format("%s 第%d周 %s %s-%s", 
            semester, weekNumber, weekDays[dayIndex], 
            startTime.toString(), endTime.toString());
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getSemester() {
        return semester;
    }
    
    public void setSemester(String semester) {
        this.semester = semester;
    }
    
    public Integer getWeekNumber() {
        return weekNumber;
    }
    
    public void setWeekNumber(Integer weekNumber) {
        this.weekNumber = weekNumber;
    }
    
    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }
    
    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }
    
    public LocalTime getStartTime() {
        return startTime;
    }
    
    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }
    
    public LocalTime getEndTime() {
        return endTime;
    }
    
    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    @JsonIgnore
    public int getDayOfWeekIndex() {
        return dayOfWeek.getValue(); // 1=Monday, 7=Sunday
    }
    
    @JsonIgnore
    public int getTimeSlotIndex() {
        // 根据开始时间计算节次
        if (startTime.equals(LocalTime.of(8, 0))) return 1;  // 1-2节
        if (startTime.equals(LocalTime.of(10, 0))) return 2; // 3-4节
        if (startTime.equals(LocalTime.of(14, 0))) return 3; // 5-6节
        if (startTime.equals(LocalTime.of(16, 0))) return 4; // 7-8节
        if (startTime.equals(LocalTime.of(19, 0))) return 5; // 9-10节
        return 6; // 其他
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}