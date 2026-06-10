package com.wj.aischedule.service;

import com.wj.aischedule.entity.Course;
import com.wj.aischedule.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseService {

    @Autowired
    private CourseRepository courseRepository;

    // 查询全部课程
    public List<Course> findAll() {
        return courseRepository.findAll();
    }

    // 根据 ID 查询
    public Course findById(Long id) {
        return courseRepository.findById(id).orElse(null);
    }

    // 新增 / 更新
    public Course save(Course course) {
        return courseRepository.save(course);
    }

    // 删除
    public void deleteById(Long id) {
        courseRepository.deleteById(id);
    }
}
