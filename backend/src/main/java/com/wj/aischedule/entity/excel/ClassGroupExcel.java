package com.wj.aischedule.entity.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

@Data
@ColumnWidth(20)
public class ClassGroupExcel {

    @ExcelProperty("班级名称")
    private String name;

    @ExcelProperty("年级")
    private String grade;

    @ExcelProperty("专业")
    private String major;
}
