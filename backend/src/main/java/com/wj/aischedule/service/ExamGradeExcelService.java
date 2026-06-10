package com.wj.aischedule.service;

import com.alibaba.excel.EasyExcel;
import com.wj.aischedule.dto.ImportResult;
import com.wj.aischedule.entity.ClassGroup;
import com.wj.aischedule.entity.Exam;
import com.wj.aischedule.entity.ExamGrade;
import com.wj.aischedule.entity.Student;
import com.wj.aischedule.entity.excel.ExamGradeExcel;
import com.wj.aischedule.repository.ClassGroupRepository;
import com.wj.aischedule.repository.CourseRepository;
import com.wj.aischedule.repository.ExamGradeRepository;
import com.wj.aischedule.repository.ExamRepository;
import com.wj.aischedule.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ExamGradeExcelService {

    @Autowired
    private ExamGradeRepository examGradeRepository;

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ClassGroupRepository classGroupRepository;

    @Autowired
    private StudentRepository studentRepository;

    public ImportResult importFromExcel(MultipartFile file) throws IOException {
        ImportResult result = new ImportResult();
        List<ExamGradeExcel> rows = EasyExcel.read(file.getInputStream())
                .head(ExamGradeExcel.class).sheet().doReadSync();
        int rowNum = 2;
        for (ExamGradeExcel row : rows) {
            try {
                // 1. 通过课程编号找课程
                if (row.getCourseCode() == null || row.getCourseCode().isBlank()) {
                    result.addError(rowNum, "课程编号不能为空");
                    rowNum++;
                    continue;
                }
                var courseOpt = courseRepository.findByCode(row.getCourseCode());
                if (courseOpt.isEmpty()) {
                    result.addError(rowNum, "课程编号'" + row.getCourseCode() + "'不存在");
                    rowNum++;
                    continue;
                }
                // 2. 通过班级名称找班级
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
                // 3. 通过课程ID+班级ID找考试
                Optional<Exam> examOpt = examRepository.findByCourseIdAndClassGroupId(
                        courseOpt.get().getId(), cgOpt.get().getId());
                if (examOpt.isEmpty()) {
                    result.addError(rowNum, "未找到课程'" + row.getCourseCode() + "'/班级'" + row.getClassGroupName() + "'对应的考试");
                    rowNum++;
                    continue;
                }
                // 4. 通过学生姓名+班级ID找学生
                if (row.getStudentName() == null || row.getStudentName().isBlank()) {
                    result.addError(rowNum, "学生姓名不能为空");
                    rowNum++;
                    continue;
                }
                Optional<Student> studentOpt = studentRepository.findByNameAndClassGroupId(
                        row.getStudentName(), cgOpt.get().getId());
                if (studentOpt.isEmpty()) {
                    result.addError(rowNum, "学生'" + row.getStudentName() + "'在班级'" + row.getClassGroupName() + "'中不存在");
                    rowNum++;
                    continue;
                }
                // 5. 保存/更新成绩（upsert）
                Long examId = examOpt.get().getId();
                Long studentId = studentOpt.get().getId();
                Optional<ExamGrade> existing = examGradeRepository.findByExamIdAndStudentId(examId, studentId);
                ExamGrade grade = existing.orElse(new ExamGrade());
                grade.setExamId(examId);
                grade.setStudentId(studentId);
                if (row.getScore() != null && !row.getScore().isBlank()) {
                    try { grade.setScore(new BigDecimal(row.getScore().trim())); }
                    catch (NumberFormatException e) { result.addError(rowNum, "成绩必须是数字"); rowNum++; continue; }
                }
                examGradeRepository.save(grade);
                result.addSuccess();
            } catch (Exception e) {
                result.addError(rowNum, e.getMessage());
            }
            rowNum++;
        }
        return result;
    }

    public void exportToExcel(HttpServletResponse response) throws IOException {
        setResponseHeader(response, "考试成绩列表");
        List<ExamGrade> grades = examGradeRepository.findAll();
        List<ExamGradeExcel> list = new ArrayList<>();
        for (ExamGrade g : grades) {
            ExamGradeExcel row = new ExamGradeExcel();
            // 通过examId找课程编号和班级名称
            examRepository.findById(g.getExamId()).ifPresent(exam -> {
                courseRepository.findById(exam.getCourseId()).ifPresent(c -> row.setCourseCode(c.getCode()));
                classGroupRepository.findById(exam.getClassGroupId()).ifPresent(cg -> row.setClassGroupName(cg.getName()));
            });
            studentRepository.findById(g.getStudentId()).ifPresent(s -> row.setStudentName(s.getName()));
            row.setScore(g.getScore() != null ? g.getScore().toPlainString() : "");
            list.add(row);
        }
        EasyExcel.write(response.getOutputStream(), ExamGradeExcel.class).sheet("考试成绩列表").doWrite(list);
    }

    public void downloadTemplate(HttpServletResponse response) throws IOException {
        setResponseHeader(response, "成绩导入模板");
        EasyExcel.write(response.getOutputStream(), ExamGradeExcel.class)
                .sheet("成绩导入模板").doWrite(new ArrayList<>());
    }

    private void setResponseHeader(HttpServletResponse response, String filename) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("UTF-8");
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + encoded + ".xlsx");
    }
}
