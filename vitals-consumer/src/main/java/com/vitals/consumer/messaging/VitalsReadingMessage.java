package com.vitals.consumer.messaging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Mirrors VitalsReadingMessage from vitals-api.
 * Kept as a separate class — modules are independently deployable
 * and should not share a runtime dependency on each other.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VitalsReadingMessage {

    private UUID readingId;
    private UUID patientId;
    private Integer heartRate;
    private Integer systolicBp;
    private Integer diastolicBp;
    private BigDecimal oxygenSaturation;
    private BigDecimal temperatureCelsius;
    private Instant timestamp;
    private Instant submittedAt;
}
