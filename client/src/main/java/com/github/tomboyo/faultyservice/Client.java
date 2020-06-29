package com.github.tomboyo.faultyservice;

import static io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType.COUNT_BASED;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.vavr.control.Try;

public class Client {
    private static final Logger logger =
        LoggerFactory.getLogger(Client.class);
    private static final Duration callDelay = Duration.ofSeconds(1);

    public static void main(String[] args) throws Exception {
        var config = CircuitBreakerConfig.custom()
            .slowCallDurationThreshold(Duration.ofMillis(1500))
            .permittedNumberOfCallsInHalfOpenState(3)
            .slidingWindow(1, 1, COUNT_BASED)
            .waitDurationInOpenState(Duration.ofSeconds(5))
            .build();
        var registry = CircuitBreakerRegistry.of(config);
        var breaker = registry.circuitBreaker("breaker-1");

        breaker.getEventPublisher()
            .onSuccess(event -> logger.info("Call OK in {} ms",
                event.getElapsedDuration().toMillis()))
            .onError(event -> logger.info(
                "Call Error: {}",
                event.getThrowable().getClass().getSimpleName()))
            .onCallNotPermitted(event -> logger.info("Call not permitted (open)"))
            .onStateTransition(event -> {
                var transition = event.getStateTransition();
                logger.info("Breaker {} transition: {} -> {}",
                    event.getCircuitBreakerName(),
                    transition.getFromState(),
                    transition.getToState());
            });
        
        var call = breaker.decorateCheckedSupplier(Client::getOk);
        while (true) {
            Try.of(call);
            Thread.sleep(callDelay.toMillis());
        }
    }

    private static String getOk() throws Exception {
        var response = HttpClient.newHttpClient().send(
            HttpRequest.newBuilder()
                .GET()
                .uri(new URI("http://localhost:8080/"))
                .build(),
            HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() != 200) {
            throw new Exception(
                "Unexpected status code=" + response.statusCode());
        }

        return response.body();
    }
}