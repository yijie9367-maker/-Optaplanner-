package com.wj.aischedule.service;

import com.wj.aischedule.entity.ConstraintConfig;
import com.wj.aischedule.repository.ConstraintConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ConstraintConfigService {

    @Autowired
    private ConstraintConfigRepository repository;

    private final Map<String, ConstraintConfig> cache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        // key, name, enabled, weight, description, category
        List<Object[]> defaults = Arrays.asList(
            new Object[]{"spreadCourseInstances", "分散同课程排课",   true,  20,   "同一课程同一天避免重复排两节",    "SOFT"},
            new Object[]{"preferWeekRange",        "限制周次范围",     true,  3,    "课程不超出设定的最大周次",        "SOFT"},
            new Object[]{"avoidEveningClasses",    "避免晚间排课",     true,  2,    "19点后的课程将被惩罚",           "SOFT"},
            new Object[]{"teacherTimePreference",  "教师晚间偏好",     true,  1,    "教师尽量不上19点后的晚课",        "SOFT"},
            new Object[]{"roomTypePreference",     "教室类型匹配",     true,  1,    "实验课优先安排在实验室",          "SOFT"},
            new Object[]{"timeSlotPreference",     "实验课时间偏好",   true,  1,    "实验课尽量排在下午时段",          "SOFT"},
            new Object[]{"continuousScheduling",   "课程连续性偏好",   true,  1,    "同课程同天尽量安排在连续时间段",  "SOFT"}
        );

        for (Object[] d : defaults) {
            String key = (String) d[0];
            if (!repository.findByConstraintKey(key).isPresent()) {
                ConstraintConfig cfg = new ConstraintConfig();
                cfg.setConstraintKey(key);
                cfg.setConstraintName((String) d[1]);
                cfg.setEnabled((Boolean) d[2]);
                cfg.setWeight((Integer) d[3]);
                cfg.setDescription((String) d[4]);
                cfg.setCategory((String) d[5]);
                repository.save(cfg);
            }
        }
        refreshCache();
    }

    public void refreshCache() {
        cache.clear();
        repository.findAll().forEach(c -> cache.put(c.getConstraintKey(), c));
    }

    public List<ConstraintConfig> getAll() {
        return repository.findAll();
    }

    public ConstraintConfig update(String key, Boolean enabled, Integer weight) {
        ConstraintConfig cfg = repository.findByConstraintKey(key)
                .orElseThrow(() -> new RuntimeException("约束配置不存在: " + key));
        if (enabled != null) cfg.setEnabled(enabled);
        if (weight != null) {
            // SOLVER 参数范围更大，SOFT 约束限制 1-100
            if ("SOLVER".equals(cfg.getCategory())) {
                cfg.setWeight(Math.max(1, Math.min(20000, weight)));
            } else {
                cfg.setWeight(Math.max(1, Math.min(100, weight)));
            }
        }
        cfg = repository.save(cfg);
        cache.put(key, cfg);
        return cfg;
    }

    /** 供 ConstraintProvider 调用：软约束权重，disabled 时返回 0 */
    public int getWeight(String key, int defaultVal) {
        ConstraintConfig cfg = cache.get(key);
        if (cfg == null) return defaultVal;
        return Boolean.TRUE.equals(cfg.getEnabled()) ? cfg.getWeight() : 0;
    }

    /** 获取 SOLVER 类参数 */
    public int getSolverParam(String key, int defaultVal) {
        ConstraintConfig cfg = cache.get(key);
        if (cfg == null || !Boolean.TRUE.equals(cfg.getEnabled())) return defaultVal;
        return cfg.getWeight();
    }
}
