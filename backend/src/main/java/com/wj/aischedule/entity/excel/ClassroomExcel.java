package com.wj.aischedule.entity.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

@Data
@ColumnWidth(20)
public class ClassroomExcel {

    @ExcelProperty("教室名称")
    private String name;

    @ExcelProperty("楼栋")
    private String building;

    @ExcelProperty("容量")
    private String capacity;

    @ExcelProperty("类型")
    private String type;
}
