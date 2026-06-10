package com.wj.aischedule.repository;

import com.wj.aischedule.entity.ClassGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClassGroupRepository extends JpaRepository<ClassGroup, Long> {
    Optional<ClassGroup> findByName(String name);
}
