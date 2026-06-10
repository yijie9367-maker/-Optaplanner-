package com.wj.aischedule.service;

import com.alibaba.excel.EasyExcel;
import com.wj.aischedule.dto.ImportResult;
import com.wj.aischedule.entity.Teacher;
import com.wj.aischedule.entity.excel.TeacherExcel;
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
public class TeacherExcelService {

    @Autowired
    private TeacherService teacherService;

    public ImportResult importFromExcel(MultipartFile file) throws IOException {
        ImportResult result = new ImportResult();
        List<TeacherExcel> rows = EasyExcel.read(file.getInputStream())
                .head(TeacherExcel.class).sheet().doReadSync();
        int rowNum = 2;
        for (TeacherExcel row : rows) {
            try {
                if (row.getName() == null || row.getName().isBlank()) {
                    result.addError(rowNum, "姓名不能为空");
                    rowNum++;
                    continue;
                }
                Teacher teacher = new Teacher();
                teacher.setName(row.getName());
                teacher.setTitle(row.getTitle());
                teacher.setDepartment(row.getDepartment());
                teacherService.save(teacher);
                result.addSuccess();
            } catch (Exception e) {
                result.addError(rowNum, e.getMessage());
            }
            rowNum++;
        }
        return result;
    }

    public void exportToExcel(HttpServletResponse response) throws IOException {
        setResponseHeader(response, "教师列表");
        List<Teacher> teachers = teacherService.findAll();
        List<TeacherExcel> list = new ArrayList<>();
        for (Teacher t : teachers) {
            TeacherExcel row = new TeacherExcel();
            row.setName(t.getName());
            row.setTitle(t.getTitle());
            row.setDepartment(t.getDepartment());
            list.add(row);
        }
        EasyExcel.write(response.getOutputStream(), TeacherExcel.class).sheet("教师列表").doWrite(list);
    }

    public void downloadTemplate(HttpServletResponse response) throws IOException {
        setResponseHeader(response, "教师导入模板");
        EasyExcel.write(response.getOutputStream(), TeacherExcel.class)
                .sheet("教师导入模板").doWrite(new ArrayList<>());
    }

    private void setResponseHeader(HttpServletResponse response, String filename) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("UTF-8");
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + encoded + ".xlsx");
    }
}
