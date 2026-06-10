package com.wj.aischedule.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class JwtTokenServiceTests {

    @Autowired
    private JwtTokenService jwtTokenService;

    @Test
    void shouldGenerateAndParseToken() {
        AuthenticatedUser user = new AuthenticatedUser(1L, "student-1", "王杰", AppRole.STUDENT, 9L);

        String token = jwtTokenService.generateToken(user);
        AuthenticatedUser parsed = jwtTokenService.parseToken(token);

        assertNotNull(token);
        assertEquals(user.getUserId(), parsed.getUserId());
        assertEquals(user.getUsername(), parsed.getUsername());
        assertEquals(user.getDisplayName(), parsed.getDisplayName());
        assertEquals(user.getRole(), parsed.getRole());
        assertEquals(user.getClassGroupId(), parsed.getClassGroupId());
    }
}
