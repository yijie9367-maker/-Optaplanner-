package com.wj.aischedule.entity.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

@Data
@ColumnWidth(20)
public class ExamExcel {

    @ExcelProperty("课程编号")
    private String courseCode;

    @ExcelProperty("班级名称")
    private String classGroupName;

    @ExcelProperty("教师姓名")
    private String teacherName;

    @ExcelProperty("教师院系")
    private String teacherDepartment;

    @ExcelProperty("考试日期(yyyy-MM-dd)")
    private String examDate;

    @ExcelProperty("开始时间(HH:mm)")
    private String startTime;

    @ExcelProperty("结束时间(HH:mm)")
    private String endTime;

    @ExcelProperty("考试地点")
    private String location;
}
