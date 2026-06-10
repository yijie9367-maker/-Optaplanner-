package com.wj.aischedule.service;

import com.wj.aischedule.entity.SensitiveWord;
import com.wj.aischedule.repository.SensitiveWordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SensitiveWordService {

    @Autowired
    private SensitiveWordRepository repository;

    /** 获取全部违禁词 */
    public List<SensitiveWord> listAll() {
        return repository.findAll();
    }

    /** 批量添加（自动去重） */
    public List<SensitiveWord> addWords(List<String> words) {
        List<SensitiveWord> saved = new ArrayList<>();
        for (String w : words) {
            String trimmed = w.trim();
            if (trimmed.isEmpty()) continue;
            if (repository.existsByWord(trimmed)) continue;
            saved.add(repository.save(new SensitiveWord(trimmed)));
        }
        return saved;
    }

    /** 删除 */
    public void deleteWord(Long id) {
        repository.deleteById(id);
    }

    /** 检查文本是否含违禁词，返回命中的词列表 */
    public List<String> check(String text) {
        if (text == null || text.isEmpty()) return List.of();
        String lower = text.toLowerCase();
        return repository.findAll().stream()
                .map(SensitiveWord::getWord)
                .filter(w -> lower.contains(w.toLowerCase()))
                .collect(Collectors.toList());
    }
}
