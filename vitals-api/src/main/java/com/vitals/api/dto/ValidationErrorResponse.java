package com.vitals.api.dto;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class ValidationErrorResponse {

    String status;
    List<FieldError> errors;

    @Value
    @Builder
    public static class FieldError {
        String field;
        String message;
    }
}
