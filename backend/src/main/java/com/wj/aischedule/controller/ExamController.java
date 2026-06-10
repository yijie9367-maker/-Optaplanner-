package com.wj.aischedule.controller;

import com.wj.aischedule.dto.ExamVO;
import com.wj.aischedule.dto.ImportResult;
import com.wj.aischedule.entity.Exam;
import com.wj.aischedule.security.AuthenticatedUser;
import com.wj.aischedule.service.ExamService;
import com.wj.aischedule.service.ExamExcelService;
import com.wj.aischedule.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/exam")
public class ExamController {

    @Autowired
    private ExamService examService;

    @Autowired
    private ExamExcelService examExcelService;

    // 管理员：查询所有考试（含名称信息）
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/list")
    public ResponseResult<List<ExamVO>> list() {
        return ResponseResult.success(examService.findAllVO());
    }

    // 教师：查询自己负责的考试
    @PreAuthorize("@authz.isTeacherOrAdmin(#teacherId)")
    @GetMapping("/listByTeacherId")
    public ResponseResult<List<ExamVO>> listByTeacherId(@RequestParam Long teacherId) {
        return ResponseResult.success(examService.findByTeacherId(teacherId));
    }

    // 学生：查询自己班级的考试
    @PreAuthorize("@authz.isStudentClassOwnerOrAdmin(#classGroupId)")
    @GetMapping("/listByClassGroupId")
    public ResponseResult<List<ExamVO>> listByClassGroupId(@RequestParam Long classGroupId) {
        return ResponseResult.success(examService.findByClassGroupId(classGroupId));
    }

    // 新增考试
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @PostMapping("/add")
    public ResponseResult<Exam> add(@RequestBody Exam exam, @AuthenticationPrincipal AuthenticatedUser currentUser) {
        try {
            return ResponseResult.success(examService.save(exam, currentUser.getRoleValue(), currentUser.getUserId()));
        } catch (RuntimeException ex) {
            return ResponseResult.error(ex.getMessage());
        }
    }

    // 更新考试
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @PutMapping("/update")
    public ResponseResult<Exam> update(@RequestBody Exam exam, @AuthenticationPrincipal AuthenticatedUser currentUser) {
        try {
            return ResponseResult.success(examService.save(exam, currentUser.getRoleValue(), currentUser.getUserId()));
        } catch (RuntimeException ex) {
            return ResponseResult.error(ex.getMessage());
        }
    }

    // 删除考试
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @DeleteMapping("/delete/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser currentUser) {
        try {
            examService.deleteById(id, currentUser.getRoleValue(), currentUser.getUserId());
            return ResponseResult.success();
        } catch (RuntimeException ex) {
            return ResponseResult.error(ex.getMessage());
        }
    }

    // 导入考试（Excel）
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/import")
    public ResponseResult<ImportResult> importExams(@RequestParam("file") MultipartFile file) throws IOException {
        return ResponseResult.success(examExcelService.importFromExcel(file));
    }

    // 导出考试（Excel）
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/export")
    public void exportExams(HttpServletResponse response) throws IOException {
        examExcelService.exportToExcel(response);
    }

    // 下载模板
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/template")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        examExcelService.downloadTemplate(response);
    }
}
