package com.vitals.consumer.config;

import com.vitals.consumer.messaging.VitalsReadingMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;

@Slf4j
@Configuration
public class KafkaConsumerConfig {

    @Value("${kafka.topics.vitals-alerts}")
    private String vitalsAlertsTopic;

    @Value("${kafka.topics.vitals-dlt}")
    private String vitalsDltTopic;

    @Value("${retry.backoff.initial-interval-ms:500}")
    private long initialIntervalMs;

    @Value("${retry.backoff.multiplier:2.0}")
    private double multiplier;

    @Value("${retry.backoff.max-attempts:4}")
    private int maxAttempts;

    /**
     * Declares the vitals-alerts topic.
     * Consumer produces here for every abnormal reading.
     */
    @Bean
    public NewTopic vitalsAlertsTopic() {
        return TopicBuilder.name(vitalsAlertsTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    /**
     * Declares the dead-letter topic.
     * Messages routed here after exhausting all retry attempts.
     */
    @Bean
    public NewTopic vitalsDltTopic() {
        return TopicBuilder.name(vitalsDltTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    /**
     * Configures exponential backoff retry with a dead-letter recoverer.
     *
     * Retry schedule (from SPEC.md section 7.1):
     *   attempt 1 — immediate
     *   attempt 2 — 500 ms
     *   attempt 3 — 1 000 ms
     *   attempt 4 — 2 000 ms
     *   attempt 5+ — route to vitals-dlt
     *
     * DeadLetterPublishingRecoverer routes failed messages to {topic}.DLT by default,
     * but we explicitly name our DLT topic for clarity and operational control.
     */
    @Bean
    public DefaultErrorHandler errorHandler(
            KafkaTemplate<String, VitalsReadingMessage> kafkaTemplate) {

        var recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
                (record, ex) -> {
                    log.error("Routing to DLT topic={} key={} error={}",
                            vitalsDltTopic, record.key(), ex.getMessage());
                    return new org.apache.kafka.common.TopicPartition(vitalsDltTopic, 0);
                });

        var backoff = new ExponentialBackOffWithMaxRetries(maxAttempts);
        backoff.setInitialInterval(initialIntervalMs);
        backoff.setMultiplier(multiplier);

        return new DefaultErrorHandler(recoverer, backoff);
    }
}
