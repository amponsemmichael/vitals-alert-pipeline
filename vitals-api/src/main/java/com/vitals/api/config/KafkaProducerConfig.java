package com.vitals.api.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaProducerConfig {

    @Value("${kafka.topics.vitals-raw}")
    private String vitalsRawTopic;

    /**
     * Declares the topic so Spring Kafka auto-creates it on startup if it doesn't exist.
     * Partition count matches the consumer replica count defined in SPEC.md section 4.1.
     * Replication factor is 1 for local dev; overridden to 3 in the HA compose profile.
     */
    @Bean
    public NewTopic vitalsRawTopic() {
        return TopicBuilder.name(vitalsRawTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
