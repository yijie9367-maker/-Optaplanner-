package com.wj.aischedule.dto;

import java.math.BigDecimal;

/**
 * 成绩录入条目 - 前端批量提交成绩时使用
 */
public class GradeItem {

    private Long studentId;
    private String studentName;
    private BigDecimal score;

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public BigDecimal getScore() { return score; }
    public void setScore(BigDecimal score) { this.score = score; }
}
