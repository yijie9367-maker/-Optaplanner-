package com.wj.aischedule.controller;

import com.wj.aischedule.dto.ImportResult;
import com.wj.aischedule.entity.AdminUser;
import com.wj.aischedule.repository.CourseRepository;
import com.wj.aischedule.repository.ScheduleRepository;
import com.wj.aischedule.repository.StudentRepository;
import com.wj.aischedule.repository.TeacherRepository;
import com.wj.aischedule.service.AdminUserService;
import com.wj.aischedule.service.AdminUserExcelService;
import com.wj.aischedule.service.AuthService;
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
@RequestMapping("/admin")
public class AdminUserController {

    @Autowired
    private AdminUserService adminUserService;

    @Autowired
    private AdminUserExcelService adminUserExcelService;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private AuthService authService;

    // 登录
    @PostMapping("/login")
    public ResponseResult<Map<String, Object>> login(@RequestBody Map<String, String> loginData) {
        try {
            return ResponseResult.success(authService.loginAdmin(loginData.get("username"), loginData.get("password")));
        } catch (RuntimeException ex) {
            return ResponseResult.error(ex.getMessage());
        }
    }


    // 查询所有管理员
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/list")
    public ResponseResult<List<AdminUser>> list() {
        return ResponseResult.success(adminUserService.findAll());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/dashboard-stats")
    public ResponseResult<Map<String, Long>> getDashboardStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("studentCount", studentRepository.count());
        stats.put("teacherCount", teacherRepository.count());
        stats.put("courseCount", courseRepository.count());
        stats.put("scheduleCount", scheduleRepository.count());
        return ResponseResult.success(stats);
    }

    // 根据 ID 查询
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseResult<AdminUser> getById(@PathVariable Long id) {
        return ResponseResult.success(adminUserService.findById(id));
    }

    // 新增管理员
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/add")
    public ResponseResult<AdminUser> add(@RequestBody AdminUser adminUser) {
        return ResponseResult.success(adminUserService.save(adminUser));
    }

    // 更新管理员
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/update")
    public ResponseResult<AdminUser> update(@RequestBody AdminUser adminUser) {
        return ResponseResult.success(adminUserService.save(adminUser));
    }

    // 删除管理员
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        adminUserService.deleteById(id);
        return ResponseResult.success();
    }

    // 导入管理员（Excel）
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/import")
    public ResponseResult<ImportResult> importAdmins(@RequestParam("file") MultipartFile file) throws IOException {
        return ResponseResult.success(adminUserExcelService.importFromExcel(file));
    }

    // 导出管理员（Excel）
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/export")
    public void exportAdmins(HttpServletResponse response) throws IOException {
        adminUserExcelService.exportToExcel(response);
    }

    // 下载模板
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/template")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        adminUserExcelService.downloadTemplate(response);
    }
}
