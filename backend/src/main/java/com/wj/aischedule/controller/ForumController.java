package com.wj.aischedule.controller;

import com.wj.aischedule.dto.ForumPostVO;
import com.wj.aischedule.entity.ForumComment;
import com.wj.aischedule.entity.ForumPost;
import com.wj.aischedule.security.AuthenticatedUser;
import com.wj.aischedule.service.ForumService;
import com.wj.aischedule.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/forum")
public class ForumController {

    @Autowired
    private ForumService forumService;

    // 获取帖子列表（搜索、排序、分页）
    @GetMapping("/posts")
    public ResponseResult<Page<ForumPostVO>> getPosts(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "newest") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String userRole,
            @AuthenticationPrincipal AuthenticatedUser currentUser) {
        Long resolvedUserId = currentUser != null ? currentUser.getUserId() : userId;
        String resolvedUserRole = currentUser != null ? currentUser.getRoleValue() : userRole;
        return ResponseResult.success(forumService.getPosts(keyword, sort, page, size, resolvedUserId, resolvedUserRole));
    }

    // 获取帖子详情（含评论）
    @GetMapping("/posts/{id}")
    public ResponseResult<ForumPostVO> getPostDetail(
            @PathVariable Long id,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String userRole,
            @AuthenticationPrincipal AuthenticatedUser currentUser) {
        Long resolvedUserId = currentUser != null ? currentUser.getUserId() : userId;
        String resolvedUserRole = currentUser != null ? currentUser.getRoleValue() : userRole;
        return ResponseResult.success(forumService.getPostDetail(id, resolvedUserId, resolvedUserRole));
    }

    // 发帖
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/posts")
    public ResponseResult<ForumPost> createPost(
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal AuthenticatedUser currentUser) {
        String title = (String) body.get("title");
        String content = (String) body.get("content");
        try {
            return ResponseResult.success(
                    forumService.createPost(title, content, currentUser.getUserId(), currentUser.getDisplayName(), currentUser.getRoleValue()));
        } catch (RuntimeException ex) {
            return ResponseResult.error(ex.getMessage());
        }
    }

    // 编辑帖子（管理员）
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/posts/{id}")
    public ResponseResult<ForumPost> updatePost(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String title = (String) body.get("title");
        String content = (String) body.get("content");
        return ResponseResult.success(forumService.updatePost(id, title, content));
    }

    // 删除帖子（管理员或帖子作者）
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/posts/{id}")
    public ResponseResult<Void> deletePost(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser currentUser) {
        try {
            forumService.deletePost(id, currentUser.getUserId(), currentUser.getRoleValue());
            return ResponseResult.success(null);
        } catch (RuntimeException ex) {
            return ResponseResult.error(ex.getMessage());
        }
    }

    // 置顶/取消置顶（管理员）
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/posts/{id}/pin")
    public ResponseResult<ForumPost> pinPost(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        boolean pinned = Boolean.TRUE.equals(body.get("pinned"));
        return ResponseResult.success(forumService.pinPost(id, pinned));
    }

    // 点赞/踩（toggle）
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/posts/{id}/vote")
    public ResponseResult<ForumPostVO> vote(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal AuthenticatedUser currentUser) {
        String voteType = (String) body.get("voteType");
        return ResponseResult.success(forumService.vote(id, currentUser.getUserId(), currentUser.getRoleValue(), voteType));
    }

    // 发评论
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/posts/{id}/comments")
    public ResponseResult<ForumComment> addComment(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal AuthenticatedUser currentUser) {
        String content = (String) body.get("content");
        try {
            return ResponseResult.success(
                    forumService.addComment(id, currentUser.getUserId(), currentUser.getDisplayName(), currentUser.getRoleValue(), content));
        } catch (RuntimeException ex) {
            return ResponseResult.error(ex.getMessage());
        }
    }

    // 删除评论（管理员）
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/comments/{id}")
    public ResponseResult<Void> deleteComment(@PathVariable Long id) {
        forumService.deleteComment(id);
        return ResponseResult.success(null);
    }
}
