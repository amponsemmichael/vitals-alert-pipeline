package com.vitals.api.messaging;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * The message published to the vitals-raw Kafka topic.
 * Matches the schema defined in SPEC.md section 4.2.
 */
@Value
@Builder
public class VitalsReadingMessage {

    UUID readingId;
    UUID patientId;
    Integer heartRate;
    Integer systolicBp;
    Integer diastolicBp;
    BigDecimal oxygenSaturation;
    BigDecimal temperatureCelsius;
    Instant timestamp;
    Instant submittedAt;
}
