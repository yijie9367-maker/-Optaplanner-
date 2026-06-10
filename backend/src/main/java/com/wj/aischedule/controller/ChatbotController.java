package com.wj.aischedule.controller;

import com.wj.aischedule.service.ChatbotService;
import com.wj.aischedule.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/chatbot")
public class ChatbotController {

    @Autowired
    private ChatbotService chatbotService;

    @PostMapping("/chat")
    public ResponseResult<Map<String, Object>> chat(@RequestBody Map<String, String> body) {
        String message = body.get("message");
        String sessionId = body.getOrDefault("sessionId", "");
        if (message == null || message.trim().isEmpty()) {
            return ResponseResult.error("消息不能为空");
        }
        try {
            return ResponseResult.success(chatbotService.chat(message, sessionId));
        } catch (Exception e) {
            return ResponseResult.error("AI 回复失败: " + e.getMessage());
        }
    }

    @GetMapping("/anonymous-config")
    public ResponseResult<Map<String, Object>> getAnonymousConfig() {
        return ResponseResult.success(chatbotService.getAnonymousConfig());
    }
}
