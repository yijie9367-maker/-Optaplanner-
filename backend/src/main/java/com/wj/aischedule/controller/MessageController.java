package com.wj.aischedule.controller;

import com.wj.aischedule.entity.Message;
import com.wj.aischedule.dto.MessageVO;
import com.wj.aischedule.service.MessageService;
import com.wj.aischedule.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

@RestController
@RequestMapping("/message")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @GetMapping("/list")
    public ResponseResult<List<MessageVO>> getAllPublishedMessages() {
        List<Message> messages = messageService.findAllPublished();
        return ResponseResult.success(convertToVO(messages));
    }

    @GetMapping("/list/{type}")
    public ResponseResult<List<MessageVO>> getMessagesByType(@PathVariable String type) {
        List<Message> messages = messageService.findByType(type);
        return ResponseResult.success(convertToVO(messages));
    }

    @GetMapping("/hotlist")
    public ResponseResult<List<MessageVO>> getHotMessages(@RequestParam(defaultValue = "5") int limit) {
        List<Message> messages = messageService.getHotMessages(limit);
        return ResponseResult.success(convertToVO(messages));
    }

    @GetMapping("/{id}")
    public ResponseResult<MessageVO> getMessageDetail(@PathVariable Long id) {
        Message message = messageService.findById(id);
        if (message == null) {
            return ResponseResult.error("消息不存在");
        }
        messageService.incrementViews(id);
        return ResponseResult.success(convertMessageToVO(message));
    }

    @PostMapping("/add")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseResult<MessageVO> addMessage(@RequestBody Message message) {
        if (message.getTitle() == null || message.getTitle().isEmpty()) {
            return ResponseResult.error("标题不能为空");
        }
        if (message.getContent() == null || message.getContent().isEmpty()) {
            return ResponseResult.error("内容不能为空");
        }
        if (message.getStatus() == null) {
            message.setStatus("published");
        }
        Message saved = messageService.save(message);
        return ResponseResult.success(convertMessageToVO(saved));
    }

    @PutMapping("/update")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseResult<MessageVO> updateMessage(@RequestBody Message message) {
        if (message.getId() == null) {
            return ResponseResult.error("消息ID不能为空");
        }
        Message existing = messageService.findById(message.getId());
        if (existing == null) {
            return ResponseResult.error("消息不存在");
        }
        if (message.getPublishTime() == null) {
            message.setPublishTime(existing.getPublishTime());
        }
        if (message.getViews() == null) {
            message.setViews(existing.getViews());
        }
        if (message.getLikes() == null) {
            message.setLikes(existing.getLikes());
        }
        Message updated = messageService.save(message);
        return ResponseResult.success(convertMessageToVO(updated));
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseResult<Void> deleteMessage(@PathVariable Long id) {
        Message message = messageService.findById(id);
        if (message == null) {
            return ResponseResult.error("消息不存在");
        }
        messageService.deleteById(id);
        return ResponseResult.success();
    }

    @PostMapping("/like/{id}")
    public ResponseResult<Integer> likeMessage(@PathVariable Long id) {
        Message message = messageService.findById(id);
        if (message == null) {
            return ResponseResult.error("消息不存在");
        }
        messageService.incrementLikes(id);
        Message updated = messageService.findById(id);
        return ResponseResult.success(updated.getLikes());
    }

    @GetMapping("/stats")
    public ResponseResult<Object> getMessageStats() {
        Long publishedCount = messageService.getPublishedMessageCount();
        HashMap<String, Object> stats = new HashMap<>();
        stats.put("publishedCount", publishedCount);
        return ResponseResult.success(stats);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseResult<List<MessageVO>> getAllMessages() {
        List<Message> messages = messageService.findAll();
        return ResponseResult.success(convertToVO(messages));
    }

    @GetMapping("/byTarget/{target}")
    public ResponseResult<List<MessageVO>> getMessagesByTarget(@PathVariable String target) {
        List<Message> messages = messageService.findPublishedByTarget(target);
        return ResponseResult.success(convertToVO(messages));
    }

    @GetMapping("/adminList/{target}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseResult<List<MessageVO>> getAdminMessagesByTarget(@PathVariable String target) {
        List<Message> messages = messageService.findAllByTarget(target);
        return ResponseResult.success(convertToVO(messages));
    }

    @PostMapping("/publish/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseResult<MessageVO> publishMessage(@PathVariable Long id) {
        Message message = messageService.publishMessage(id);
        if (message == null) {
            return ResponseResult.error("消息不存在");
        }
        return ResponseResult.success(convertMessageToVO(message));
    }

    private List<MessageVO> convertToVO(List<Message> messages) {
        List<MessageVO> voList = new ArrayList<>();
        for (Message message : messages) {
            voList.add(convertMessageToVO(message));
        }
        return voList;
    }

    private MessageVO convertMessageToVO(Message message) {
        return new MessageVO(
            message.getId(),
            message.getTitle(),
            message.getContent(),
            message.getType(),
            message.getTarget(),
            message.getPublisherId(),
            message.getPublisherName(),
            message.getViews(),
            message.getLikes(),
            message.getStatus(),
            message.getPublishTime(),
            message.getCreatedAt(),
            message.getUpdatedAt()
        );
    }
}

