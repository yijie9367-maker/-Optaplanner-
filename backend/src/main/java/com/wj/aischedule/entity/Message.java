package com.wj.aischedule.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "message")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title; // 标题

    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String content; // 内容

    @Column(length = 50)
    private String type; // 消息类型: announcement(公告), notice(通知), event(事件)

    @Column(length = 20)
    private String target; // 目标受众: admin, teacher, student

    @Column(length = 50)
    private Long publisherId; // 发布者ID（管理员ID）

    @Column(length = 100)
    private String publisherName; // 发布者名称

    @Column(columnDefinition = "INT DEFAULT 0")
    private Integer views; // 浏览次数

    @Column(columnDefinition = "INT DEFAULT 0")
    private Integer likes; // 点赞数

    /**
     * 消息状态: 
     * published(已发布), 
     * draft(草稿), 
     * archived(归档)
     */
    @Column(columnDefinition = "VARCHAR(20) DEFAULT 'published'")
    private String status;

    private LocalDateTime publishTime; // 发布时间
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (publishTime == null && "published".equals(status)) {
            publishTime = LocalDateTime.now();
        }
        if (views == null) {
            views = 0;
        }
        if (likes == null) {
            likes = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public Long getPublisherId() {
        return publisherId;
    }

    public void setPublisherId(Long publisherId) {
        this.publisherId = publisherId;
    }

    public String getPublisherName() {
        return publisherName;
    }

    public void setPublisherName(String publisherName) {
        this.publisherName = publisherName;
    }

    public Integer getViews() {
        return views;
    }

    public void setViews(Integer views) {
        this.views = views;
    }

    public Integer getLikes() {
        return likes;
    }

    public void setLikes(Integer likes) {
        this.likes = likes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(LocalDateTime publishTime) {
        this.publishTime = publishTime;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
