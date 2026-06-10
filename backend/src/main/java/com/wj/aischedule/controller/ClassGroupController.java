package com.wj.aischedule.controller;

import com.wj.aischedule.dto.ClassGroupCourseAssignmentRequest;
import com.wj.aischedule.dto.ImportResult;
import com.wj.aischedule.entity.ClassGroup;
import com.wj.aischedule.entity.Course;
import com.wj.aischedule.service.ClassGroupService;
import com.wj.aischedule.service.ClassGroupExcelService;
import com.wj.aischedule.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/classgroup")
public class ClassGroupController {

    @Autowired
    private ClassGroupService classGroupService;

    @Autowired
    private ClassGroupExcelService classGroupExcelService;

    // 查询全部
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/list")
    public ResponseResult<List<ClassGroup>> list() {
        return ResponseResult.success(classGroupService.getAllClassGroups());
    }

    // 根据 ID 查询
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseResult<ClassGroup> getById(@PathVariable Long id) {
        return ResponseResult.success(classGroupService.getClassGroupById(id));
    }

    // 新增
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/add")
    public ResponseResult<ClassGroup> add(@RequestBody ClassGroup classGroup) {
        return ResponseResult.success(classGroupService.saveClassGroup(classGroup));
    }

    // 修改
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/update")
    public ResponseResult<ClassGroup> update(@RequestBody ClassGroup classGroup) {
        return ResponseResult.success(classGroupService.saveClassGroup(classGroup));
    }

    // 删除
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        classGroupService.deleteClassGroup(id);
        return ResponseResult.success();
    }

    // 导入班级（Excel）
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/import")
    public ResponseResult<ImportResult> importClassGroups(@RequestParam("file") MultipartFile file) throws IOException {
        return ResponseResult.success(classGroupExcelService.importFromExcel(file));
    }

    // 导出班级（Excel）
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/export")
    public void exportClassGroups(HttpServletResponse response) throws IOException {
        classGroupExcelService.exportToExcel(response);
    }

    // 下载模板
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/template")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        classGroupExcelService.downloadTemplate(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}/courses")
    public ResponseResult<List<Long>> getAssignedCourseIds(
            @PathVariable Long id,
            @RequestParam(required = false) String semester) {
        return ResponseResult.success(classGroupService.getAssignedCourseIds(id, semester));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}/course-details")
    public ResponseResult<List<Course>> getAssignedCourses(
            @PathVariable Long id,
            @RequestParam(required = false) String semester) {
        return ResponseResult.success(classGroupService.getAssignedCourses(id, semester));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/courses")
    public ResponseResult<List<Long>> assignCourses(
            @PathVariable Long id,
            @RequestBody ClassGroupCourseAssignmentRequest body) {
        try {
            return ResponseResult.success(classGroupService.assignCourses(id, body.getSemester(), body.getCourseIds()));
        } catch (RuntimeException ex) {
            return ResponseResult.error(ex.getMessage());
        }
    }
}
