package com.wj.aischedule.repository;

import com.wj.aischedule.entity.ConstraintConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConstraintConfigRepository extends JpaRepository<ConstraintConfig, Long> {
    Optional<ConstraintConfig> findByConstraintKey(String constraintKey);
}
