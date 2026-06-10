package com.wj.aischedule.entity.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

@Data
@ColumnWidth(20)
public class AdminUserExcel {

    @ExcelProperty("用户名")
    private String username;

    @ExcelProperty("密码")
    private String password;
}
