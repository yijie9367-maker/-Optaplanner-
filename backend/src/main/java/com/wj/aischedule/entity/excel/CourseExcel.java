package com.wj.aischedule.entity.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

@Data
@ColumnWidth(20)
public class CourseExcel {

    @ExcelProperty("课程编号")
    private String code;

    @ExcelProperty("课程名称")
    private String name;

    @ExcelProperty("教师姓名")
    private String teacherName;

    @ExcelProperty("教师院系")
    private String teacherDepartment;

    @ExcelProperty("学分")
    private String credit;
}
