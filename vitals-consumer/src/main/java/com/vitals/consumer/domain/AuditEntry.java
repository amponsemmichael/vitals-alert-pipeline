package com.vitals.consumer.domain;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@Document(collection = "audit")
public class AuditEntry {

    @Id
    private UUID id;

    private UUID readingId;
    private UUID patientId;
    private String submittedBy;
    private Instant submittedAt;
}
