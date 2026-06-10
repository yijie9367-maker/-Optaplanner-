package com.wj.aischedule.service;

import com.alibaba.excel.EasyExcel;
import com.wj.aischedule.dto.ImportResult;
import com.wj.aischedule.entity.Student;
import com.wj.aischedule.entity.ClassGroup;
import com.wj.aischedule.entity.excel.StudentExcel;
import com.wj.aischedule.repository.ClassGroupRepository;
import com.wj.aischedule.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class StudentExcelService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private ClassGroupRepository classGroupRepository;

    @Autowired
    private StudentService studentService;

    public ImportResult importFromExcel(MultipartFile file) throws IOException {
        ImportResult result = new ImportResult();
        List<StudentExcel> rows = EasyExcel.read(file.getInputStream())
                .head(StudentExcel.class).sheet().doReadSync();
        int rowNum = 2;
        for (StudentExcel row : rows) {
            try {
                if (row.getName() == null || row.getName().isBlank()) {
                    result.addError(rowNum, "姓名不能为空");
                    rowNum++;
                    continue;
                }
                if (row.getClassGroupName() == null || row.getClassGroupName().isBlank()) {
                    result.addError(rowNum, "班级名称不能为空");
                    rowNum++;
                    continue;
                }
                Optional<ClassGroup> cg = classGroupRepository.findByName(row.getClassGroupName());
                if (cg.isEmpty()) {
                    result.addError(rowNum, "班级'" + row.getClassGroupName() + "'不存在");
                    rowNum++;
                    continue;
                }
                Student student = new Student();
                student.setName(row.getName());
                if (row.getAge() != null && !row.getAge().isBlank()) {
                    try { student.setAge(Integer.parseInt(row.getAge().trim())); }
                    catch (NumberFormatException e) { result.addError(rowNum, "年龄必须是整数"); rowNum++; continue; }
                }
                student.setMajor(row.getMajor());
                student.setClassGroupId(cg.get().getId());
                studentService.save(student);
                result.addSuccess();
            } catch (Exception e) {
                result.addError(rowNum, e.getMessage());
            }
            rowNum++;
        }
        return result;
    }

    public void exportToExcel(HttpServletResponse response) throws IOException {
        setResponseHeader(response, "学生列表");
        List<Student> students = studentRepository.findAll();
        List<StudentExcel> list = new ArrayList<>();
        for (Student s : students) {
            StudentExcel row = new StudentExcel();
            row.setName(s.getName());
            row.setAge(s.getAge() != null ? String.valueOf(s.getAge()) : "");
            row.setMajor(s.getMajor());
            if (s.getClassGroupId() != null) {
                classGroupRepository.findById(s.getClassGroupId())
                        .ifPresent(cg -> row.setClassGroupName(cg.getName()));
            }
            list.add(row);
        }
        EasyExcel.write(response.getOutputStream(), StudentExcel.class).sheet("学生列表").doWrite(list);
    }

    public void downloadTemplate(HttpServletResponse response) throws IOException {
        setResponseHeader(response, "学生导入模板");
        EasyExcel.write(response.getOutputStream(), StudentExcel.class)
                .sheet("学生导入模板").doWrite(new ArrayList<>());
    }

    private void setResponseHeader(HttpServletResponse response, String filename) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("UTF-8");
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + encoded + ".xlsx");
    }
}
