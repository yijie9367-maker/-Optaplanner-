package com.wj.aischedule.entity.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

@Data
@ColumnWidth(20)
public class ExamGradeExcel {

    @ExcelProperty("课程编号")
    private String courseCode;

    @ExcelProperty("班级名称")
    private String classGroupName;

    @ExcelProperty("学生姓名")
    private String studentName;

    @ExcelProperty("成绩")
    private String score;
}
