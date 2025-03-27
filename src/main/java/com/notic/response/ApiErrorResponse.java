package com.notic.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Standard error format")
public record ApiErrorResponse(String code, String message, int status) {}
