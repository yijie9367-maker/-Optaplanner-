package com.wj.aischedule.controller;

import com.wj.aischedule.entity.SensitiveWord;
import com.wj.aischedule.service.SensitiveWordService;
import com.wj.aischedule.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sensitive-words")
public class SensitiveWordController {

    @Autowired
    private SensitiveWordService service;

    /** 获取全部违禁词 */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseResult<List<SensitiveWord>> list() {
        return ResponseResult.success(service.listAll());
    }

    /** 添加违禁词（支持单个或批量：body 传 {"words":["词1","词2"]}） */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseResult<List<SensitiveWord>> add(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<String> words = (List<String>) body.get("words");
        if (words == null || words.isEmpty()) {
            return ResponseResult.error("请提供违禁词");
        }
        return ResponseResult.success(service.addWords(words));
    }

    /** 删除违禁词 */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        service.deleteWord(id);
        return ResponseResult.success(null);
    }
}
