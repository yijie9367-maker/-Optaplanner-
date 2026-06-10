package com.wj.aischedule.entity.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

@Data
@ColumnWidth(20)
public class StudentExcel {

    @ExcelProperty("姓名")
    private String name;

    @ExcelProperty("年龄")
    private String age;

    @ExcelProperty("专业")
    private String major;

    @ExcelProperty("班级名称")
    private String classGroupName;
}
