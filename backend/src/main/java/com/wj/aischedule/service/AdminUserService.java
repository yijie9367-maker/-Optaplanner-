package com.wj.aischedule.service;

import com.wj.aischedule.entity.AdminUser;
import com.wj.aischedule.repository.AdminUserRepository;
import com.wj.aischedule.security.PasswordCodecService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class AdminUserService {

    private static final String DEFAULT_ADMIN_PASSWORD = "123456";

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private PasswordCodecService passwordCodecService;

    public AdminUser authenticate(String username, String password) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            throw new RuntimeException("用户名或密码不能为空");
        }

        AdminUser admin = adminUserRepository.findByUsername(username);
        if (admin == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        String storedPassword = admin.getPassword();
        String comparablePassword = StringUtils.hasText(storedPassword) ? storedPassword : DEFAULT_ADMIN_PASSWORD;
        if (!passwordCodecService.matches(password, comparablePassword)) {
            throw new RuntimeException("用户名或密码错误");
        }

        if (!passwordCodecService.isEncoded(storedPassword)) {
            admin.setPassword(passwordCodecService.encode(password));
            admin = adminUserRepository.save(admin);
        }

        return admin;
    }

    // 查询全部
    public List<AdminUser> findAll() {
        return adminUserRepository.findAll();
    }

    // 根据 ID 查询
    public AdminUser findById(Long id) {
        return adminUserRepository.findById(id).orElse(null);
    }

    // 新增 / 更新
    public AdminUser save(AdminUser adminUser) {
        AdminUser existing = adminUser.getId() == null
                ? null
                : adminUserRepository.findById(adminUser.getId()).orElse(null);

        if (!StringUtils.hasText(adminUser.getPassword())) {
            if (existing != null && StringUtils.hasText(existing.getPassword())) {
                adminUser.setPassword(existing.getPassword());
            } else {
                adminUser.setPassword(passwordCodecService.encode(DEFAULT_ADMIN_PASSWORD));
            }
        } else {
            adminUser.setPassword(passwordCodecService.encodeIfNecessary(adminUser.getPassword()));
        }

        return adminUserRepository.save(adminUser);
    }

    // 删除
    public void deleteById(Long id) {
        adminUserRepository.deleteById(id);
    }
}
