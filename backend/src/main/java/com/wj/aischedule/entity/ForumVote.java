package com.wj.aischedule.entity;

import javax.persistence.*;

/**
 * 论坛投票记录实体类
 * 用于记录用户对帖子的点赞/点踩行为，同一用户对同一帖子只能有一条投票记录
 */
@Entity
@Table(name = "forum_vote", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"postId", "userId", "userRole"})
})
public class ForumVote {

    /** 投票记录ID，主键自增 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 被投票的帖子ID */
    private Long postId;

    /** 投票用户ID */
    private Long userId;

    /** 投票用户角色（如：admin、teacher、student） */
    private String userRole;

    /** 投票类型：like（点赞）或 dislike（点踩） */
    private String voteType;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }
    public String getVoteType() { return voteType; }
    public void setVoteType(String voteType) { this.voteType = voteType; }
}