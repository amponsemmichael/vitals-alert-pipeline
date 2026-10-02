package com.vitals.consumer.domain;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@Document(collection = "alerts")
public class Alert {

    @Id
    private UUID alertId;

    private UUID readingId;
    private UUID patientId;
    private String alertType;
    private BigDecimal value;
    private BigDecimal threshold;
    private String severity;
    private Instant triggeredAt;
}
