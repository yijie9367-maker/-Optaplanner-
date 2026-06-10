package com.wj.aischedule.repository;

import com.wj.aischedule.entity.ForumVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ForumVoteRepository extends JpaRepository<ForumVote, Long> {

    Optional<ForumVote> findByPostIdAndUserIdAndUserRole(Long postId, Long userId, String userRole);

    void deleteByPostIdAndUserIdAndUserRole(Long postId, Long userId, String userRole);
}
