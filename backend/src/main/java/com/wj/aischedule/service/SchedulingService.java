package com.wj.aischedule.service;

import com.wj.aischedule.entity.Schedule;
import com.wj.aischedule.entity.ClassGroupCourse;
import com.wj.aischedule.entity.Course;
import com.wj.aischedule.entity.ClassGroup;
import com.wj.aischedule.optaplanner.domain.*;
import com.wj.aischedule.repository.ClassGroupCourseRepository;
import com.wj.aischedule.repository.ClassGroupRepository;
import com.wj.aischedule.repository.ClassroomRepository;
import com.wj.aischedule.repository.CourseRepository;
import com.wj.aischedule.repository.ScheduleRepository;
import com.wj.aischedule.repository.TeacherRepository;
import org.optaplanner.core.api.solver.SolverJob;
import org.optaplanner.core.api.solver.SolverManager;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.config.solver.SolverManagerConfig;
import org.optaplanner.core.config.solver.termination.TerminationConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * 排课服务类
 * 处理智能排课逻辑
 */
@Service
public class SchedulingService {

    @Autowired
    private ClassroomRepository classroomRepository;
    @Autowired
    private com.wj.aischedule.repository.TimeslotRepository timeslotRepository;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private TeacherRepository teacherRepository;
    @Autowired
    private ClassGroupRepository classGroupRepository;

    @Autowired
    private ClassGroupCourseRepository classGroupCourseRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private ClassGroupService classGroupService;

    @Autowired
    private ConstraintConfigService constraintConfigService;

    private SolverManager<TimeTable, Long> solverManager;

    @PostConstruct
    public void init() {
        // 配置 Optaplanner 求解器
        SolverConfig solverConfig = SolverConfig.createFromXmlResource(
                "solverConfig.xml");

        // 如果没有配置文件，使用代码配置
        if (solverConfig == null) {
            solverConfig = new SolverConfig()
                    .withSolutionClass(TimeTable.class)
                    .withEntityClasses(Lesson.class)
                    .withConstraintProviderClass(com.wj.aischedule.optaplanner.solver.TimeTableConstraintProvider.class)
                    .withTerminationSpentLimit(Duration.ofSeconds(60)); // 增加到 60 秒求解时间，确保找到好方案
        }

        solverManager = SolverManager.create(solverConfig, new SolverManagerConfig());
    }

    /**
     * 生成排课问题数据 - 从数据库读取真实数据
     */
    public TimeTable generateSchedulingProblem() {
        TimeTable timeTable = new TimeTable(
                "schedule-" + System.currentTimeMillis(),
            getCurrentSemester(),
                "智能排课数据");

        // 生成时间槽（周一至周五，每天 5 个时间段）
        generateTimeslots(timeTable);

        // 从数据库加载教室
        loadRoomsFromDatabase(timeTable);

        // 从数据库加载课程并生成 Lessons
        loadLessonsFromDatabase(timeTable);

        return timeTable;
    }

    private void generateTimeslots(TimeTable timeTable) {
        String semester = getCurrentSemester();
        LocalTime[] startTimes = {
                LocalTime.of(8, 0), // 第1-2节
                LocalTime.of(10, 0), // 第3-4节
                LocalTime.of(14, 0), // 第5-6节
                LocalTime.of(16, 0), // 第7-8节
                LocalTime.of(19, 0) // 第9-10节
        };

        LocalTime[] endTimes = {
                LocalTime.of(9, 40),
                LocalTime.of(11, 40),
                LocalTime.of(15, 40),
                LocalTime.of(17, 40),
                LocalTime.of(20, 40)
        };

        long timeslotId = 1L;

        // 生成第 1-12 周的时间槽（3月1日～6月1日，约12周教学周期）
        for (int week = 1; week <= 12; week++) {
            for (DayOfWeek day : DayOfWeek.values()) {
                // 跳过周末
                if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
                    continue;
                }

                for (int i = 0; i < startTimes.length; i++) {
                    Timeslot timeslot = new Timeslot(
                            semester,
                            week,
                            day,
                            startTimes[i],
                            endTimes[i]);
                    timeslot.setId(timeslotId++);
                    timeTable.addTimeslot(timeslot);
                }
            }
        }
    }

    /**
     * 从数据库加载教室
     */
    private void loadRoomsFromDatabase(TimeTable timeTable) {
        List<com.wj.aischedule.entity.Classroom> classrooms = classroomRepository.findAll();
        long roomId = 1L;

        for (com.wj.aischedule.entity.Classroom classroom : classrooms) {
            Room room = new Room(
                    classroom.getName(),
                    classroom.getBuilding(),
                    classroom.getCapacity(),
                    classroom.getType());
            room.setId(roomId++);

            // 根据类型设置设备
            if ("多媒体教室".equals(classroom.getType())) {
                room.setHasProjector(true);
                room.setHasComputer(true);
            } else if ("实验室".equals(classroom.getType())) {
                room.setHasComputer(true);
            }

            timeTable.addRoom(room);
        }
    }

    /**
     * 从数据库加载课程并生成 Lessons
     * 优化版：每个班级随机分配8门课，每门课根据学时数生成多个课程实例
     */
    private void loadLessonsFromDatabase(TimeTable timeTable) {
        List<ClassGroup> classGroups = classGroupRepository.findAll();
        String semester = timeTable.getSemester() != null ? timeTable.getSemester() : getCurrentSemester();

        long nextLessonId = 1L;

        for (ClassGroup clazz : classGroups) {
            List<ClassGroupCourse> assignments = classGroupCourseRepository
                    .findByClassGroupIdAndSemester(clazz.getId(), semester);
            if (assignments.isEmpty()) {
                System.out.println("班级 " + clazz.getName() + " 在学期 " + semester + " 未分配课程，已跳过排课");
                continue;
            }

            List<Long> courseIds = new ArrayList<>();
            for (ClassGroupCourse assignment : assignments) {
                courseIds.add(assignment.getCourseId());
            }
            List<Course> selectedCourses = courseRepository.findAllById(courseIds);
            if (selectedCourses.isEmpty()) {
                System.out.println("班级 " + clazz.getName() + " 在学期 " + semester + " 的课程分配无有效课程，已跳过排课");
                continue;
            }

            for (Course course : selectedCourses) {
                // 获取授课教师信息
                Long teacherId = course.getTeacherId();
                String teacherName = "Unknown";
                if (teacherId != null) {
                    com.wj.aischedule.entity.Teacher teacher = teacherRepository.findById(teacherId)
                            .orElse(null);
                    if (teacher != null) {
                        teacherName = teacher.getName();
                    }
                }

                // 获取该班级的学生人数
                Integer studentCount = classGroupService.getStudentCountByClassGroupId(clazz.getId());
                if (studentCount == null || studentCount == 0) {
                    studentCount = 40; // 默认值
                }

                // 【改进】根据实际可用时间槽动态计算课程次数
                // 而不是固定的 46 或 24 次
                // 12 周 × 5 天 × 5 时段 = 300 个时间槽
                // 8 门课均衡分配：每门课约 12-15 次（一周 1-2 次）
                int classesNeeded = 12; // 固定为 12 次，这样 8 × 12 = 96 次，完全可以放入时间表

                // 如果数据库里有显式设置，可以用，但用合理值取代 46/24
                if (course.getTotalClassesNeeded() != null && course.getTotalClassesNeeded() > 0) {
                    // 上限 20，避免数据过多
                    classesNeeded = Math.min(course.getTotalClassesNeeded(), 20);
                    // 下限 8
                    classesNeeded = Math.max(classesNeeded, 8);
                }

                for (int i = 0; i < classesNeeded; i++) {
                    String courseCode = course.getCode() != null ? course.getCode()
                            : "COURSE-" + course.getId();
                    String courseName = course.getName() != null ? course.getName() : "未命名课程";

                    Lesson lesson = new Lesson(
                            courseCode,
                            courseName,
                            course.getCredit() != null ? course.getCredit().intValue() : 2,
                            studentCount,
                            teacherId != null ? teacherId : 1L,
                            teacherName,
                            clazz.getId(),
                            clazz.getName());

                    lesson.setId(nextLessonId++);
                    lesson.setDuration(2); // 每节课2课时

                    // 【改进】所有课程使用均匀分配，不区分专业课/非专业课
                    // 每门课的实例均匀分散在 1-12 周
                    lesson.setPreferredStartWeek(1);
                    lesson.setPreferredEndWeek(12);
                    lesson.setMaxWeek(12);

                    // 根据课程类型设置教室要求
                    String requiredRoomType = "普通教室";
                    if (courseName.contains("实验") || courseName.contains("实践")) {
                        requiredRoomType = "实验室";
                    } else if (courseName.contains("计算机") || courseName.contains("编程")
                            || courseName.contains("算法")) {
                        requiredRoomType = "多媒体教室";
                    }
                    lesson.setRequiredRoomType(requiredRoomType);

                    timeTable.addLesson(lesson);
                }
            }
        }
    }

    /**
     * 执行智能排课
     */
    public TimeTable solve(TimeTable timeTable) throws ExecutionException, InterruptedException {
        long problemId = System.currentTimeMillis();
        SolverJob<TimeTable, Long> solverJob = solverManager.solve(problemId, timeTable);
        return solverJob.getFinalBestSolution();
    }

    /**
     * 一键智能排课
     */
    public TimeTable autoSchedule() throws ExecutionException, InterruptedException {
        // 生成真实排课问题数据
        TimeTable timeTable = generateSchedulingProblem();

        // 将约束权重注入到每个 Lesson
        java.util.Map<String, Integer> weights = buildWeightMap();
        timeTable.getLessonList().forEach(lesson -> lesson.setConstraintWeights(weights));

        // 固定求解时间，与 solverConfig 保持一致，给局部搜索足够时间补全未排课程
        int solverSeconds = 60;
        SolverConfig solverConfig = SolverConfig.createFromXmlResource("solverConfig.xml");
        if (solverConfig == null) {
            solverConfig = new SolverConfig()
                    .withSolutionClass(TimeTable.class)
                    .withEntityClasses(Lesson.class)
                    .withConstraintProviderClass(com.wj.aischedule.optaplanner.solver.TimeTableConstraintProvider.class);
        }
        // 替换根级终止条件，避免与 XML 中已有的 secondsSpentLimit 冲突
        TerminationConfig terminationConfig = new TerminationConfig();
        terminationConfig.setSpentLimit(Duration.ofSeconds(solverSeconds));
        solverConfig.setTerminationConfig(terminationConfig);
        SolverManager<TimeTable, Long> dynamicSolverManager = SolverManager.create(solverConfig, new SolverManagerConfig());

        // 执行智能排课算法
        long problemId = System.currentTimeMillis();
        SolverJob<TimeTable, Long> job = dynamicSolverManager.solve(problemId, timeTable);
        TimeTable solution = job.getFinalBestSolution();

        // 保存排课结果到数据库
        saveScheduleToDatabase(solution);

        return solution;
    }

    /** 构建当前约束权重 Map */
    private java.util.Map<String, Integer> buildWeightMap() {
        java.util.Map<String, Integer> map = new java.util.HashMap<>();
        String[] keys = {"spreadCourseInstances","preferWeekRange","avoidEveningClasses",
                         "teacherTimePreference","roomTypePreference","timeSlotPreference","continuousScheduling"};
        int[] defaults = {100, 3, 2, 1, 1, 1, 1};
        for (int i = 0; i < keys.length; i++) {
            map.put(keys[i], constraintConfigService.getWeight(keys[i], defaults[i]));
        }
        return map;
    }

    private String getCurrentSemester() {
        LocalDate now = LocalDate.now();
        return now.getMonthValue() <= 7
                ? now.getYear() + "-spring"
                : now.getYear() + "-autumn";
    }

    /**
     * 保存排课结果到数据库
     */
    public void saveScheduleToDatabase(TimeTable timeTable) {
        saveSchedule(timeTable);
    }

    /**
     * 获取排课统计信息
     */
    public Map<String, Object> getScheduleStats(TimeTable timeTable) {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalLessons", timeTable.getLessonList().size());
        stats.put("scheduledLessons", timeTable.getScheduledLessons().size());
        stats.put("unscheduledLessons", timeTable.getUnscheduledLessons().size());
        stats.put("conflictCount", timeTable.getConflictCount());

        return stats;
    }

    /**
     * 保存排课结果到数据库
     * 优化版：处理新的多实例课程模型
     */
    public void saveSchedule(TimeTable timeTable) {
        // 清空旧的排课数据
        scheduleRepository.deleteAll();

        // 保存新的排课结果
        for (Lesson lesson : timeTable.getLessonList()) {
            if (lesson.getTimeslot() != null && lesson.getRoom() != null) {
                Schedule schedule = new Schedule();
                
                // 根据 lesson 的 courseCode 映射到真实 Course.id
                Course course = courseRepository.findByCode(lesson.getCourseCode()).orElse(null);
                if (course == null) {
                    // 找不到对应课程时跳过
                    continue;
                }
                
                schedule.setCourseId(course.getId());
                schedule.setClassGroupId(lesson.getClassGroupId());
                schedule.setClassroomId(lesson.getRoom().getId());
                schedule.setTeacherId(lesson.getTeacherId());
                schedule.setSemester(timeTable.getSemester());
                
                // 周号：1-12（3月1日～6月1日的教学周）
                int weekNumber = lesson.getTimeslot().getWeekNumber();
                schedule.setWeekNumber(Math.min(weekNumber, 12)); // 确保不超过12周
                
                schedule.setWeekDay(lesson.getTimeslot().getDayOfWeek().getValue());
                
                // 【修复】根据课程实际开始时间计算时间段
                int timeSlot = calculateTimeSlot(lesson.getTimeslot().getStartTime());
                schedule.setTimeSlot(timeSlot);
                
                schedule.setDuration(2);
                schedule.setIsConflict(false);

                scheduleRepository.save(schedule);
            }
        }
    }

    /**
     * 检查排课冲突
     */
    public Map<String, Object> checkConflicts(TimeTable timeTable) {
        Map<String, Object> result = new HashMap<>();
        List<String> conflicts = new ArrayList<>();

        // 检查教室冲突
        Map<Timeslot, Set<Room>> roomUsage = new HashMap<>();
        for (Lesson lesson : timeTable.getLessonList()) {
            if (lesson.getTimeslot() != null && lesson.getRoom() != null) {
                roomUsage.computeIfAbsent(lesson.getTimeslot(), k -> new HashSet<>()).add(lesson.getRoom());
            }
        }

        for (Map.Entry<Timeslot, Set<Room>> entry : roomUsage.entrySet()) {
            if (entry.getValue().size() > 1) {
                conflicts.add("教室冲突：" + entry.getKey() + " 被多个课程使用");
            }
        }

        // 检查教师冲突
        Map<Timeslot, Set<Long>> teacherUsage = new HashMap<>();
        for (Lesson lesson : timeTable.getLessonList()) {
            if (lesson.getTimeslot() != null) {
                teacherUsage.computeIfAbsent(lesson.getTimeslot(), k -> new HashSet<>()).add(lesson.getTeacherId());
            }
        }

        for (Map.Entry<Timeslot, Set<Long>> entry : teacherUsage.entrySet()) {
            if (entry.getValue().size() > 1) {
                conflicts.add("教师冲突：" + entry.getKey() + " 有多个课程安排同一教师");
            }
        }

        // 检查班级冲突
        Map<Timeslot, Set<Long>> classUsage = new HashMap<>();
        for (Lesson lesson : timeTable.getLessonList()) {
            if (lesson.getTimeslot() != null) {
                classUsage.computeIfAbsent(lesson.getTimeslot(), k -> new HashSet<>()).add(lesson.getClassGroupId());
            }
        }

        for (Map.Entry<Timeslot, Set<Long>> entry : classUsage.entrySet()) {
            if (entry.getValue().size() > 1) {
                conflicts.add("班级冲突：" + entry.getKey() + " 有多个课程安排同一班级");
            }
        }

        result.put("hasConflicts", !conflicts.isEmpty());
        result.put("conflictCount", conflicts.size());
        result.put("conflicts", conflicts);

        return result;
    }

    /**
     * 【新增】根据课程开始时间计算时间段编号
     * 时间段编号与开始时间的映射：
     * 08:00 -> 1-2 节  (timeSlot = 1)
     * 10:00 -> 3-4 节  (timeSlot = 3)
     * 14:00 -> 5-6 节  (timeSlot = 5)
     * 16:00 -> 7-8 节  (timeSlot = 7)
     * 19:00 -> 9-10 节 (timeSlot = 9)
     */
    private int calculateTimeSlot(java.time.LocalTime startTime) {
        int hour = startTime.getHour();
        
        if (hour == 8) {
            return 1; // 08:00-09:40
        } else if (hour == 10) {
            return 3; // 10:00-11:40
        } else if (hour == 14) {
            return 5; // 14:00-15:40（下午）
        } else if (hour == 16) {
            return 7; // 16:00-17:40（下午）
        } else if (hour == 19) {
            return 9; // 19:00-20:40（晚上）
        }
        
        // 默认第一节
        return 1;
    }

    /**
     * 获取排课建议
     */
    public Map<String, Object> getSuggestions(TimeTable timeTable) {
        Map<String, Object> suggestions = new HashMap<>();
        List<String> recommendationList = new ArrayList<>();

        int unscheduledCount = timeTable.getUnscheduledLessons().size();
        if (unscheduledCount > 0) {
            recommendationList.add("还有 " + unscheduledCount + " 门课程未排课，建议增加时间槽或教室资源");
        }

        int conflictCount = timeTable.getConflictCount();
        if (conflictCount > 0) {
            recommendationList.add("检测到 " + conflictCount + " 个冲突，请调整课程安排");
        }

        // 检查教室利用率
        int totalRooms = timeTable.getRoomList().size();
        int usedRooms = (int) timeTable.getLessonList().stream()
                .map(Lesson::getRoom)
                .filter(Objects::nonNull)
                .distinct()
                .count();

        double utilizationRate = (double) usedRooms / totalRooms * 100;
        if (utilizationRate < 50) {
            recommendationList.add("教室利用率较低 (" + String.format("%.1f", utilizationRate) + "%)，可以考虑合并某些课程");
        }

        suggestions.put("suggestions", recommendationList);
        return suggestions;
    }

    /**
     * 导出排课结果
     */
    public Map<String, Object> exportSchedule(TimeTable timeTable) {
        Map<String, Object> export = new HashMap<>();
        export.put("semester", timeTable.getSemester());
        export.put("generatedAt", new Date());
        export.put("totalLessons", timeTable.getLessonList().size());
        export.put("scheduledLessons", timeTable.getScheduledLessons().size());

        List<Map<String, Object>> lessons = new ArrayList<>();
        for (Lesson lesson : timeTable.getLessonList()) {
            if (lesson.getTimeslot() != null && lesson.getRoom() != null) {
                Map<String, Object> lessonData = new HashMap<>();
                lessonData.put("courseCode", lesson.getCourseCode());
                lessonData.put("courseName", lesson.getCourseName());
                lessonData.put("teacherId", lesson.getTeacherId());
                lessonData.put("classGroupId", lesson.getClassGroupId());
                lessonData.put("week", lesson.getTimeslot().getWeekNumber());
                lessonData.put("dayOfWeek", lesson.getTimeslot().getDayOfWeek().getValue());
                lessonData.put("startTime", lesson.getTimeslot().getStartTime().toString());
                lessonData.put("endTime", lesson.getTimeslot().getEndTime().toString());
                lessonData.put("room", lesson.getRoom().getName());
                lessons.add(lessonData);
            }
        }

        export.put("lessons", lessons);
        return export;
    }

    /**
     * 手动调整排课
     */
    public boolean manualAdjust(TimeTable timeTable, String lessonId, Long timeslotId, Long roomId) {
        Lesson lesson = timeTable.getLessonList().stream()
                .filter(l -> l.getId().equals(lessonId))
                .findFirst()
                .orElse(null);

        if (lesson == null) {
            return false;
        }

        Timeslot timeslot = timeTable.getTimeslotList().stream()
                .filter(t -> t.getId().equals(timeslotId))
                .findFirst()
                .orElse(null);

        Room room = timeTable.getRoomList().stream()
                .filter(r -> r.getId().equals(roomId))
                .findFirst()
                .orElse(null);

        if (timeslot != null && room != null) {
            lesson.setTimeslot(timeslot);
            lesson.setRoom(room);
            return true;
        }

        return false;
    }

    /**
     * 获取排课进度
     */
    public Map<String, Object> getProgress(Long problemId) {
        Map<String, Object> progress = new HashMap<>();
        progress.put("problemId", problemId);
        progress.put("status", "COMPLETED");
        progress.put("percentComplete", 100);
        return progress;
    }
}
