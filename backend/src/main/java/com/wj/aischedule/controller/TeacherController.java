package com.wj.aischedule.controller;

import com.wj.aischedule.dto.ImportResult;
import com.wj.aischedule.entity.Teacher;
import com.wj.aischedule.service.AuthService;
import com.wj.aischedule.service.TeacherService;
import com.wj.aischedule.service.TeacherExcelService;
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
@RequestMapping("/teacher")
public class TeacherController {

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private TeacherExcelService teacherExcelService;

    @Autowired
    private AuthService authService;

    // 查询所有教师
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @GetMapping("/list")
    public ResponseResult<List<Teacher>> list() {
        return ResponseResult.success(teacherService.findAll());
    }

    // 根据 ID 查询
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @GetMapping("/{id}")
    public ResponseResult<Teacher> getById(@PathVariable Long id) {
        return ResponseResult.success(teacherService.findById(id));
    }

    // 新增教师
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/add")
    public ResponseResult<Teacher> add(@RequestBody Teacher teacher) {
        return ResponseResult.success(teacherService.save(teacher));
    }

    // 更新教师
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/update")
    public ResponseResult<Teacher> update(@RequestBody Teacher teacher) {
        return ResponseResult.success(teacherService.save(teacher));
    }

    // 删除教师
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        teacherService.deleteById(id);
        return ResponseResult.success();
    }

    // 导入教师（Excel）
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/import")
    public ResponseResult<ImportResult> importTeachers(@RequestParam("file") MultipartFile file) throws IOException {
        return ResponseResult.success(teacherExcelService.importFromExcel(file));
    }

    // 导出教师（Excel）
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/export")
    public void exportTeachers(HttpServletResponse response) throws IOException {
        teacherExcelService.exportToExcel(response);
    }

    // 下载模板
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/template")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        teacherExcelService.downloadTemplate(response);
    }

    // 教师登录
    @PostMapping("/login")
    public ResponseResult<Map<String, Object>> login(@RequestBody Map<String, String> loginRequest) {
        try {
            return ResponseResult.success(authService.loginTeacher(loginRequest.get("name"), loginRequest.get("password")));
        } catch (RuntimeException ex) {
            return ResponseResult.error(ex.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/mute")
    public ResponseResult<Teacher> updateMuteStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        boolean muted = Boolean.TRUE.equals(body.get("muted"));
        try {
            return ResponseResult.success(teacherService.updateMuteStatus(id, muted));
        } catch (RuntimeException ex) {
            return ResponseResult.error(ex.getMessage());
        }
    }
}
