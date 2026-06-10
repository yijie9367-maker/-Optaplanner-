package com.wj.aischedule.repository;

import com.wj.aischedule.entity.ClassGroupCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClassGroupCourseRepository extends JpaRepository<ClassGroupCourse, Long> {
    List<ClassGroupCourse> findByClassGroupIdAndSemester(Long classGroupId, String semester);

    @Modifying
    @Query("delete from ClassGroupCourse c where c.classGroupId = :classGroupId and c.semester = :semester")
    void deleteByClassGroupIdAndSemester(@Param("classGroupId") Long classGroupId, @Param("semester") String semester);
}