package com.vitals.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=localhost:9999",
        "spring.security.user.password=test-password",
        "API_PASSWORD=test-password"
})
class VitalsApiApplicationTests {

    @Test
    void contextLoads() {
        // Verifies the Spring context assembles without errors.
        // Kafka is pointed at a non-existent broker so no real connection is attempted.
    }
}
