package com.wj.aischedule.controller;

import com.wj.aischedule.dto.ImportResult;
import com.wj.aischedule.entity.Classroom;
import com.wj.aischedule.service.ClassroomService;
import com.wj.aischedule.service.ClassroomExcelService;
import com.wj.aischedule.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/classroom")
public class ClassroomController {

    @Autowired
    private ClassroomService classroomService;

    @Autowired
    private ClassroomExcelService classroomExcelService;

    // 查询所有教室
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @GetMapping("/list")
    public ResponseResult<List<Classroom>> list() {
        return ResponseResult.success(classroomService.findAll());
    }

    // 根据 ID 查询教室
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @GetMapping("/{id}")
    public ResponseResult<Classroom> getById(@PathVariable Long id) {
        return ResponseResult.success(classroomService.findById(id));
    }

    // 新增教室
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/add")
    public ResponseResult<Classroom> add(@RequestBody Classroom classroom) {
        return ResponseResult.success(classroomService.save(classroom));
    }

    // 更新教室
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/update")
    public ResponseResult<Classroom> update(@RequestBody Classroom classroom) {
        return ResponseResult.success(classroomService.save(classroom));
    }

    // 删除教室
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        classroomService.deleteById(id);
        return ResponseResult.success();
    }

    // 导入教室（Excel）
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/import")
    public ResponseResult<ImportResult> importClassrooms(@RequestParam("file") MultipartFile file) throws IOException {
        return ResponseResult.success(classroomExcelService.importFromExcel(file));
    }

    // 导出教室（Excel）
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/export")
    public void exportClassrooms(HttpServletResponse response) throws IOException {
        classroomExcelService.exportToExcel(response);
    }

    // 下载模板
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/template")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        classroomExcelService.downloadTemplate(response);
    }
}
