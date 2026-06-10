package com.wj.aischedule.service;

import com.wj.aischedule.dto.ScheduleVO;
import com.wj.aischedule.entity.Course;
import com.wj.aischedule.entity.ClassGroup;
import com.wj.aischedule.entity.Classroom;
import com.wj.aischedule.entity.Schedule;
import com.wj.aischedule.entity.Teacher;
import com.wj.aischedule.repository.CourseRepository;
import com.wj.aischedule.repository.ClassGroupRepository;
import com.wj.aischedule.repository.ClassroomRepository;
import com.wj.aischedule.repository.ScheduleRepository;
import com.wj.aischedule.repository.TeacherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ScheduleService {

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
    private ClassGroupService classGroupService;

    // 查询所有排课
    public List<Schedule> getAllSchedules() {
        return scheduleRepository.findAll();
    }

    // 查询所有排课（带详细信息）
    public List<ScheduleVO> getAllSchedulesWithDetails() {
        List<Schedule> schedules = scheduleRepository.findAll();
        return schedules.stream().map(this::convertToScheduleVO).collect(Collectors.toList());
    }

    // 根据ID查询
    public Schedule getScheduleById(Long id) {
        return scheduleRepository.findById(id).orElse(null);
    }

    // 根据ID查询（带详细信息）
    public ScheduleVO getScheduleWithDetails(Long id) {
        Schedule schedule = scheduleRepository.findById(id).orElse(null);
        if (schedule == null) {
            return null;
        }
        return convertToScheduleVO(schedule);
    }

    // 新增 / 更新排课
    public Schedule saveSchedule(Schedule schedule) {
        return scheduleRepository.save(schedule);
    }

    // 删除排课
    public void deleteSchedule(Long id) {
        scheduleRepository.deleteById(id);
    }

    // 分页查询排课（批量加载，避免N+1问题）
    public Map<String, Object> getSchedulesPagedWithDetails(int page, int size, String keyword) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Schedule> schedulePage;
        if (keyword == null || keyword.trim().isEmpty()) {
            schedulePage = scheduleRepository.findAll(pageable);
        } else {
            schedulePage = scheduleRepository.searchSchedules(keyword.trim(), pageable);
        }

        List<Schedule> schedules = schedulePage.getContent();

        // 收集本页所有关联ID，做批量查询，彻底消灭N+1问题
        Set<Long> courseIds = schedules.stream().map(Schedule::getCourseId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> classGroupIds = schedules.stream().map(Schedule::getClassGroupId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> classroomIds = schedules.stream().map(Schedule::getClassroomId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> teacherIds = schedules.stream().map(Schedule::getTeacherId).filter(Objects::nonNull).collect(Collectors.toSet());

        Map<Long, Course> courseMap = courseRepository.findAllById(courseIds).stream()
                .collect(Collectors.toMap(Course::getId, c -> c));
        Map<Long, ClassGroup> classGroupMap = classGroupRepository.findAllById(classGroupIds).stream()
                .collect(Collectors.toMap(ClassGroup::getId, cg -> cg));
        Map<Long, Classroom> classroomMap = classroomRepository.findAllById(classroomIds).stream()
                .collect(Collectors.toMap(Classroom::getId, cr -> cr));
        Map<Long, Teacher> teacherMap = teacherRepository.findAllById(teacherIds).stream()
                .collect(Collectors.toMap(Teacher::getId, t -> t));

        List<ScheduleVO> records = schedules.stream()
                .map(s -> convertToScheduleVOFast(s, courseMap, classGroupMap, classroomMap, teacherMap))
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("total", schedulePage.getTotalElements());
        result.put("records", records);
        return result;
    }

    // 快速转换（使用预加载的Map，O(1)查找，无额外DB查询）
    private ScheduleVO convertToScheduleVOFast(Schedule schedule, Map<Long, Course> courseMap,
            Map<Long, ClassGroup> classGroupMap, Map<Long, Classroom> classroomMap, Map<Long, Teacher> teacherMap) {
        ScheduleVO vo = new ScheduleVO();
        vo.setId(schedule.getId());
        vo.setSemester(schedule.getSemester());
        vo.setWeekNumber(schedule.getWeekNumber());
        vo.setWeekDay(schedule.getWeekDay());
        vo.setTimeSlot(schedule.getTimeSlot());
        vo.setDuration(schedule.getDuration());
        vo.setIsConflict(schedule.getIsConflict());
        vo.setConflictReason(schedule.getConflictReason());
        vo.setScore(schedule.getScore());
        vo.setCreatedAt(schedule.getCreatedAt());
        vo.setUpdatedAt(schedule.getUpdatedAt());

        if (schedule.getCourseId() != null) {
            Course course = courseMap.get(schedule.getCourseId());
            if (course != null) {
                vo.setCourseId(course.getId());
                vo.setCourseCode(course.getCode());
                vo.setCourseName(course.getName());
            }
        }

        if (schedule.getClassGroupId() != null) {
            ClassGroup classGroup = classGroupMap.get(schedule.getClassGroupId());
            if (classGroup != null) {
                vo.setClassGroupId(classGroup.getId());
                vo.setClassName(classGroup.getName());
                vo.setStudentNum(classGroup.getStudentCount());
            }
        }

        if (schedule.getClassroomId() != null) {
            Classroom classroom = classroomMap.get(schedule.getClassroomId());
            if (classroom != null) {
                vo.setClassroomId(classroom.getId());
                vo.setBuilding(classroom.getBuilding());
                vo.setRoomName(classroom.getName());
                vo.setCapacity(classroom.getCapacity());
            }
        }

        if (schedule.getTeacherId() != null) {
            Teacher teacher = teacherMap.get(schedule.getTeacherId());
            if (teacher != null) {
                vo.setTeacherId(teacher.getId());
                vo.setTeacherName(teacher.getName());
                vo.setTitle(teacher.getTitle());
            }
        }

        return vo;
    }

    // 根据班级查询排课
    public List<Schedule> getSchedulesByClassGroupId(Long classGroupId) {
        return scheduleRepository.findByClassGroupId(classGroupId);
    }

    // 根据教师查询排课（带详细信息）
    public List<ScheduleVO> getSchedulesByTeacherId(Long teacherId) {
        List<Schedule> schedules = scheduleRepository.findByTeacherId(teacherId);
        return schedules.stream().map(this::convertToScheduleVO).collect(Collectors.toList());
    }

    /**
     * 将Schedule实体转换为ScheduleVO
     */
    private ScheduleVO convertToScheduleVO(Schedule schedule) {
        ScheduleVO vo = new ScheduleVO();

        // 基本信息
        vo.setId(schedule.getId());
        vo.setSemester(schedule.getSemester());
        vo.setWeekNumber(schedule.getWeekNumber());
        vo.setWeekDay(schedule.getWeekDay());
        vo.setTimeSlot(schedule.getTimeSlot());
        vo.setDuration(schedule.getDuration());
        vo.setIsConflict(schedule.getIsConflict());
        vo.setConflictReason(schedule.getConflictReason());
        vo.setScore(schedule.getScore());
        vo.setCreatedAt(schedule.getCreatedAt());
        vo.setUpdatedAt(schedule.getUpdatedAt());

        // 课程信息
        if (schedule.getCourseId() != null) {
            Course course = courseRepository.findById(schedule.getCourseId()).orElse(null);
            if (course != null) {
                vo.setCourseId(course.getId());
                vo.setCourseCode(course.getCode());
                vo.setCourseName(course.getName());
            }
        }

        // 班级信息
        if (schedule.getClassGroupId() != null) {
            ClassGroup classGroup = classGroupRepository.findById(schedule.getClassGroupId()).orElse(null);
            if (classGroup != null) {
                vo.setClassGroupId(classGroup.getId());
                vo.setClassName(classGroup.getName());
                // 计算该班级的实际学生人数
                Integer studentCount = classGroupService.getStudentCountByClassGroupId(classGroup.getId());
                vo.setStudentNum(studentCount);
            }
        }

        // 教室信息
        if (schedule.getClassroomId() != null) {
            Classroom classroom = classroomRepository.findById(schedule.getClassroomId()).orElse(null);
            if (classroom != null) {
                vo.setClassroomId(classroom.getId());
                vo.setBuilding(classroom.getBuilding());
                vo.setRoomName(classroom.getName());
                vo.setCapacity(classroom.getCapacity());
            }
        }

        // 教师信息
        if (schedule.getTeacherId() != null) {
            Teacher teacher = teacherRepository.findById(schedule.getTeacherId()).orElse(null);
            if (teacher != null) {
                vo.setTeacherId(teacher.getId());
                vo.setTeacherName(teacher.getName());
                vo.setTitle(teacher.getTitle());
            }
        }

        return vo;
    }
}
