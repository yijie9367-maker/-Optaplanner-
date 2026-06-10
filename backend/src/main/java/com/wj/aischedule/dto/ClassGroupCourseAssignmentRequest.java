package com.wj.aischedule.dto;

import java.util.List;

public class ClassGroupCourseAssignmentRequest {

    private String semester;
    private List<Long> courseIds;

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public List<Long> getCourseIds() {
        return courseIds;
    }

    public void setCourseIds(List<Long> courseIds) {
        this.courseIds = courseIds;
    }
}