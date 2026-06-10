package com.wj.aischedule.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 论坛帖子实体类
 * 用于存储用户在论坛中发布的帖子信息，包含标题、内容、作者信息以及点赞/点踩统计等
 */
@Entity
@Table(name = "forum_post")
public class ForumPost {

    /** 帖子ID，主键自增 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 帖子标题 */
    private String title;

    /** 帖子正文内容，使用TEXT类型存储长文本 */
    @Column(columnDefinition = "TEXT")
    private String content;

    /** 发帖人用户ID */
    private Long authorId;

    /** 发帖人用户名 */
    private String authorName;

    /** 发帖人角色（如：admin、teacher、student） */
    private String authorRole;

    /** 点赞数量，默认为0 */
    private int likeCount = 0;

    /** 点踩数量，默认为0 */
    private int dislikeCount = 0;

    /** 是否置顶，默认为false（不置顶） */
    private boolean isPinned = false;

    /** 帖子创建时间 */
    private LocalDateTime createdAt;

    /** 帖子最后更新时间 */
    private LocalDateTime updatedAt;

    /** JPA持久化前自动设置创建时间和更新时间 */
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /** JPA更新前自动刷新更新时间 */
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Long getAuthorId() { return authorId; }
    public void setAuthorId(Long authorId) { this.authorId = authorId; }
    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    public String getAuthorRole() { return authorRole; }
    public void setAuthorRole(String authorRole) { this.authorRole = authorRole; }
    public int getLikeCount() { return likeCount; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }
    public int getDislikeCount() { return dislikeCount; }
    public void setDislikeCount(int dislikeCount) { this.dislikeCount = dislikeCount; }
    public boolean isPinned() { return isPinned; }
    public void setPinned(boolean pinned) { isPinned = pinned; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}