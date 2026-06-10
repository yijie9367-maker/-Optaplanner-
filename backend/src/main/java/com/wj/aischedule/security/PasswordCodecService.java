package com.wj.aischedule.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class PasswordCodecService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    public boolean isEncoded(String password) {
        return StringUtils.hasText(password)
                && (password.startsWith("$2a$") || password.startsWith("$2b$") || password.startsWith("$2y$"));
    }

    public String encode(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    public String encodeIfNecessary(String password) {
        if (!StringUtils.hasText(password)) {
            return null;
        }
        return isEncoded(password) ? password : passwordEncoder.encode(password);
    }

    public boolean matches(String rawPassword, String storedPassword) {
        if (!StringUtils.hasText(rawPassword) || !StringUtils.hasText(storedPassword)) {
            return false;
        }
        if (isEncoded(storedPassword)) {
            return passwordEncoder.matches(rawPassword, storedPassword);
        }
        return storedPassword.equals(rawPassword);
    }
}
