package com.wj.aischedule.service;

import com.wj.aischedule.entity.Message;
import com.wj.aischedule.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    /**
     * 获取所有已发布的消息
     */
    public List<Message> findAllPublished() {
        return messageRepository.findByStatusOrderByPublishTimeDesc("published");
    }

    /**
     * 根据类型获取消息
     */
    public List<Message> findByType(String type) {
        return messageRepository.findByTypeAndStatusOrderByPublishTimeDesc(type, "published");
    }

    /**
     * 获取热门消息（浏览次数最多）
     */
    public List<Message> getHotMessages(int limit) {
        return messageRepository.findTopByViews("published", limit);
    }

    /**
     * 根据ID获取消息
     */
    public Message findById(Long id) {
        return messageRepository.findById(id).orElse(null);
    }

    /**
     * 获取所有消息（包含草稿和归档）
     */
    public List<Message> findAll() {
        return messageRepository.findAll();
    }

    /**
     * 根据目标受众获取已发布消息
     */
    public List<Message> findPublishedByTarget(String target) {
        return messageRepository.findByTargetAndStatusOrderByPublishTimeDesc(target, "published");
    }

    /**
     * 根据目标受众获取所有消息（含草稿）
     */
    public List<Message> findAllByTarget(String target) {
        return messageRepository.findByTargetOrderByCreatedAtDesc(target);
    }

    /**
     * 新增或更新消息
     */
    public Message save(Message message) {
        if (message.getId() == null) {
            // 新增时，如果状态为发布，设置发布时间
            if ("published".equals(message.getStatus())) {
                message.setPublishTime(LocalDateTime.now());
            }
        }
        return messageRepository.save(message);
    }

    /**
     * 发布消息（草稿转发布）
     */
    public Message publishMessage(Long id) {
        Message message = messageRepository.findById(id).orElse(null);
        if (message != null) {
            message.setStatus("published");
            message.setPublishTime(LocalDateTime.now());
            return messageRepository.save(message);
        }
        return null;
    }

    /**
     * 删除消息
     */
    public void deleteById(Long id) {
        messageRepository.deleteById(id);
    }

    /**
     * 增加浏览次数
     */
    public void incrementViews(Long id) {
        Message message = messageRepository.findById(id).orElse(null);
        if (message != null) {
            message.setViews(message.getViews() + 1);
            messageRepository.save(message);
        }
    }

    /**
     * 增加点赞数
     */
    public void incrementLikes(Long id) {
        Message message = messageRepository.findById(id).orElse(null);
        if (message != null) {
            message.setLikes(message.getLikes() + 1);
            messageRepository.save(message);
        }
    }

    /**
     * 获取消息统计信息
     */
    public Long getPublishedMessageCount() {
        return messageRepository.countByStatus("published");
    }
}
