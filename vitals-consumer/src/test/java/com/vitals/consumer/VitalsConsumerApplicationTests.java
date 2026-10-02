package com.vitals.consumer;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=localhost:9999",
        "spring.data.mongodb.uri=mongodb://localhost:27017/vitals-test"
})
class VitalsConsumerApplicationTests {

    @Test
    void contextLoads() {
        // Verifies the Spring context assembles without errors.
        // Kafka and MongoDB are pointed at non-existent addresses so no real connections are attempted.
    }
}
