package com.wj.aischedule.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class PasswordCodecServiceTests {

    @Autowired
    private PasswordCodecService passwordCodecService;

    @Test
    void shouldMatchPlaintextAndEncodedPassword() {
        String rawPassword = "123456";
        String encodedPassword = passwordCodecService.encode(rawPassword);

        assertTrue(passwordCodecService.matches(rawPassword, rawPassword));
        assertTrue(passwordCodecService.matches(rawPassword, encodedPassword));
        assertTrue(passwordCodecService.isEncoded(encodedPassword));
        assertFalse(passwordCodecService.isEncoded(rawPassword));
        assertNotEquals(rawPassword, encodedPassword);
    }
}
