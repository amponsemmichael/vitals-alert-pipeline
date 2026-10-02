package com.vitals.api.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class VitalsProducer {

    private final KafkaTemplate<String, VitalsReadingMessage> kafkaTemplate;

    @Value("${kafka.topics.vitals-raw}")
    private String vitalsRawTopic;

    /**
     * Publishes a vitals reading to the vitals-raw topic.
     *
     * Partition key is patientId (as string) so all readings for the same patient
     * land on the same partition, preserving order per patient.
     * See SPEC.md section 4.1 for the partitioning rationale.
     *
     * @param message the reading to publish
     * @return a future that completes when the broker acknowledges the write
     */
    public CompletableFuture<SendResult<String, VitalsReadingMessage>> publish(VitalsReadingMessage message) {
        String partitionKey = message.getPatientId().toString();

        CompletableFuture<SendResult<String, VitalsReadingMessage>> future =
                kafkaTemplate.send(vitalsRawTopic, partitionKey, message);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish reading readingId={} patientId={} error={}",
                        message.getReadingId(), message.getPatientId(), ex.getMessage());
            } else {
                log.info("Published reading readingId={} patientId={} partition={} offset={}",
                        message.getReadingId(),
                        message.getPatientId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });

        return future;
    }
}
