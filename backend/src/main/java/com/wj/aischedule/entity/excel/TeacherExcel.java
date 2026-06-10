package com.wj.aischedule.entity.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

@Data
@ColumnWidth(20)
public class TeacherExcel {

    @ExcelProperty("姓名")
    private String name;

    @ExcelProperty("职称")
    private String title;

    @ExcelProperty("院系")
    private String department;
}
