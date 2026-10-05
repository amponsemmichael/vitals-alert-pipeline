package com.vitals.consumer.service;

import com.vitals.consumer.domain.Alert;
import com.vitals.consumer.domain.VitalsReading;
import com.vitals.consumer.messaging.AlertPublisher;
import com.vitals.consumer.messaging.VitalsAlertMessage;
import com.vitals.consumer.messaging.VitalsReadingMessage;
import com.vitals.consumer.repository.AlertRepository;
import com.vitals.consumer.repository.VitalsReadingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VitalsProcessingService {

    private final VitalsReadingRepository vitalsReadingRepository;
    private final AlertRepository alertRepository;
    private final AbnormalityEvaluator evaluator;
    private final AlertPublisher alertPublisher;

    /**
     * Processes one vitals reading end-to-end:
     *   1. Idempotency check — skip if readingId already exists in MongoDB
     *   2. Persist the reading document
     *   3. Evaluate abnormality rules
     *   4. Persist and publish any alerts
     *
     * The idempotency check satisfies SPEC.md section 7.2:
     * re-delivered Kafka messages are safely skipped at the DB level.
     *
     * @param message the reading consumed from vitals-raw
     */
    public void process(VitalsReadingMessage message) {

        // Step 1 — idempotency: if we already stored this readingId, skip silently
        if (vitalsReadingRepository.existsById(message.getReadingId())) {
            log.warn("Duplicate reading skipped readingId={}", message.getReadingId());
            return;
        }

        // Step 2 — evaluate rules before persisting so we can set the abnormal flag
        List<VitalsAlertMessage> alerts = evaluator.evaluate(message);
        boolean isAbnormal = !alerts.isEmpty();

        // Step 3 — persist the reading
        VitalsReading reading = VitalsReading.builder()
                .readingId(message.getReadingId())
                .patientId(message.getPatientId())
                .heartRate(message.getHeartRate())
                .systolicBp(message.getSystolicBp())
                .diastolicBp(message.getDiastolicBp())
                .oxygenSaturation(message.getOxygenSaturation())
                .temperatureCelsius(message.getTemperatureCelsius())
                .timestamp(message.getTimestamp())
                .submittedAt(message.getSubmittedAt())
                .processedAt(Instant.now())
                .abnormal(isAbnormal)
                .build();

        vitalsReadingRepository.save(reading);
        log.info("Persisted reading readingId={} patientId={} abnormal={}",
                message.getReadingId(), message.getPatientId(), isAbnormal);

        // Step 4 — persist alerts to MongoDB and publish each to vitals-alerts
        if (isAbnormal) {
            alerts.forEach(alertMessage -> {
                alertRepository.save(toAlertDocument(alertMessage));
                alertPublisher.publish(alertMessage);
                log.info("Alert raised readingId={} alertType={} severity={}",
                        alertMessage.getReadingId(), alertMessage.getAlertType(), alertMessage.getSeverity());
            });
        }
    }

    private Alert toAlertDocument(VitalsAlertMessage a) {
        return Alert.builder()
                .alertId(a.getAlertId())
                .readingId(a.getReadingId())
                .patientId(a.getPatientId())
                .alertType(a.getAlertType())
                .value(a.getValue())
                .threshold(a.getThreshold())
                .severity(a.getSeverity())
                .triggeredAt(a.getTriggeredAt())
                .build();
    }
}
