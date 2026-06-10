package com.wj.aischedule.controller;

import com.wj.aischedule.common.Result;
import com.wj.aischedule.entity.ConstraintConfig;
import com.wj.aischedule.service.ConstraintConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/constraint-config")
public class ConstraintConfigController {

    @Autowired
    private ConstraintConfigService constraintConfigService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public Result<List<ConstraintConfig>> getAll() {
        return Result.success(constraintConfigService.getAll());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{key}")
    public Result<ConstraintConfig> update(@PathVariable String key,
                                           @RequestBody Map<String, Object> body) {
        Boolean enabled = body.containsKey("enabled") ? (Boolean) body.get("enabled") : null;
        Integer weight = body.containsKey("weight") ? ((Number) body.get("weight")).intValue() : null;
        return Result.success(constraintConfigService.update(key, enabled, weight));
    }
}
