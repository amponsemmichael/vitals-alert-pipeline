package com.vitals.api.dto;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Value
@Builder
public class VitalsReadingRequest {

    @NotNull(message = "patientId is required")
    UUID patientId;

    @NotNull(message = "heartRate is required")
    @Min(value = 20, message = "heartRate must be at least 20")
    @Max(value = 300, message = "heartRate must be at most 300")
    Integer heartRate;

    @NotNull(message = "systolicBp is required")
    @Min(value = 50, message = "systolicBp must be at least 50")
    @Max(value = 300, message = "systolicBp must be at most 300")
    Integer systolicBp;

    @NotNull(message = "diastolicBp is required")
    @Min(value = 30, message = "diastolicBp must be at least 30")
    @Max(value = 200, message = "diastolicBp must be at most 200")
    Integer diastolicBp;

    @NotNull(message = "oxygenSaturation is required")
    @DecimalMin(value = "50.0", message = "oxygenSaturation must be at least 50.0")
    @DecimalMax(value = "100.0", message = "oxygenSaturation must be at most 100.0")
    BigDecimal oxygenSaturation;

    @NotNull(message = "temperatureCelsius is required")
    @DecimalMin(value = "25.0", message = "temperatureCelsius must be at least 25.0")
    @DecimalMax(value = "45.0", message = "temperatureCelsius must be at most 45.0")
    BigDecimal temperatureCelsius;

    @NotNull(message = "timestamp is required")
    @PastOrPresent(message = "timestamp must not be in the future")
    Instant timestamp;
}
