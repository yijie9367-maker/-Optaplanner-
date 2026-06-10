package com.wj.aischedule.repository;

import com.wj.aischedule.entity.ExamGrade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExamGradeRepository extends JpaRepository<ExamGrade, Long> {

    List<ExamGrade> findByExamId(Long examId);

    List<ExamGrade> findByStudentId(Long studentId);

    Optional<ExamGrade> findByExamIdAndStudentId(Long examId, Long studentId);

    void deleteByExamId(Long examId);
}
