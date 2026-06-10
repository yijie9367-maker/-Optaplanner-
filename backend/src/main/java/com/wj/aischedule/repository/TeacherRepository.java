package com.wj.aischedule.repository;

import com.wj.aischedule.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    Teacher findByName(String name);
    Optional<Teacher> findByNameAndDepartment(String name, String department);
}
