package com.wj.aischedule.controller;

import com.wj.aischedule.dto.ImportResult;
import com.wj.aischedule.entity.Course;
import com.wj.aischedule.entity.Teacher;
import com.wj.aischedule.service.CourseService;
import com.wj.aischedule.service.CourseExcelService;
import com.wj.aischedule.service.TeacherService;
import com.wj.aischedule.service.ClassGroupService;
import com.wj.aischedule.util.ResponseResult;
import com.wj.aischedule.dto.CourseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/course")
public class CourseController {

    @Autowired
    private CourseService courseService;
    
    @Autowired
    private TeacherService teacherService;

    @Autowired
    private ClassGroupService classGroupService;

    @Autowired
    private CourseExcelService courseExcelService;

    // 查询所有课程
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/list")
    public ResponseResult<List<Course>> list() {
        return ResponseResult.success(courseService.findAll());
    }

    // 根据 ID 查询课程
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseResult<Course> getById(@PathVariable Long id) {
        return ResponseResult.success(courseService.findById(id));
    }

    // 新增课程
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/add")
    public ResponseResult<Course> add(@RequestBody Course course) {
        return ResponseResult.success(courseService.save(course));
    }

    // 更新课程
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/update")
    public ResponseResult<Course> update(@RequestBody Course course) {
        return ResponseResult.success(courseService.save(course));
    }

    // 删除课程
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        courseService.deleteById(id);
        return ResponseResult.success();
    }

    // 导入课程（Excel）
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/import")
    public ResponseResult<ImportResult> importCourses(@RequestParam("file") MultipartFile file) throws IOException {
        return ResponseResult.success(courseExcelService.importFromExcel(file));
    }

    // 导出课程（Excel）
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/export")
    public void exportCourses(HttpServletResponse response) throws IOException {
        courseExcelService.exportToExcel(response);
    }

    // 下载模板
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/template")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        courseExcelService.downloadTemplate(response);
    }

    // 根据班级ID获取课程列表（从班级课程分配查询）
    @PreAuthorize("@authz.isStudentClassOwnerOrAdmin(#classGroupId)")
    @GetMapping("/listByClassGroupId")
    public ResponseResult<List<CourseDTO>> listByClassGroupId(
            @RequestParam Long classGroupId,
            @RequestParam(required = false) String semester) {
        List<Course> assignedCourses = classGroupService.getAssignedCourses(classGroupId, semester);
        List<CourseDTO> courses = assignedCourses.stream()
                .map(course -> {
                    CourseDTO dto = new CourseDTO();
                    dto.setId(course.getId());
                    dto.setName(course.getName());
                    dto.setCode(course.getCode());
                    dto.setCredit(course.getCredit());
                    dto.setTeacherId(course.getTeacherId());
                    dto.setCourseType(course.getCourseType());

                    if (course.getTeacherId() != null) {
                        Teacher teacher = teacherService.findById(course.getTeacherId());
                        if (teacher != null) {
                            dto.setTeacherName(teacher.getName());
                        }
                    }
                    return dto;
                })
                .collect(Collectors.toList());
        return ResponseResult.success(courses);
    }
}
