package com.wj.aischedule.controller;

import com.wj.aischedule.security.AuthenticatedUser;
import com.wj.aischedule.service.AuthService;
import com.wj.aischedule.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseResult<Map<String, Object>> studentLogin(@RequestBody Map<String, String> loginRequest) {
        try {
            return ResponseResult.success(authService.loginStudent(loginRequest.get("name"), loginRequest.get("password")));
        } catch (RuntimeException ex) {
            return ResponseResult.error(ex.getMessage());
        }
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/auth/me")
    public ResponseResult<Map<String, Object>> currentUser(@AuthenticationPrincipal AuthenticatedUser currentUser) {
        try {
            return ResponseResult.success(authService.getCurrentUserProfile(currentUser));
        } catch (RuntimeException ex) {
            return ResponseResult.error(ex.getMessage());
        }
    }
}
