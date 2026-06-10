package com.wj.aischedule.entity.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

@Data
@ColumnWidth(20)
public class ScheduleExcel {

    @ExcelProperty("课程编号")
    private String courseCode;

    @ExcelProperty("课程名称")
    private String courseName;

    @ExcelProperty("班级名称")
    private String classGroupName;

    @ExcelProperty("教室名称")
    private String classroomName;

    @ExcelProperty("教师姓名")
    private String teacherName;

    @ExcelProperty("教师院系")
    private String teacherDepartment;

    @ExcelProperty("学期")
    private String semester;

    @ExcelProperty("第几周")
    private String weekNumber;

    @ExcelProperty("星期(1-7)")
    private String weekDay;

    @ExcelProperty("节次")
    private String timeSlot;

    @ExcelProperty("时长")
    private String duration;
}
