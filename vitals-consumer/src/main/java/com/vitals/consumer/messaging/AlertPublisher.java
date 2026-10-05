package com.vitals.consumer.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlertPublisher {

    private final KafkaTemplate<String, VitalsAlertMessage> kafkaTemplate;

    @Value("${kafka.topics.vitals-alerts}")
    private String vitalsAlertsTopic;

    /**
     * Publishes an alert event to the vitals-alerts topic.
     * Partition key is patientId — consistent with the producer in vitals-api.
     */
    public void publish(VitalsAlertMessage alert) {
        String partitionKey = alert.getPatientId().toString();

        kafkaTemplate.send(vitalsAlertsTopic, partitionKey, alert)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish alert alertId={} readingId={} error={}",
                                alert.getAlertId(), alert.getReadingId(), ex.getMessage());
                    } else {
                        log.info("Published alert alertId={} readingId={} alertType={} severity={}",
                                alert.getAlertId(), alert.getReadingId(),
                                alert.getAlertType(), alert.getSeverity());
                    }
                });
    }
}
