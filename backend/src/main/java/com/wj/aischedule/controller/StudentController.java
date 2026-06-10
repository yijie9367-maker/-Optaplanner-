package com.wj.aischedule.controller;

import com.wj.aischedule.dto.ImportResult;
import com.wj.aischedule.entity.Student;
import com.wj.aischedule.service.AuthService;
import com.wj.aischedule.service.StudentService;
import com.wj.aischedule.service.StudentExcelService;
import com.wj.aischedule.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/student")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @Autowired
    private StudentExcelService studentExcelService;

    @Autowired
    private AuthService authService;

    // 查询所有学生（加 /list 路径）
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/list")
    public ResponseResult<List<Student>> getAllStudents() {
        List<Student> list = studentService.findAll();
        return ResponseResult.success(list); // code=200
    }

    // 根据ID查询
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseResult<Student> getStudentById(@PathVariable Long id) {
        Student student = studentService.findById(id);
        if (student == null) {
            return ResponseResult.error("学生不存在");
        }
        return ResponseResult.success(student);
    }

    // 新增学生
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/add")
    public ResponseResult<Student> addStudent(@RequestBody Student student) {
        try {
            Student saved = studentService.save(student);
            return ResponseResult.success(saved);
        } catch (RuntimeException ex) {
            return ResponseResult.error(ex.getMessage());
        }
    }

    // 更新学生
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/update")
    public ResponseResult<Student> updateStudent(@RequestBody Student student) {
        try {
            Student saved = studentService.save(student);
            return ResponseResult.success(saved);
        } catch (RuntimeException ex) {
            return ResponseResult.error(ex.getMessage());
        }
    }

    // 删除学生
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete/{id}")
    public ResponseResult<Void> deleteStudent(@PathVariable Long id) {
        studentService.deleteById(id);
        return ResponseResult.success();
    }

    // 导入学生（Excel）
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/import")
    public ResponseResult<ImportResult> importStudents(@RequestParam("file") MultipartFile file) throws IOException {
        return ResponseResult.success(studentExcelService.importFromExcel(file));
    }

    // 导出学生（Excel）
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/export")
    public void exportStudents(HttpServletResponse response) throws IOException {
        studentExcelService.exportToExcel(response);
    }

    // 下载导入模板
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/template")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        studentExcelService.downloadTemplate(response);
    }
    
    // 获取指定班级的学生人数
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/countByClass/{classGroupId}")
    public ResponseResult<Map<String, Object>> getStudentCountByClass(@PathVariable Long classGroupId) {
        List<Student> students = studentService.findByClassGroupId(classGroupId);
        Map<String, Object> result = new HashMap<>();
        result.put("classGroupId", classGroupId);
        result.put("count", students.size());
        return ResponseResult.success(result);
    }
    
    // 学生登录接口
    @PostMapping("/login")
    public ResponseResult<Map<String, Object>> studentLogin(@RequestBody Map<String, String> loginRequest) {
        try {
            return ResponseResult.success(authService.loginStudent(loginRequest.get("name"), loginRequest.get("password")));
        } catch (RuntimeException ex) {
            return ResponseResult.error(ex.getMessage());
        }
    }
    
    // 根据班级ID获取学生列表
    @PreAuthorize("@authz.isStudentClassOwnerOrAdmin(#classGroupId)")
    @GetMapping("/listByClassGroupId")
    public ResponseResult<List<Student>> listByClassGroupId(@RequestParam Long classGroupId) {
        List<Student> students = studentService.findByClassGroupId(classGroupId);
        return ResponseResult.success(students);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/mute")
    public ResponseResult<Student> updateMuteStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        boolean muted = Boolean.TRUE.equals(body.get("muted"));
        try {
            return ResponseResult.success(studentService.updateMuteStatus(id, muted));
        } catch (RuntimeException ex) {
            return ResponseResult.error(ex.getMessage());
        }
    }
}
