package com.wj.aischedule.service;

import com.wj.aischedule.dto.ExamVO;
import com.wj.aischedule.entity.ClassGroup;
import com.wj.aischedule.entity.Course;
import com.wj.aischedule.entity.Exam;
import com.wj.aischedule.entity.Teacher;
import com.wj.aischedule.repository.ExamRepository;
import com.wj.aischedule.service.ClassGroupService;
import com.wj.aischedule.service.CourseService;
import com.wj.aischedule.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExamService {

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private CourseService courseService;

    @Autowired
    private ClassGroupService classGroupService;

    @Autowired
    private TeacherService teacherService;

    public List<Exam> findAll() {
        return examRepository.findAll();
    }

    public Exam findById(Long id) {
        return examRepository.findById(id).orElse(null);
    }

    public Exam save(Exam exam, String requesterRole, Long requesterId) {
        if (requesterRole == null || requesterRole.isBlank()) {
            throw new RuntimeException("未授权，无法保存考试安排");
        }

        if (exam.getCourseId() == null) {
            throw new RuntimeException("请选择课程");
        }
        if (exam.getClassGroupId() == null) {
            throw new RuntimeException("请选择班级");
        }
        if (exam.getExamDate() == null) {
            throw new RuntimeException("请选择考试日期");
        }
        if (exam.getStartTime() == null || exam.getStartTime().isBlank()) {
            throw new RuntimeException("请选择开始时间");
        }
        if (exam.getEndTime() == null || exam.getEndTime().isBlank()) {
            throw new RuntimeException("请选择结束时间");
        }
        if (exam.getStartTime().compareTo(exam.getEndTime()) >= 0) {
            throw new RuntimeException("结束时间必须晚于开始时间");
        }

        Course course = courseService.findById(exam.getCourseId());
        if (course == null) {
            throw new RuntimeException("课程不存在");
        }

        ClassGroup classGroup = classGroupService.getClassGroupById(exam.getClassGroupId());
        if (classGroup == null) {
            throw new RuntimeException("班级不存在");
        }

        Long courseTeacherId = course.getTeacherId();
        if (courseTeacherId == null) {
            throw new RuntimeException("该课程未分配授课教师，无法创建考试");
        }

        if ("teacher".equals(requesterRole) && !courseTeacherId.equals(requesterId)) {
            throw new RuntimeException("教师只能为自己授课的课程创建或修改考试");
        }

        if (exam.getTeacherId() != null && !courseTeacherId.equals(exam.getTeacherId())) {
            throw new RuntimeException("只能为当前课程的授课教师创建考试");
        }

        exam.setTeacherId(courseTeacherId);

        Exam existing = examRepository.findByCourseIdAndClassGroupId(exam.getCourseId(), exam.getClassGroupId())
                .orElse(null);
        if (existing != null && (exam.getId() == null || !existing.getId().equals(exam.getId()))) {
            throw new RuntimeException("该班级的这门课程考试已存在");
        }

        return examRepository.save(exam);
    }

    public void deleteById(Long id, String requesterRole, Long requesterId) {
        if (requesterRole == null || requesterRole.isBlank()) {
            throw new RuntimeException("未授权，无法删除考试安排");
        }

        Exam exam = findById(id);
        if (exam == null) {
            throw new RuntimeException("考试安排不存在");
        }

        if ("teacher".equals(requesterRole) && !exam.getTeacherId().equals(requesterId)) {
            throw new RuntimeException("教师只能删除自己课程的考试安排");
        }

        examRepository.deleteById(id);
    }

    public List<ExamVO> findByTeacherId(Long teacherId) {
        return examRepository.findByTeacherId(teacherId)
                .stream().map(this::toVO).collect(Collectors.toList());
    }

    public List<ExamVO> findByClassGroupId(Long classGroupId) {
        return examRepository.findByClassGroupId(classGroupId)
                .stream().map(this::toVO).collect(Collectors.toList());
    }

    public List<ExamVO> findAllVO() {
        return examRepository.findAll()
                .stream().map(this::toVO).collect(Collectors.toList());
    }

    private ExamVO toVO(Exam exam) {
        ExamVO vo = new ExamVO();
        vo.setId(exam.getId());
        vo.setCourseId(exam.getCourseId());
        vo.setClassGroupId(exam.getClassGroupId());
        vo.setTeacherId(exam.getTeacherId());
        vo.setExamDate(exam.getExamDate());
        vo.setStartTime(exam.getStartTime());
        vo.setEndTime(exam.getEndTime());
        vo.setLocation(exam.getLocation());

        Course course = courseService.findById(exam.getCourseId());
        if (course != null) {
            vo.setCourseName(course.getName());
        }

        ClassGroup cg = classGroupService.getClassGroupById(exam.getClassGroupId());
        if (cg != null) {
            vo.setClassName(cg.getName());
        }

        Teacher teacher = teacherService.findById(exam.getTeacherId());
        if (teacher != null) {
            vo.setTeacherName(teacher.getName());
        }

        return vo;
    }
}
