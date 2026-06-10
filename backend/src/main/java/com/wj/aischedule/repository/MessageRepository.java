package com.wj.aischedule.repository;

import com.wj.aischedule.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    // 查询所有已发布的消息（按发布时间倒序）
    List<Message> findByStatusOrderByPublishTimeDesc(String status);

    // 根据类型查询消息
    List<Message> findByTypeAndStatusOrderByPublishTimeDesc(String type, String status);

    // 根据目标受众查询已发布消息
    List<Message> findByTargetAndStatusOrderByPublishTimeDesc(String target, String status);

    // 根据目标受众查询所有消息（包含草稿）
    List<Message> findByTargetOrderByCreatedAtDesc(String target);

    // 查询指定状态的消息数量
    Long countByStatus(String status);

    // 查询浏览次数最多的消息（热门消息）
    @Query(value = "SELECT * FROM message WHERE status = ?1 ORDER BY views DESC LIMIT ?2", nativeQuery = true)
    List<Message> findTopByViews(String status, int limit);
}
