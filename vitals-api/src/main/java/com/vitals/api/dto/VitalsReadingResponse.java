package com.vitals.api.dto;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
public class VitalsReadingResponse {

    UUID readingId;
    String status;
}
