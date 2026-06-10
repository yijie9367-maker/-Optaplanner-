package com.wj.aischedule.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * AI 助手服务 — 通过阿里云百炼 OpenAI 兼容接口调用智能体应用
 */
@Service
public class ChatbotService {

    @Value("${appflow.chat.app-id:}")
    private String appId;

    @Value("${appflow.chat.api-key:}")
    private String apiKey;

    private final RestTemplate restTemplate;

    public ChatbotService() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(120000);
        this.restTemplate = new RestTemplate(factory);
    }

    /**
     * 聊天代理 — 调用百炼 OpenAI 兼容接口
     */
    public Map<String, Object> chat(String message, String sessionId) {
        Map<String, Object> result = new HashMap<>();

        if (appId.isEmpty() || apiKey.isEmpty()) {
            // 未配置 → 模拟回复
            String mockSessionId = sessionId.isEmpty() ? UUID.randomUUID().toString().substring(0, 8) : sessionId;
            result.put("reply", getMockReply(message));
            result.put("sessionId", mockSessionId);
            return result;
        }

        try {
            // 构建请求体（百炼应用 responses 格式）
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("input", message);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // 尝试多个 API 路径
            String[] apiUrls = {
                "https://dashscope.aliyuncs.com/api/v2/apps/agent/" + appId + "/compatible-mode/v1/responses",
                "https://dashscope.aliyuncs.com/api/v2/apps/agent/" + appId + "/compatible-mode/v1/",
                "https://dashscope.aliyuncs.com/api/v1/apps/" + appId + "/completion"
            };

            String rawBody = null;
            for (String apiUrl : apiUrls) {
                try {
                    System.out.println("[Chatbot] 尝试: " + apiUrl);
                    ResponseEntity<String> resp = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, String.class);
                    if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                        rawBody = resp.getBody();
                        break;
                    }
                } catch (Exception ignored) {
                    System.out.println("[Chatbot] 路径失败: " + apiUrl);
                }
            }

            if (rawBody != null) {
                System.out.println("[Chatbot] 百炼返回: " + rawBody);
                String reply = extractReply(rawBody);
                result.put("reply", reply);
                result.put("sessionId", sessionId.isEmpty() ? UUID.randomUUID().toString().substring(0, 8) : sessionId);
            } else {
                String mockSessionId = sessionId.isEmpty() ? UUID.randomUUID().toString().substring(0, 8) : sessionId;
                result.put("reply", getMockReply(message));
                result.put("sessionId", mockSessionId);
            }
        } catch (Exception e) {
            System.err.println("[Chatbot] 百炼API失败，降级模拟: " + e.getMessage());
            String mockSessionId = sessionId.isEmpty() ? UUID.randomUUID().toString().substring(0, 8) : sessionId;
            result.put("reply", getMockReply(message));
            result.put("sessionId", mockSessionId);
        }

        return result;
    }

    /**
     * 解析 OpenAI 兼容格式: choices[0].message.content
     */
    private String extractReply(String rawBody) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            Map<String, Object> map = mapper.readValue(rawBody, Map.class);

            // OpenAI 格式: choices[0].message.content
            Object choices = map.get("choices");
            if (choices instanceof List && !((List<?>) choices).isEmpty()) {
                Object first = ((List<?>) choices).get(0);
                if (first instanceof Map) {
                    Object msg = ((Map<?, ?>) first).get("message");
                    if (msg instanceof Map) {
                        Object content = ((Map<?, ?>) msg).get("content");
                        if (content instanceof String && !((String) content).isEmpty()) {
                            return (String) content;
                        }
                    }
                }
            }

            // 百炼应用格式: output[0].content[0].text
            Object output = map.get("output");
            if (output instanceof List && !((List<?>) output).isEmpty()) {
                Object first = ((List<?>) output).get(0);
                if (first instanceof Map) {
                    Object content = ((Map<?, ?>) first).get("content");
                    if (content instanceof List && !((List<?>) content).isEmpty()) {
                        Object firstContent = ((List<?>) content).get(0);
                        if (firstContent instanceof Map) {
                            Object text = ((Map<?, ?>) firstContent).get("text");
                            if (text instanceof String && !((String) text).isEmpty()) {
                                return (String) text;
                            }
                        }
                    }
                }
            }

            // 备选: output 是 Map 时有 output.text
            if (output instanceof Map) {
                Object text = ((Map<?, ?>) output).get("text");
                if (text instanceof String && !((String) text).isEmpty()) {
                    return (String) text;
                }
            }

            // 兜底字段
            String[] keys = {"reply", "content", "text", "answer", "message", "data"};
            for (String key : keys) {
                Object val = map.get(key);
                if (val instanceof String && !((String) val).isEmpty()) {
                    return (String) val;
                }
            }

        } catch (Exception e) {
            if (rawBody.length() > 500) return rawBody.substring(0, 500);
            return rawBody;
        }
        return "（AI返回了未知格式，请联系管理员查看后台日志）";
    }

    private String getMockReply(String message) {
        if (message.contains("课表") || message.contains("课程")) {
            return "您好！您可以在首页「📅 课表」查看本周课程安排。点击左右箭头可以切换第1~20周。\n\n如果有课程冲突问题，请联系教务处。";
        }
        if (message.contains("考试")) {
            return "考试安排可以在课表页顶部点击「📝 考试」按钮查看。\n\n考试分为今日安排、未来安排和已结束三个区域，未来考试会显示倒计时天数。";
        }
        if (message.contains("论坛") || message.contains("帖子") || message.contains("发帖")) {
            return "论坛是同学们的交流社区💬\n- 浏览帖子无需登录\n- 登录后可以点赞、评论和发帖\n- 点击右下角「+」按钮即可发布新帖";
        }
        if (message.contains("登录") || message.contains("密码")) {
            return "请点击底部「👤 我的」Tab进行登录。\n\n输入您的姓名和密码即可。如果忘记密码，请联系辅导员或管理员重置。";
        }
        if (message.contains("你好") || message.contains("嗨") || message.contains("hello")) {
            return "你好！👋 我是AI智能排课助手。\n\n我可以帮您：\n📅 查看课表\n📝 查询考试\n💬 使用论坛\n👤 管理个人资料\n\n请问有什么可以帮您的？";
        }
        return "收到您的问题：「" + message + "」\n\n💡 提示：您可以问我关于课表、考试安排、论坛使用、登录等问题。\n\n⚠️ 当前为演示模式，AI回复功能需配置阿里云百炼后方可启用。";
    }

    public Map<String, Object> getAnonymousConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("appId", appId);
        return config;
    }
}
