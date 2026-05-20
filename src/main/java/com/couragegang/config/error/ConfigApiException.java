package com.couragegang.config.error;

import com.couragegang.config.api.dto.ErrorBody;
import io.micronaut.http.HttpStatus;

public final class ConfigApiException extends RuntimeException {

    private final HttpStatus status;
    private final ErrorBody body;

    public ConfigApiException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.body = ErrorBody.of(code, message);
    }

    public HttpStatus status() {
        return status;
    }

    public ErrorBody body() {
        return body;
    }
}
