package com.wj.aischedule.controller;

import com.wj.aischedule.dto.GradeItem;
import com.wj.aischedule.dto.ImportResult;
import com.wj.aischedule.entity.ExamGrade;
import com.wj.aischedule.service.ExamGradeService;
import com.wj.aischedule.service.ExamGradeExcelService;
import com.wj.aischedule.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/exam-grade")
public class ExamGradeController {

    @Autowired
    private ExamGradeService examGradeService;

    @Autowired
    private ExamGradeExcelService examGradeExcelService;

    /**
     * 教师：查询某场考试某班级所有学生的成绩（含未录入的学生）
     */
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @GetMapping("/listByExamAndClass")
    public ResponseResult<List<GradeItem>> listByExamAndClass(
            @RequestParam Long examId,
            @RequestParam Long classGroupId) {
        return ResponseResult.success(
                examGradeService.findStudentsWithGradeByExamAndClass(examId, classGroupId));
    }

    /**
     * 学生：查询自己所有的考试成绩
     */
    @PreAuthorize("@authz.isStudentOrAdmin(#studentId)")
    @GetMapping("/listByStudentId")
    public ResponseResult<List<ExamGrade>> listByStudentId(@RequestParam Long studentId) {
        return ResponseResult.success(examGradeService.findByStudentId(studentId));
    }

    /**
     * 教师：批量保存一场考试的成绩
     * Body: { "examId": 1, "grades": [ { "studentId": 1, "score": 88.5 }, ... ] }
     */
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @PostMapping("/saveAll")
    public ResponseResult<Void> saveAll(@RequestBody Map<String, Object> body) {
        Long examId = Long.valueOf(body.get("examId").toString());
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> rawGrades = (List<Map<String, Object>>) body.get("grades");
        List<GradeItem> items = new java.util.ArrayList<>();
        if (rawGrades != null) {
            for (Map<String, Object> raw : rawGrades) {
                GradeItem item = new GradeItem();
                if (raw.get("studentId") != null) {
                    item.setStudentId(Long.valueOf(raw.get("studentId").toString()));
                }
                if (raw.get("score") != null && !raw.get("score").toString().isEmpty()) {
                    item.setScore(new java.math.BigDecimal(raw.get("score").toString()));
                }
                if (raw.get("studentName") != null) {
                    item.setStudentName(raw.get("studentName").toString());
                }
                items.add(item);
            }
        }
        examGradeService.saveAll(examId, items);
        return ResponseResult.success();
    }

    // 导入成绩（Excel）
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/import")
    public ResponseResult<ImportResult> importGrades(@RequestParam("file") MultipartFile file) throws IOException {
        return ResponseResult.success(examGradeExcelService.importFromExcel(file));
    }

    // 导出成绩（Excel）
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/export")
    public void exportGrades(HttpServletResponse response) throws IOException {
        examGradeExcelService.exportToExcel(response);
    }

    // 下载模板
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/template")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        examGradeExcelService.downloadTemplate(response);
    }
}
