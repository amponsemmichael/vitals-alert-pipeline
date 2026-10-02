package com.vitals.consumer.listener;

import com.vitals.consumer.messaging.VitalsReadingMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class VitalsReadingListener {

    @KafkaListener(
            topics = "${kafka.topics.vitals-raw}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void onMessage(VitalsReadingMessage message, Acknowledgment ack) {
        // TODO Session 4: persist to MongoDB, evaluate rules, publish alerts
        log.info("Received reading readingId={} patientId={}", message.getReadingId(), message.getPatientId());
        ack.acknowledge();
    }
}
