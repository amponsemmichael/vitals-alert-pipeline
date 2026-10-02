package com.vitals.consumer.domain;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@Document(collection = "vitals")
public class VitalsReading {

    @Id
    private UUID readingId;

    @Indexed
    private UUID patientId;

    private Integer heartRate;
    private Integer systolicBp;
    private Integer diastolicBp;
    private BigDecimal oxygenSaturation;
    private BigDecimal temperatureCelsius;
    private Instant timestamp;
    private Instant submittedAt;

    @Indexed(expireAfter = "90d")
    private Instant processedAt;

    private boolean abnormal;
}
