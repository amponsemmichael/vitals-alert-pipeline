package com.vitals.api.controller;

import com.vitals.api.dto.VitalsReadingRequest;
import com.vitals.api.dto.VitalsReadingResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/vitals")
public class VitalsController {

    @PostMapping
    public ResponseEntity<VitalsReadingResponse> submitReading(
            @Valid @RequestBody VitalsReadingRequest request) {

        // TODO Session 3: publish to Kafka
        UUID readingId = UUID.randomUUID();
        log.info("Received vitals reading readingId={} patientId={}", readingId, request.getPatientId());

        return ResponseEntity.accepted()
                .body(VitalsReadingResponse.builder()
                        .readingId(readingId)
                        .status("ACCEPTED")
                        .build());
    }
}
