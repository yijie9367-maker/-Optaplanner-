package com.wj.aischedule.repository;

import com.wj.aischedule.entity.ForumComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ForumCommentRepository extends JpaRepository<ForumComment, Long> {

    List<ForumComment> findAllByPostIdOrderByCreatedAtAsc(Long postId);

    long countByPostId(Long postId);
}
