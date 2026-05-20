package com.couragegang.config.error;

import com.couragegang.config.api.dto.ErrorBody;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import jakarta.inject.Singleton;

@Singleton
@Produces
public final class ConfigExceptionHandler implements ExceptionHandler<ConfigApiException, HttpResponse<ErrorBody>> {

    @Override
    public HttpResponse<ErrorBody> handle(HttpRequest request, ConfigApiException exception) {
        return HttpResponse.status(exception.status()).body(exception.body());
    }
}
