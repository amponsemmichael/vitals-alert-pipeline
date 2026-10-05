package com.vitals.consumer.listener;

import com.vitals.consumer.messaging.VitalsReadingMessage;
import com.vitals.consumer.service.VitalsProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class VitalsReadingListener {

    private final VitalsProcessingService processingService;

    /**
     * Consumes messages from vitals-raw.
     *
     * Manual acknowledgment (MANUAL_IMMEDIATE) means the offset is only committed
     * after processingService.process() returns successfully.
     * If an exception is thrown, the DefaultErrorHandler retries with exponential
     * backoff before routing to the DLT. See KafkaConsumerConfig for retry wiring.
     *
     * Delivery guarantee: at-least-once. Idempotency is enforced in
     * VitalsProcessingService via the readingId unique index. See SPEC.md section 7.
     */
    @KafkaListener(
            topics = "${kafka.topics.vitals-raw}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void onMessage(VitalsReadingMessage message, Acknowledgment ack) {
        log.info("Consuming readingId={} patientId={}",
                message.getReadingId(), message.getPatientId());

        processingService.process(message);
        ack.acknowledge();
    }
}
