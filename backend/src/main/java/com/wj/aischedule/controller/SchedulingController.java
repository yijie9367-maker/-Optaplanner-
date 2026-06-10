package com.wj.aischedule.controller;

import com.wj.aischedule.common.Result;
import com.wj.aischedule.optaplanner.domain.TimeTable;
import com.wj.aischedule.service.SchedulingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 智能排课控制器
 */
@RestController
@RequestMapping("/scheduling")
public class SchedulingController {

    @Autowired
    private SchedulingService schedulingService;

    /**
     * 执行智能排课
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/solve")
    public Result<TimeTable> solve(@RequestBody TimeTable problem) {
        try {
            TimeTable solution = schedulingService.solve(problem);
            return Result.success("排课求解完成", solution);
        } catch (Exception e) {
            return Result.error("排课求解失败: " + e.getMessage());
        }
    }

    /**
     * 一键智能排课
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/auto-schedule")
    public Result<TimeTable> autoSchedule() {
        try {
            TimeTable solution = schedulingService.autoSchedule();
            return Result.success("智能排课完成", solution);
        } catch (Exception e) {
            return Result.error("智能排课失败: " + e.getMessage());
        }
    }

    /**
     * 获取排课统计信息
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/stats")
    public Result<Map<String, Object>> getStats(@RequestBody TimeTable timeTable) {
        try {
            Map<String, Object> stats = schedulingService.getScheduleStats(timeTable);
            return Result.success("统计信息获取成功", stats);
        } catch (Exception e) {
            return Result.error("统计信息获取失败: " + e.getMessage());
        }
    }

    /**
     * 检查排课冲突
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/check-conflicts")
    public Result<Map<String, Object>> checkConflicts(@RequestBody TimeTable timeTable) {
        try {
            // 这里可以添加具体的冲突检查逻辑
            Map<String, Object> result = Map.of(
                    "totalLessons", timeTable.getLessonList().size(),
                    "conflictCount", timeTable.getConflictCount(),
                    "conflictedLessons", timeTable.getConflictedLessons(),
                    "hasConflicts", timeTable.getConflictCount() > 0);
            return Result.success("冲突检查完成", result);
        } catch (Exception e) {
            return Result.error("冲突检查失败: " + e.getMessage());
        }
    }

    /**
     * 获取排课建议（手动排课时的实时建议）
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/suggestions")
    public Result<Map<String, Object>> getSuggestions(@RequestBody Map<String, Object> request) {
        try {
            // 这里可以添加排课建议逻辑
            // 例如：为指定课程推荐合适的时间和教室

            Map<String, Object> suggestions = Map.of(
                    "message", "排课建议功能待实现",
                    "status", "development");

            return Result.success("获取建议成功", suggestions);
        } catch (Exception e) {
            return Result.error("获取建议失败: " + e.getMessage());
        }
    }

    /**
     * 导出排课结果
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/export")
    public Result<Map<String, Object>> exportSchedule(@RequestBody TimeTable timeTable) {
        try {
            // 这里可以添加导出逻辑（Excel、PDF等）

            Map<String, Object> exportResult = Map.of(
                    "message", "导出功能待实现",
                    "format", "Excel",
                    "status", "development",
                    "data", timeTable.getScheduledLessons());

            return Result.success("导出请求已接收", exportResult);
        } catch (Exception e) {
            return Result.error("导出失败: " + e.getMessage());
        }
    }

    /**
     * 手动调整排课
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/manual-adjust")
    public Result<TimeTable> manualAdjust(@RequestBody Map<String, Object> adjustment) {
        try {
            return Result.error("手动调整功能暂未实现");
        } catch (Exception e) {
            return Result.error("手动调整失败: " + e.getMessage());
        }
    }

    /**
     * 获取排课进度
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/progress/{problemId}")
    public Result<Map<String, Object>> getProgress(@PathVariable String problemId) {
        try {
            // 这里可以添加进度查询逻辑

            Map<String, Object> progress = Map.of(
                    "problemId", problemId,
                    "status", "completed", // 或 "solving", "queued"
                    "progress", 100,
                    "message", "进度查询功能待完善");

            return Result.success("进度查询成功", progress);
        } catch (Exception e) {
            return Result.error("进度查询失败: " + e.getMessage());
        }
    }

    /**
     * 取消排课求解
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/cancel/{problemId}")
    public Result<Map<String, Object>> cancelSolving(@PathVariable String problemId) {
        try {
            // 这里可以添加取消求解逻辑

            Map<String, Object> result = Map.of(
                    "problemId", problemId,
                    "cancelled", true,
                    "message", "取消功能待实现");

            return Result.success("取消请求已接收", result);
        } catch (Exception e) {
            return Result.error("取消失败: " + e.getMessage());
        }
    }

    /**
     * 保存排课结果到数据库
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/save")
    public Result<Void> saveSchedule(@RequestBody TimeTable timeTable) {
        try {
            schedulingService.saveScheduleToDatabase(timeTable);
            return Result.success("排课结果保存成功", null);
        } catch (Exception e) {
            return Result.error("保存失败：" + e.getMessage());
        }
    }
}
