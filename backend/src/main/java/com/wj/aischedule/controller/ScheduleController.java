package com.wj.aischedule.controller;

import com.wj.aischedule.dto.ImportResult;
import com.wj.aischedule.dto.ScheduleVO;
import com.wj.aischedule.entity.Schedule;
import com.wj.aischedule.service.ScheduleService;
import com.wj.aischedule.service.ScheduleExcelService;
import com.wj.aischedule.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/schedule")
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private ScheduleExcelService scheduleExcelService;

    // 查询所有排课（返回完整信息）
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/list")
    public ResponseResult<List<ScheduleVO>> list() {
        List<ScheduleVO> scheduleVOList = scheduleService.getAllSchedulesWithDetails();
        return ResponseResult.success(scheduleVOList);
    }

    // 分页查询排课（高性能，批量加载，支持关键字搜索）
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/pageList")
    public ResponseResult<Map<String, Object>> pageList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {
        Map<String, Object> result = scheduleService.getSchedulesPagedWithDetails(page, size, keyword);
        return ResponseResult.success(result);
    }

    // 根据ID 查询（返回完整信息）
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseResult<ScheduleVO> getById(@PathVariable Long id) {
        ScheduleVO scheduleVO = scheduleService.getScheduleWithDetails(id);
        return ResponseResult.success(scheduleVO);
    }

    // 新增排课
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/add")
    public ResponseResult<Schedule> add(@RequestBody Schedule schedule) {
        return ResponseResult.success(scheduleService.saveSchedule(schedule));
    }

    // 更新排课
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/update")
    public ResponseResult<Schedule> update(@RequestBody Schedule schedule) {
        return ResponseResult.success(scheduleService.saveSchedule(schedule));
    }

    // 根据班级查询排课
    @PreAuthorize("@authz.isStudentClassOwnerOrAdmin(#classGroupId)")
    @GetMapping("/listByClassGroupId")
    public ResponseResult<List<Schedule>> listByClassGroupId(@RequestParam Long classGroupId) {
        List<Schedule> schedules = scheduleService.getSchedulesByClassGroupId(classGroupId);
        return ResponseResult.success(schedules);
    }

    // 根据教师ID查询排课（带详细信息）
    @PreAuthorize("@authz.isTeacherOrAdmin(#teacherId)")
    @GetMapping("/listByTeacherId")
    public ResponseResult<List<ScheduleVO>> listByTeacherId(@RequestParam Long teacherId) {
        List<ScheduleVO> schedules = scheduleService.getSchedulesByTeacherId(teacherId);
        return ResponseResult.success(schedules);
    }

    // 删除排课
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        scheduleService.deleteSchedule(id);
        return ResponseResult.success();
    }

    // 导入课表（Excel）
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/import")
    public ResponseResult<ImportResult> importSchedules(@RequestParam("file") MultipartFile file) throws IOException {
        return ResponseResult.success(scheduleExcelService.importFromExcel(file));
    }

    // 导出课表（Excel）
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/export")
    public void exportSchedules(HttpServletResponse response) throws IOException {
        scheduleExcelService.exportToExcel(response);
    }

    // 下载模板
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/template")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        scheduleExcelService.downloadTemplate(response);
    }
}
