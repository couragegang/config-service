package com.couragegang.config.error;

import static org.assertj.core.api.Assertions.assertThat;

import com.couragegang.config.api.dto.ErrorBody;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import org.junit.jupiter.api.Test;

class ConfigExceptionHandlerTest {

    @Test
    void handleMapsException() {
        var ex = new ConfigApiException(HttpStatus.CONFLICT, "CONFLICT", "exists");
        var response = new ConfigExceptionHandler().handle(HttpRequest.GET("/"), ex);

        assertThat(response.getStatus().getCode()).isEqualTo(HttpStatus.CONFLICT.getCode());
        assertThat(response.body()).isEqualTo(ErrorBody.of("CONFLICT", "exists"));
    }
}
