package com.wj.aischedule.service;

import com.wj.aischedule.entity.ClassGroup;
import com.wj.aischedule.entity.ClassGroupCourse;
import com.wj.aischedule.entity.Course;
import com.wj.aischedule.entity.Student;
import com.wj.aischedule.repository.ClassGroupCourseRepository;
import com.wj.aischedule.repository.ClassGroupRepository;
import com.wj.aischedule.repository.CourseRepository;
import com.wj.aischedule.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
public class ClassGroupService {

    @Autowired
    private ClassGroupRepository classGroupRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ClassGroupCourseRepository classGroupCourseRepository;

    // 查询全部班级
    public List<ClassGroup> getAllClassGroups() {
        List<ClassGroup> classGroups = classGroupRepository.findAll();
        // 自动计算每个班级的学生人数
        classGroups.forEach(this::updateStudentCount);
        return classGroups;
    }

    // 根据 ID 查询
    public ClassGroup getClassGroupById(Long id) {
        ClassGroup classGroup = classGroupRepository.findById(id).orElse(null);
        if (classGroup != null) {
            updateStudentCount(classGroup);
        }
        return classGroup;
    }

    // 新增 / 修改
    public ClassGroup saveClassGroup(ClassGroup classGroup) {
        // 保存前先更新学生人数
        updateStudentCount(classGroup);
        return classGroupRepository.save(classGroup);
    }

    // 删除
    public void deleteClassGroup(Long id) {
        classGroupRepository.deleteById(id);
    }

    /**
     * 根据班级ID的学生列表计算班级人数
     */
    public Integer getStudentCountByClassGroupId(Long classGroupId) {
        List<Student> students = studentRepository.findByClassGroupId(classGroupId);
        return (int) students.size();
    }

    /**
     * 自动更新班级的学生人数（不保存）
     */
    private void updateStudentCount(ClassGroup classGroup) {
        if (classGroup != null && classGroup.getId() != null) {
            int studentCount = getStudentCountByClassGroupId(classGroup.getId());
            classGroup.setStudentCount(studentCount);
        }
    }

    public List<Long> getAssignedCourseIds(Long classGroupId, String semester) {
        String resolvedSemester = normalizeSemester(semester);
        return classGroupCourseRepository.findByClassGroupIdAndSemester(classGroupId, resolvedSemester)
                .stream()
                .map(ClassGroupCourse::getCourseId)
                .toList();
    }

    public List<Course> getAssignedCourses(Long classGroupId, String semester) {
        List<Long> courseIds = getAssignedCourseIds(classGroupId, semester);
        if (courseIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<Course> courses = courseRepository.findAllById(courseIds);
        Map<Long, Course> courseMap = new LinkedHashMap<>();
        courses.forEach(course -> courseMap.put(course.getId(), course));

        List<Course> orderedCourses = new ArrayList<>();
        for (Long courseId : courseIds) {
            Course course = courseMap.get(courseId);
            if (course != null) {
                orderedCourses.add(course);
            }
        }
        return orderedCourses;
    }

    @Transactional
    public List<Long> assignCourses(Long classGroupId, String semester, List<Long> courseIds) {
        ClassGroup classGroup = getClassGroupById(classGroupId);
        if (classGroup == null) {
            throw new RuntimeException("班级不存在");
        }

        String resolvedSemester = normalizeSemester(semester);
        Set<Long> uniqueCourseIds = new LinkedHashSet<>();
        if (courseIds != null) {
            courseIds.stream().filter(Objects::nonNull).forEach(uniqueCourseIds::add);
        }

        classGroupCourseRepository.deleteByClassGroupIdAndSemester(classGroupId, resolvedSemester);
        classGroupCourseRepository.flush();

        if (uniqueCourseIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<Course> existingCourses = courseRepository.findAllById(uniqueCourseIds);
        if (existingCourses.size() != uniqueCourseIds.size()) {
            throw new RuntimeException("部分课程不存在，无法完成分配");
        }

        List<ClassGroupCourse> assignments = new ArrayList<>();
        for (Long courseId : uniqueCourseIds) {
            ClassGroupCourse assignment = new ClassGroupCourse();
            assignment.setClassGroupId(classGroupId);
            assignment.setCourseId(courseId);
            assignment.setSemester(resolvedSemester);
            assignments.add(assignment);
        }
        classGroupCourseRepository.saveAll(assignments);
        classGroupCourseRepository.flush();
        return assignments.stream().map(ClassGroupCourse::getCourseId).toList();
    }

    public String getDefaultSemester() {
        LocalDate now = LocalDate.now();
        return now.getMonthValue() <= 7
                ? now.getYear() + "-spring"
                : now.getYear() + "-autumn";
    }

    private String normalizeSemester(String semester) {
        if (semester == null || semester.isBlank()) {
            return getDefaultSemester();
        }
        return semester.trim();
    }
}
