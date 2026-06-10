package com.wj.aischedule.service;

import com.alibaba.excel.EasyExcel;
import com.wj.aischedule.dto.ExamVO;
import com.wj.aischedule.dto.ImportResult;
import com.wj.aischedule.entity.ClassGroup;
import com.wj.aischedule.entity.Course;
import com.wj.aischedule.entity.Exam;
import com.wj.aischedule.entity.Teacher;
import com.wj.aischedule.entity.excel.ExamExcel;
import com.wj.aischedule.repository.ClassGroupRepository;
import com.wj.aischedule.repository.CourseRepository;
import com.wj.aischedule.repository.ExamRepository;
import com.wj.aischedule.repository.TeacherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ExamExcelService {

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ClassGroupRepository classGroupRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private ExamService examService;

    public ImportResult importFromExcel(MultipartFile file) throws IOException {
        ImportResult result = new ImportResult();
        List<ExamExcel> rows = EasyExcel.read(file.getInputStream())
                .head(ExamExcel.class).sheet().doReadSync();
        int rowNum = 2;
        for (ExamExcel row : rows) {
            try {
                if (row.getCourseCode() == null || row.getCourseCode().isBlank()) {
                    result.addError(rowNum, "课程编号不能为空");
                    rowNum++;
                    continue;
                }
                Optional<Course> courseOpt = courseRepository.findByCode(row.getCourseCode());
                if (courseOpt.isEmpty()) {
                    result.addError(rowNum, "课程编号'" + row.getCourseCode() + "'不存在");
                    rowNum++;
                    continue;
                }
                if (row.getClassGroupName() == null || row.getClassGroupName().isBlank()) {
                    result.addError(rowNum, "班级名称不能为空");
                    rowNum++;
                    continue;
                }
                Optional<ClassGroup> cgOpt = classGroupRepository.findByName(row.getClassGroupName());
                if (cgOpt.isEmpty()) {
                    result.addError(rowNum, "班级'" + row.getClassGroupName() + "'不存在");
                    rowNum++;
                    continue;
                }
                Optional<Teacher> teacherOpt;
                if (row.getTeacherDepartment() != null && !row.getTeacherDepartment().isBlank()) {
                    teacherOpt = teacherRepository.findByNameAndDepartment(row.getTeacherName(), row.getTeacherDepartment());
                    if (teacherOpt.isEmpty()) {
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
                    teacherOpt = Optional.of(t);
                }
                Exam exam = new Exam();
                exam.setCourseId(courseOpt.get().getId());
                exam.setClassGroupId(cgOpt.get().getId());
                exam.setTeacherId(teacherOpt.get().getId());
                if (row.getExamDate() != null && !row.getExamDate().isBlank()) {
                    exam.setExamDate(LocalDate.parse(row.getExamDate()));
                }
                exam.setStartTime(row.getStartTime());
                exam.setEndTime(row.getEndTime());
                exam.setLocation(row.getLocation());
                examRepository.save(exam);
                result.addSuccess();
            } catch (Exception e) {
                result.addError(rowNum, e.getMessage());
            }
            rowNum++;
        }
        return result;
    }

    public void exportToExcel(HttpServletResponse response) throws IOException {
        setResponseHeader(response, "考试列表");
        List<ExamVO> exams = examService.findAllVO();
        List<ExamExcel> list = new ArrayList<>();
        for (ExamVO e : exams) {
            ExamExcel row = new ExamExcel();
            // 用课程编号
            if (e.getCourseId() != null) {
                courseRepository.findById(e.getCourseId())
                        .ifPresent(c -> row.setCourseCode(c.getCode()));
            }
            row.setClassGroupName(e.getClassName());
            row.setTeacherName(e.getTeacherName());
            if (e.getTeacherId() != null) {
                teacherRepository.findById(e.getTeacherId())
                        .ifPresent(t -> row.setTeacherDepartment(t.getDepartment()));
            }
            row.setExamDate(e.getExamDate() != null ? e.getExamDate().toString() : "");
            row.setStartTime(e.getStartTime());
            row.setEndTime(e.getEndTime());
            row.setLocation(e.getLocation());
            list.add(row);
        }
        EasyExcel.write(response.getOutputStream(), ExamExcel.class).sheet("考试列表").doWrite(list);
    }

    public void downloadTemplate(HttpServletResponse response) throws IOException {
        setResponseHeader(response, "考试导入模板");
        EasyExcel.write(response.getOutputStream(), ExamExcel.class)
                .sheet("考试导入模板").doWrite(new ArrayList<>());
    }

    private void setResponseHeader(HttpServletResponse response, String filename) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("UTF-8");
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + encoded + ".xlsx");
    }
}
