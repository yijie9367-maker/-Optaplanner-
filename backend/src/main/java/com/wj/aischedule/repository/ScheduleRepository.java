package com.wj.aischedule.repository;

import com.wj.aischedule.entity.Schedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    @Query("SELECT DISTINCT s FROM Schedule s WHERE s.classGroupId = :classGroupId")
    List<Schedule> findByClassGroupId(@Param("classGroupId") Long classGroupId);

    @Query("SELECT DISTINCT s FROM Schedule s WHERE s.teacherId = :teacherId")
    List<Schedule> findByTeacherId(@Param("teacherId") Long teacherId);

    @Query(value = "SELECT s.* FROM schedule s " +
            "LEFT JOIN course c ON s.course_id = c.id " +
            "LEFT JOIN teacher t ON s.teacher_id = t.id " +
            "LEFT JOIN class_group cg ON s.class_group_id = cg.id " +
            "WHERE (:keyword IS NULL OR c.name LIKE %:keyword% " +
            "OR t.name LIKE %:keyword% OR cg.name LIKE %:keyword% " +
            "OR c.code LIKE %:keyword%)",
            countQuery = "SELECT COUNT(s.id) FROM schedule s " +
            "LEFT JOIN course c ON s.course_id = c.id " +
            "LEFT JOIN teacher t ON s.teacher_id = t.id " +
            "LEFT JOIN class_group cg ON s.class_group_id = cg.id " +
            "WHERE (:keyword IS NULL OR c.name LIKE %:keyword% " +
            "OR t.name LIKE %:keyword% OR cg.name LIKE %:keyword% " +
            "OR c.code LIKE %:keyword%)",
            nativeQuery = true)
    Page<Schedule> searchSchedules(@Param("keyword") String keyword, Pageable pageable);
}
