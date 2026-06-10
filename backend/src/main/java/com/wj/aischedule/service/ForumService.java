package com.wj.aischedule.service;

import com.wj.aischedule.dto.ForumPostVO;
import com.wj.aischedule.entity.ForumComment;
import com.wj.aischedule.entity.ForumPost;
import com.wj.aischedule.entity.ForumVote;
import com.wj.aischedule.entity.Student;
import com.wj.aischedule.entity.Teacher;
import com.wj.aischedule.repository.ForumCommentRepository;
import com.wj.aischedule.repository.ForumPostRepository;
import com.wj.aischedule.repository.ForumVoteRepository;
import com.wj.aischedule.repository.StudentRepository;
import com.wj.aischedule.repository.TeacherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ForumService {

    @Autowired
    private ForumPostRepository postRepository;

    @Autowired
    private ForumCommentRepository commentRepository;

    @Autowired
    private ForumVoteRepository voteRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    // ---- 帖子列表（置顶帖排首，关键词搜索，时间排序）----
    public Page<ForumPostVO> getPosts(String keyword, String sort, int page, int size, Long userId, String userRole) {
        Sort.Direction dir = "oldest".equals(sort) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, "createdAt"));

        // 先取置顶帖
        List<ForumPost> pinned = postRepository.findByIsPinnedTrue();

        Page<ForumPost> normalPage;
        if (keyword != null && !keyword.isBlank()) {
            normalPage = postRepository.findAllByTitleContainingIgnoreCase(keyword.trim(), pageable);
            // 搜索时不单独提置顶，直接走分页（置顶帖若符合关键词也在其中）
            return normalPage.map(p -> toVO(p, userId, userRole, false));
        } else {
            normalPage = postRepository.findAll(pageable);
        }

        // 无搜索时：第0页拼置顶
        if (page == 0) {
            List<ForumPost> merged = new ArrayList<>();
            for (ForumPost pin : pinned) {
                if (merged.stream().noneMatch(x -> x.getId().equals(pin.getId()))) {
                    merged.add(pin);
                }
            }
            for (ForumPost p : normalPage.getContent()) {
                if (merged.stream().noneMatch(x -> x.getId().equals(p.getId()))) {
                    merged.add(p);
                }
            }
            List<ForumPostVO> voList = new ArrayList<>();
            for (ForumPost p : merged) {
                voList.add(toVO(p, userId, userRole, false));
            }
            return new PageImpl<>(voList, pageable, normalPage.getTotalElements() + pinned.size());
        }

        return normalPage.map(p -> toVO(p, userId, userRole, false));
    }

    // ---- 帖子详情（含评论列表）----
    public ForumPostVO getPostDetail(Long id, Long userId, String userRole) {
        ForumPost post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("帖子不存在"));
        ForumPostVO vo = toVO(post, userId, userRole, true);
        return vo;
    }

    // ---- 发帖 ----
    public ForumPost createPost(String title, String content, Long authorId, String authorName, String authorRole) {
        ensureUserNotMuted(authorId, authorRole);
        ForumPost post = new ForumPost();
        post.setTitle(title);
        post.setContent(content);
        post.setAuthorId(authorId);
        post.setAuthorName(authorName);
        post.setAuthorRole(authorRole);
        return postRepository.save(post);
    }

    // ---- 编辑帖子（仅管理员）----
    public ForumPost updatePost(Long id, String title, String content) {
        ForumPost post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("帖子不存在"));
        post.setTitle(title);
        post.setContent(content);
        return postRepository.save(post);
    }

    // ---- 删除帖子（管理员或帖子作者）----
    @Transactional
    public void deletePost(Long id, Long requesterId, String requesterRole) {
        ForumPost post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("帖子不存在"));
        boolean isAdmin = "admin".equalsIgnoreCase(requesterRole);
        boolean isAuthor = post.getAuthorId() != null
                && post.getAuthorId().equals(requesterId)
                && post.getAuthorRole() != null
                && post.getAuthorRole().equalsIgnoreCase(requesterRole);
        if (!isAdmin && !isAuthor) {
            throw new RuntimeException("只有管理员或帖子作者可以删除帖子");
        }
        commentRepository.findAllByPostIdOrderByCreatedAtAsc(id)
                .forEach(c -> commentRepository.deleteById(c.getId()));
        voteRepository.findAll().stream()
                .filter(v -> v.getPostId().equals(id))
                .forEach(v -> voteRepository.deleteById(v.getId()));
        postRepository.deleteById(id);
    }

    // ---- 置顶/取消置顶（仅管理员）----
    public ForumPost pinPost(Long id, boolean pinned) {
        ForumPost post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("帖子不存在"));
        post.setPinned(pinned);
        return postRepository.save(post);
    }

    // ---- 点赞/踩 Toggle ----
    @Transactional
    public ForumPostVO vote(Long postId, Long userId, String userRole, String voteType) {
        ForumPost post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("帖子不存在"));

        Optional<ForumVote> existing = voteRepository.findByPostIdAndUserIdAndUserRole(postId, userId, userRole);

        if (existing.isPresent()) {
            ForumVote ev = existing.get();
            if (ev.getVoteType().equals(voteType)) {
                // 相同类型：取消
                voteRepository.delete(ev);
                if ("like".equals(voteType)) {
                    post.setLikeCount(Math.max(0, post.getLikeCount() - 1));
                } else {
                    post.setDislikeCount(Math.max(0, post.getDislikeCount() - 1));
                }
            } else {
                // 不同类型：切换
                if ("like".equals(voteType)) {
                    post.setLikeCount(post.getLikeCount() + 1);
                    post.setDislikeCount(Math.max(0, post.getDislikeCount() - 1));
                } else {
                    post.setDislikeCount(post.getDislikeCount() + 1);
                    post.setLikeCount(Math.max(0, post.getLikeCount() - 1));
                }
                ev.setVoteType(voteType);
                voteRepository.save(ev);
            }
        } else {
            // 新增
            ForumVote newVote = new ForumVote();
            newVote.setPostId(postId);
            newVote.setUserId(userId);
            newVote.setUserRole(userRole);
            newVote.setVoteType(voteType);
            voteRepository.save(newVote);
            if ("like".equals(voteType)) {
                post.setLikeCount(post.getLikeCount() + 1);
            } else {
                post.setDislikeCount(post.getDislikeCount() + 1);
            }
        }

        postRepository.save(post);
        return toVO(post, userId, userRole, false);
    }

    // ---- 发评论 ----
    public ForumComment addComment(Long postId, Long authorId, String authorName, String authorRole, String content) {
        if (!postRepository.existsById(postId)) {
            throw new RuntimeException("帖子不存在");
        }
        ensureUserNotMuted(authorId, authorRole);
        ForumComment comment = new ForumComment();
        comment.setPostId(postId);
        comment.setAuthorId(authorId);
        comment.setAuthorName(authorName);
        comment.setAuthorRole(authorRole);
        comment.setContent(content);
        return commentRepository.save(comment);
    }

    // ---- 删除评论（管理员）----
    public void deleteComment(Long id) {
        commentRepository.deleteById(id);
    }

    // ---- 内部转换 ----
    private ForumPostVO toVO(ForumPost post, Long userId, String userRole, boolean withComments) {
        ForumPostVO vo = new ForumPostVO();
        vo.setId(post.getId());
        vo.setTitle(post.getTitle());
        vo.setContent(post.getContent());
        vo.setAuthorId(post.getAuthorId());
        vo.setAuthorName(post.getAuthorName());
        vo.setAuthorRole(post.getAuthorRole());
        vo.setLikeCount(post.getLikeCount());
        vo.setDislikeCount(post.getDislikeCount());
        vo.setPinned(post.isPinned());
        vo.setCreatedAt(post.getCreatedAt());
        vo.setUpdatedAt(post.getUpdatedAt());
        vo.setCommentCount((int) commentRepository.countByPostId(post.getId()));

        if (userId != null && userRole != null) {
            voteRepository.findByPostIdAndUserIdAndUserRole(post.getId(), userId, userRole)
                    .ifPresent(v -> vo.setMyVote(v.getVoteType()));
        }

        if (withComments) {
            vo.setComments(commentRepository.findAllByPostIdOrderByCreatedAtAsc(post.getId()));
        }

        return vo;
    }

    private void ensureUserNotMuted(Long authorId, String authorRole) {
        if (authorId == null || authorRole == null || authorRole.isBlank()) {
            throw new RuntimeException("用户身份信息不完整");
        }
        if ("admin".equalsIgnoreCase(authorRole)) {
            return;
        }
        if ("student".equalsIgnoreCase(authorRole)) {
            Student student = studentRepository.findById(authorId)
                    .orElseThrow(() -> new RuntimeException("学生不存在"));
            if (Boolean.TRUE.equals(student.getIsMuted())) {
                throw new RuntimeException("你已被管理员禁言，当前不能发帖或评论");
            }
            return;
        }
        if ("teacher".equalsIgnoreCase(authorRole)) {
            Teacher teacher = teacherRepository.findById(authorId)
                    .orElseThrow(() -> new RuntimeException("教师不存在"));
            if (Boolean.TRUE.equals(teacher.getIsMuted())) {
                throw new RuntimeException("你已被管理员禁言，当前不能发帖或评论");
            }
            return;
        }
        throw new RuntimeException("不支持的用户角色");
    }
}
