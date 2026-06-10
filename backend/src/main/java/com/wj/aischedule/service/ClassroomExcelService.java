package com.wj.aischedule.service;

import com.alibaba.excel.EasyExcel;
import com.wj.aischedule.dto.ImportResult;
import com.wj.aischedule.entity.Classroom;
import com.wj.aischedule.entity.excel.ClassroomExcel;
import com.wj.aischedule.repository.ClassroomRepository;
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
public class ClassroomExcelService {

    @Autowired
    private ClassroomRepository classroomRepository;

    public ImportResult importFromExcel(MultipartFile file) throws IOException {
        ImportResult result = new ImportResult();
        List<ClassroomExcel> rows = EasyExcel.read(file.getInputStream())
                .head(ClassroomExcel.class).sheet().doReadSync();
        int rowNum = 2;
        for (ClassroomExcel row : rows) {
            try {
                if (row.getName() == null || row.getName().isBlank()) {
                    result.addError(rowNum, "教室名称不能为空");
                    rowNum++;
                    continue;
                }
                Classroom classroom = new Classroom();
                classroom.setName(row.getName());
                classroom.setBuilding(row.getBuilding());
                if (row.getCapacity() != null && !row.getCapacity().isBlank()) {
                    try { classroom.setCapacity(Integer.parseInt(row.getCapacity().trim())); }
                    catch (NumberFormatException e) { result.addError(rowNum, "容量必须是整数"); rowNum++; continue; }
                }
                classroom.setType(row.getType());
                classroomRepository.save(classroom);
                result.addSuccess();
            } catch (Exception e) {
                result.addError(rowNum, e.getMessage());
            }
            rowNum++;
        }
        return result;
    }

    public void exportToExcel(HttpServletResponse response) throws IOException {
        setResponseHeader(response, "教室列表");
        List<Classroom> classrooms = classroomRepository.findAll();
        List<ClassroomExcel> list = new ArrayList<>();
        for (Classroom c : classrooms) {
            ClassroomExcel row = new ClassroomExcel();
            row.setName(c.getName());
            row.setBuilding(c.getBuilding());
            row.setCapacity(c.getCapacity() != null ? String.valueOf(c.getCapacity()) : "");
            row.setType(c.getType());
            list.add(row);
        }
        EasyExcel.write(response.getOutputStream(), ClassroomExcel.class).sheet("教室列表").doWrite(list);
    }

    public void downloadTemplate(HttpServletResponse response) throws IOException {
        setResponseHeader(response, "教室导入模板");
        EasyExcel.write(response.getOutputStream(), ClassroomExcel.class)
                .sheet("教室导入模板").doWrite(new ArrayList<>());
    }

    private void setResponseHeader(HttpServletResponse response, String filename) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("UTF-8");
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + encoded + ".xlsx");
    }
}
