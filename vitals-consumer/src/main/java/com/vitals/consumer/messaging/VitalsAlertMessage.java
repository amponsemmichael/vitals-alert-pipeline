package com.vitals.consumer.messaging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * The message published to the vitals-alerts Kafka topic.
 * Matches the schema defined in SPEC.md section 4.3.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VitalsAlertMessage {

    private UUID alertId;
    private UUID readingId;
    private UUID patientId;
    private String alertType;
    private BigDecimal value;
    private BigDecimal threshold;
    private String severity;
    private Instant triggeredAt;
}
