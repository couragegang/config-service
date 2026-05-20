package com.couragegang.config.security;

import com.couragegang.config.api.dto.ErrorBody;
import com.couragegang.config.error.ConfigApiException;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.HttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import io.micronaut.http.filter.ServerFilterPhase;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

@Filter("/internal/**")
public final class InternalApiKeyFilter implements HttpServerFilter {

    public static final String HEADER = "X-Config-Internal-Key";

    private final String expectedKey;

    public InternalApiKeyFilter(@Value("${config-service.internal-api-key}") String expectedKey) {
        this.expectedKey = expectedKey;
    }

    @Override
    public int getOrder() {
        return ServerFilterPhase.SECURITY.order();
    }

    @Override
    public Publisher<MutableHttpResponse<?>> doFilter(HttpRequest<?> request, ServerFilterChain chain) {
        var provided = request.getHeaders().get(HEADER);
        if (provided == null || !provided.equals(expectedKey)) {
            var ex = new ConfigApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "invalid internal api key");
            return Mono.just(HttpResponse.status(ex.status()).body(ex.body()));
        }
        return chain.proceed(request);
    }
}
