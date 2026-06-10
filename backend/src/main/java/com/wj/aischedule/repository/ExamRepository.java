package com.wj.aischedule.repository;

import com.wj.aischedule.entity.Exam;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExamRepository extends JpaRepository<Exam, Long> {

    List<Exam> findByTeacherId(Long teacherId);

    List<Exam> findByClassGroupId(Long classGroupId);

    List<Exam> findByCourseId(Long courseId);

    Optional<Exam> findByCourseIdAndClassGroupId(Long courseId, Long classGroupId);
}
