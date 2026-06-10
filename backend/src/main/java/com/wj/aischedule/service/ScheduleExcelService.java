package com.wj.aischedule.service;

import com.alibaba.excel.EasyExcel;
import com.wj.aischedule.dto.ImportResult;
import com.wj.aischedule.dto.ScheduleVO;
import com.wj.aischedule.entity.ClassGroup;
import com.wj.aischedule.entity.Classroom;
import com.wj.aischedule.entity.Course;
import com.wj.aischedule.entity.Schedule;
import com.wj.aischedule.entity.Teacher;
import com.wj.aischedule.entity.excel.ScheduleExcel;
import com.wj.aischedule.repository.ClassGroupRepository;
import com.wj.aischedule.repository.ClassroomRepository;
import com.wj.aischedule.repository.CourseRepository;
import com.wj.aischedule.repository.ScheduleRepository;
import com.wj.aischedule.repository.TeacherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ScheduleExcelService {

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ClassGroupRepository classGroupRepository;

    @Autowired
    private ClassroomRepository classroomRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private ScheduleService scheduleService;

    public ImportResult importFromExcel(MultipartFile file) throws IOException {
        ImportResult result = new ImportResult();
        List<ScheduleExcel> rows = EasyExcel.read(file.getInputStream())
                .head(ScheduleExcel.class).sheet().doReadSync();
        int rowNum = 2;
        for (ScheduleExcel row : rows) {
            try {
                // 课程：课程编号唯一
                if (row.getCourseCode() == null || row.getCourseCode().isBlank()) {
                    result.addError(rowNum, "课程编号不能为空");
                    rowNum++;
                    continue;
                }
                Optional<Course> courseOpt = courseRepository.findByCode(row.getCourseCode());
                if (courseOpt.isEmpty()) {
                    result.addError(rowNum, "课程编号'" + row.getCourseCode() + "'不存在");
                    rowNum++;
                    continue;
                }
                // 班级
                if (row.getClassGroupName() == null || row.getClassGroupName().isBlank()) {
                    result.addError(rowNum, "班级名称不能为空");
                    rowNum++;
                    continue;
                }
                Optional<ClassGroup> cgOpt = classGroupRepository.findByName(row.getClassGroupName());
                if (cgOpt.isEmpty()) {
                    result.addError(rowNum, "班级'" + row.getClassGroupName() + "'不存在");
                    rowNum++;
                    continue;
                }
                // 教室
                if (row.getClassroomName() == null || row.getClassroomName().isBlank()) {
                    result.addError(rowNum, "教室名称不能为空");
                    rowNum++;
                    continue;
                }
                Optional<Classroom> roomOpt = classroomRepository.findByName(row.getClassroomName());
                if (roomOpt.isEmpty()) {
                    result.addError(rowNum, "教室'" + row.getClassroomName() + "'不存在");
                    rowNum++;
                    continue;
                }
                // 教师
                Optional<Teacher> teacherOpt;
                if (row.getTeacherDepartment() != null && !row.getTeacherDepartment().isBlank()) {
                    teacherOpt = teacherRepository.findByNameAndDepartment(row.getTeacherName(), row.getTeacherDepartment());
                    if (teacherOpt.isEmpty()) {
                        result.addError(rowNum, "教师'" + row.getTeacherName() + "/" + row.getTeacherDepartment() + "'不存在");
                        rowNum++;
                        continue;
                    }
                } else {
                    Teacher t = teacherRepository.findByName(row.getTeacherName());
                    if (t == null) {
                        result.addError(rowNum, "教师'" + row.getTeacherName() + "'不存在");
                        rowNum++;
                        continue;
                    }
                    teacherOpt = Optional.of(t);
                }
                Schedule schedule = new Schedule();
                schedule.setCourseId(courseOpt.get().getId());
                schedule.setClassGroupId(cgOpt.get().getId());
                schedule.setClassroomId(roomOpt.get().getId());
                schedule.setTeacherId(teacherOpt.get().getId());
                schedule.setSemester(row.getSemester());
                if (row.getWeekNumber() != null && !row.getWeekNumber().isBlank()) {
                    try { schedule.setWeekNumber(Integer.parseInt(row.getWeekNumber().trim())); }
                    catch (NumberFormatException e) { result.addError(rowNum, "第几周必须是整数"); rowNum++; continue; }
                }
                if (row.getWeekDay() != null && !row.getWeekDay().isBlank()) {
                    try { schedule.setWeekDay(Integer.parseInt(row.getWeekDay().trim())); }
                    catch (NumberFormatException e) { result.addError(rowNum, "星期必须是整数"); rowNum++; continue; }
                }
                if (row.getTimeSlot() != null && !row.getTimeSlot().isBlank()) {
                    try { schedule.setTimeSlot(Integer.parseInt(row.getTimeSlot().trim())); }
                    catch (NumberFormatException e) { result.addError(rowNum, "节次必须是整数"); rowNum++; continue; }
                }
                if (row.getDuration() != null && !row.getDuration().isBlank()) {
                    try { schedule.setDuration(Integer.parseInt(row.getDuration().trim())); }
                    catch (NumberFormatException e) { result.addError(rowNum, "时长必须是整数"); rowNum++; continue; }
                } else { schedule.setDuration(2); }
                scheduleRepository.save(schedule);
                result.addSuccess();
            } catch (Exception e) {
                result.addError(rowNum, e.getMessage());
            }
            rowNum++;
        }
        return result;
    }

    public void exportToExcel(HttpServletResponse response) throws IOException {
        setResponseHeader(response, "课表列表");
        List<ScheduleVO> schedules = scheduleService.getAllSchedulesWithDetails();
        List<ScheduleExcel> list = new ArrayList<>();
        for (ScheduleVO s : schedules) {
            ScheduleExcel row = new ScheduleExcel();
            row.setCourseCode(s.getCourseCode());
            row.setCourseName(s.getCourseName());
            row.setClassGroupName(s.getClassName());
            row.setClassroomName(s.getRoomName());
            row.setTeacherName(s.getTeacherName());
            if (s.getTeacherId() != null) {
                teacherRepository.findById(s.getTeacherId())
                        .ifPresent(t -> row.setTeacherDepartment(t.getDepartment()));
            }
            row.setSemester(s.getSemester());
            row.setWeekNumber(s.getWeekNumber() != null ? String.valueOf(s.getWeekNumber()) : "");
            row.setWeekDay(s.getWeekDay() != null ? String.valueOf(s.getWeekDay()) : "");
            row.setTimeSlot(s.getTimeSlot() != null ? String.valueOf(s.getTimeSlot()) : "");
            row.setDuration(s.getDuration() != null ? String.valueOf(s.getDuration()) : "");
            list.add(row);
        }
        EasyExcel.write(response.getOutputStream(), ScheduleExcel.class).sheet("课表列表").doWrite(list);
    }

    public void downloadTemplate(HttpServletResponse response) throws IOException {
        setResponseHeader(response, "课表导入模板");
        EasyExcel.write(response.getOutputStream(), ScheduleExcel.class)
                .sheet("课表导入模板").doWrite(new ArrayList<>());
    }

    private void setResponseHeader(HttpServletResponse response, String filename) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("UTF-8");
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + encoded + ".xlsx");
    }
}
