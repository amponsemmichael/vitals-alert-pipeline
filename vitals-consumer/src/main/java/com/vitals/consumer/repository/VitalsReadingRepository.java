package com.vitals.consumer.repository;

import com.vitals.consumer.domain.VitalsReading;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface VitalsReadingRepository extends MongoRepository<VitalsReading, UUID> {
}
