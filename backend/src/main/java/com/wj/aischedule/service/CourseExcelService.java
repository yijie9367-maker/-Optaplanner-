package com.wj.aischedule.service;

import com.alibaba.excel.EasyExcel;
import com.wj.aischedule.dto.ImportResult;
import com.wj.aischedule.entity.Course;
import com.wj.aischedule.entity.Teacher;
import com.wj.aischedule.entity.excel.CourseExcel;
import com.wj.aischedule.repository.CourseRepository;
import com.wj.aischedule.repository.TeacherRepository;
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
public class CourseExcelService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    public ImportResult importFromExcel(MultipartFile file) throws IOException {
        ImportResult result = new ImportResult();
        List<CourseExcel> rows = EasyExcel.read(file.getInputStream())
                .head(CourseExcel.class).sheet().doReadSync();
        int rowNum = 2;
        for (CourseExcel row : rows) {
            try {
                if (row.getCode() == null || row.getCode().isBlank()) {
                    result.addError(rowNum, "课程编号不能为空");
                    rowNum++;
                    continue;
                }
                if (row.getTeacherName() == null || row.getTeacherName().isBlank()) {
                    result.addError(rowNum, "教师姓名不能为空");
                    rowNum++;
                    continue;
                }
                Optional<Teacher> teacher;
                if (row.getTeacherDepartment() != null && !row.getTeacherDepartment().isBlank()) {
                    teacher = teacherRepository.findByNameAndDepartment(row.getTeacherName(), row.getTeacherDepartment());
                    if (teacher.isEmpty()) {
                        result.addError(rowNum, "教师'" + row.getTeacherName() + "/" + row.getTeacherDepartment() + "'不存在");
                        rowNum++;
                        continue;
                    }
                } else {
                    Teacher t = teacherRepository.findByName(row.getTeacherName());
                    if (t == null) {
                        result.addError(rowNum, "教师'" + row.getTeacherName() + "'不存在");
                        rowNum++;
                        continue;
                    }
                    teacher = Optional.of(t);
                }
                Course course = new Course();
                course.setCode(row.getCode());
                course.setName(row.getName());
                course.setTeacherId(teacher.get().getId());
                if (row.getCredit() != null && !row.getCredit().isBlank()) {
                    try { course.setCredit(Double.parseDouble(row.getCredit().trim())); }
                    catch (NumberFormatException e) { result.addError(rowNum, "学分必须是数字"); rowNum++; continue; }
                }
                courseRepository.save(course);
                result.addSuccess();
            } catch (Exception e) {
                result.addError(rowNum, e.getMessage());
            }
            rowNum++;
        }
        return result;
    }

    public void exportToExcel(HttpServletResponse response) throws IOException {
        setResponseHeader(response, "课程列表");
        List<Course> courses = courseRepository.findAll();
        List<CourseExcel> list = new ArrayList<>();
        for (Course c : courses) {
            CourseExcel row = new CourseExcel();
            row.setCode(c.getCode());
            row.setName(c.getName());
            row.setCredit(c.getCredit() != null ? String.valueOf(c.getCredit()) : "");
            if (c.getTeacherId() != null) {
                teacherRepository.findById(c.getTeacherId()).ifPresent(t -> {
                    row.setTeacherName(t.getName());
                    row.setTeacherDepartment(t.getDepartment());
                });
            }
            list.add(row);
        }
        EasyExcel.write(response.getOutputStream(), CourseExcel.class).sheet("课程列表").doWrite(list);
    }

    public void downloadTemplate(HttpServletResponse response) throws IOException {
        setResponseHeader(response, "课程导入模板");
        EasyExcel.write(response.getOutputStream(), CourseExcel.class)
                .sheet("课程导入模板").doWrite(new ArrayList<>());
    }

    private void setResponseHeader(HttpServletResponse response, String filename) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("UTF-8");
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + encoded + ".xlsx");
    }
}
