package com.vitals.consumer.repository;

import com.vitals.consumer.domain.AuditEntry;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface AuditRepository extends MongoRepository<AuditEntry, UUID> {
}
