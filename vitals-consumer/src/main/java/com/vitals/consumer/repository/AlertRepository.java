package com.vitals.consumer.repository;

import com.vitals.consumer.domain.Alert;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface AlertRepository extends MongoRepository<Alert, UUID> {
}
