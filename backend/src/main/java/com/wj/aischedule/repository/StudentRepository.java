package com.wj.aischedule.repository;

import com.wj.aischedule.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    
    // 根据班级ID查询学生
    List<Student> findByClassGroupId(Long classGroupId);
    
    // 根据名字查询学生
    Student findByName(String name);

    // 根据名字+班级联合查询（解决同名问题）
    Optional<Student> findByNameAndClassGroupId(String name, Long classGroupId);
}
