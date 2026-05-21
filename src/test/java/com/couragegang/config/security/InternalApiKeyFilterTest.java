package com.couragegang.config.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.filter.ServerFilterChain;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

class InternalApiKeyFilterTest {

    @Test
    void rejectsWrongKey() {
        var filter = new InternalApiKeyFilter("expected");
        var chain = mock(ServerFilterChain.class);

        var response =
                Mono.from(filter.doFilter(HttpRequest.GET("/internal/x").header("X-Config-Internal-Key", "bad"), chain))
                        .block();

        assertThat(response.getStatus().getCode()).isEqualTo(HttpStatus.UNAUTHORIZED.getCode());
    }

    @Test
    void allowsValidKey() {
        var filter = new InternalApiKeyFilter("expected");
        var chain = mock(ServerFilterChain.class);
        when(chain.proceed(org.mockito.ArgumentMatchers.any())).thenReturn(Mono.empty());
        var request = HttpRequest.GET("/internal/x").header("X-Config-Internal-Key", "expected");

        Mono.from(filter.doFilter(request, chain)).block();

        verify(chain).proceed(request);
    }
}
