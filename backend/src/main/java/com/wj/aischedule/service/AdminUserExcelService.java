package com.wj.aischedule.service;

import com.alibaba.excel.EasyExcel;
import com.wj.aischedule.dto.ImportResult;
import com.wj.aischedule.entity.AdminUser;
import com.wj.aischedule.entity.excel.AdminUserExcel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdminUserExcelService {

    @Autowired
    private AdminUserService adminUserService;

    public ImportResult importFromExcel(MultipartFile file) throws IOException {
        ImportResult result = new ImportResult();
        List<AdminUserExcel> rows = EasyExcel.read(file.getInputStream())
                .head(AdminUserExcel.class).sheet().doReadSync();
        int rowNum = 2;
        for (AdminUserExcel row : rows) {
            try {
                if (row.getUsername() == null || row.getUsername().isBlank()) {
                    result.addError(rowNum, "用户名不能为空");
                    rowNum++;
                    continue;
                }
                if (row.getPassword() == null || row.getPassword().isBlank()) {
                    result.addError(rowNum, "密码不能为空");
                    rowNum++;
                    continue;
                }
                AdminUser admin = new AdminUser();
                admin.setUsername(row.getUsername());
                admin.setPassword(row.getPassword());
                adminUserService.save(admin);
                result.addSuccess();
            } catch (Exception e) {
                result.addError(rowNum, e.getMessage());
            }
            rowNum++;
        }
        return result;
    }

    public void exportToExcel(HttpServletResponse response) throws IOException {
        setResponseHeader(response, "管理员列表");
        List<AdminUser> admins = adminUserService.findAll();
        List<AdminUserExcel> list = new ArrayList<>();
        for (AdminUser a : admins) {
            AdminUserExcel row = new AdminUserExcel();
            row.setUsername(a.getUsername());
            row.setPassword(a.getPassword());
            list.add(row);
        }
        EasyExcel.write(response.getOutputStream(), AdminUserExcel.class).sheet("管理员列表").doWrite(list);
    }

    public void downloadTemplate(HttpServletResponse response) throws IOException {
        setResponseHeader(response, "管理员导入模板");
        EasyExcel.write(response.getOutputStream(), AdminUserExcel.class)
                .sheet("管理员导入模板").doWrite(new ArrayList<>());
    }

    private void setResponseHeader(HttpServletResponse response, String filename) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("UTF-8");
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + encoded + ".xlsx");
    }
}
