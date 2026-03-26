package com.vectortech.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class BackendApplicationTests {

    @MockBean
    private RedisTemplate<String, Long> redisTemplate;

    @Test
    void contextLoads() {
        // Verifies the Spring application context starts without errors.
    }
}
