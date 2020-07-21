package com.github.tomboyo.faultyservice;

import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.vavr.control.Try;

@Component
public class Requester {
    private static final Logger logger =
        LoggerFactory.getLogger(Requester.class);

    private final Environment env;
    private final FaultyService client;
    private final Supplier<TimedCall<String>> call;

    @Autowired
    public Requester(
        Environment env,
        FaultyService client,
        CircuitBreakerRegistry registry
    ) {
        this.env = env;
        this.client = client;
        this.call = getCall(this.env, this.client);

        configureEventHandlers(registry.circuitBreaker("faultyservice-ping"));
    }

    @Scheduled(fixedRate = 1000)
    public void makeCall() {
        Try.ofSupplier(call)
            .onSuccess(x -> {
                logger.info("Call OK in {} ms", x.elapsed().toMillis());
            })
            .onFailure(Exception.class, e -> {
                logger.error("Call failed: {}", e.getMessage());
            });
    }

    private static Supplier<TimedCall<String>> getCall(
        Environment env,
        FaultyService client
    ) {
        String mode = env.getRequiredProperty("mode");

        switch (mode) {
            case "circuitbreaker":
                logger.info("Starting 'circuitbreaker' demo.");
                return () -> TimedCall.time(client::ping);
            case "ratelimiter":
                logger.info("Starting 'ratelimiter' demo.");
                return () -> TimedCall.time(client::ratelimitedPing);
            default:
                throw new IllegalStateException(
                    "mode must be one of [ratelimiter,circuitbreaker]");
        }
    }

    private static void configureEventHandlers(
        CircuitBreaker faultyservice
    ) {
        faultyservice.getEventPublisher()
            .onStateTransition(event -> {
                var transition = event.getStateTransition();
                logger.info(
                    "Breaker {} transition: {} -> {}",
                    event.getCircuitBreakerName(),
                    transition.getFromState(),
                    transition.getToState());
            });
    }
}
