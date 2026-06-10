package com.wj.aischedule.service;

import com.alibaba.excel.EasyExcel;
import com.wj.aischedule.dto.ImportResult;
import com.wj.aischedule.entity.ClassGroup;
import com.wj.aischedule.entity.excel.ClassGroupExcel;
import com.wj.aischedule.repository.ClassGroupRepository;
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
public class ClassGroupExcelService {

    @Autowired
    private ClassGroupRepository classGroupRepository;

    public ImportResult importFromExcel(MultipartFile file) throws IOException {
        ImportResult result = new ImportResult();
        List<ClassGroupExcel> rows = EasyExcel.read(file.getInputStream())
                .head(ClassGroupExcel.class).sheet().doReadSync();
        int rowNum = 2;
        for (ClassGroupExcel row : rows) {
            try {
                if (row.getName() == null || row.getName().isBlank()) {
                    result.addError(rowNum, "班级名称不能为空");
                    rowNum++;
                    continue;
                }
                ClassGroup cg = new ClassGroup();
                cg.setName(row.getName());
                cg.setGrade(row.getGrade());
                cg.setMajor(row.getMajor());
                classGroupRepository.save(cg);
                result.addSuccess();
            } catch (Exception e) {
                result.addError(rowNum, e.getMessage());
            }
            rowNum++;
        }
        return result;
    }

    public void exportToExcel(HttpServletResponse response) throws IOException {
        setResponseHeader(response, "班级列表");
        List<ClassGroup> list = classGroupRepository.findAll();
        List<ClassGroupExcel> rows = new ArrayList<>();
        for (ClassGroup cg : list) {
            ClassGroupExcel row = new ClassGroupExcel();
            row.setName(cg.getName());
            row.setGrade(cg.getGrade());
            row.setMajor(cg.getMajor());
            rows.add(row);
        }
        EasyExcel.write(response.getOutputStream(), ClassGroupExcel.class).sheet("班级列表").doWrite(rows);
    }

    public void downloadTemplate(HttpServletResponse response) throws IOException {
        setResponseHeader(response, "班级导入模板");
        EasyExcel.write(response.getOutputStream(), ClassGroupExcel.class)
                .sheet("班级导入模板").doWrite(new ArrayList<>());
    }

    private void setResponseHeader(HttpServletResponse response, String filename) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("UTF-8");
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + encoded + ".xlsx");
    }
}
