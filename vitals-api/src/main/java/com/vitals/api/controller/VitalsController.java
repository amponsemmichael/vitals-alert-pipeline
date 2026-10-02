package com.vitals.api.controller;

import com.vitals.api.dto.VitalsReadingRequest;
import com.vitals.api.dto.VitalsReadingResponse;
import com.vitals.api.messaging.VitalsProducer;
import com.vitals.api.messaging.VitalsReadingMessage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/vitals")
@RequiredArgsConstructor
public class VitalsController {

    private final VitalsProducer producer;

    @PostMapping
    public ResponseEntity<VitalsReadingResponse> submitReading(
            @Valid @RequestBody VitalsReadingRequest request,
            @AuthenticationPrincipal UserDetails user) {

        UUID readingId = UUID.randomUUID();

        VitalsReadingMessage message = VitalsReadingMessage.builder()
                .readingId(readingId)
                .patientId(request.getPatientId())
                .heartRate(request.getHeartRate())
                .systolicBp(request.getSystolicBp())
                .diastolicBp(request.getDiastolicBp())
                .oxygenSaturation(request.getOxygenSaturation())
                .temperatureCelsius(request.getTemperatureCelsius())
                .timestamp(request.getTimestamp())
                .submittedAt(Instant.now())
                .build();

        // Log the submission for audit — submittedBy is the authenticated username.
        // patientId is a UUID only; no name or ID number is logged.
        log.info("Vitals submitted readingId={} patientId={} submittedBy={}",
                readingId, request.getPatientId(), user.getUsername());

        producer.publish(message);

        return ResponseEntity.accepted()
                .body(VitalsReadingResponse.builder()
                        .readingId(readingId)
                        .status("ACCEPTED")
                        .build());
    }
}
