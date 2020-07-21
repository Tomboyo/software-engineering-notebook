package com.github.tomboyo.faultyservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.vavr.control.Try;

@Component
public class Requester {
    private static final Logger logger =
        LoggerFactory.getLogger(Requester.class);

    private final FaultyService client;

    @Autowired
    public Requester(
        FaultyService client,
        CircuitBreakerRegistry registry
    ) {
        this.client = client;

        configureEventHandlers(registry.circuitBreaker("faultyservice-ping"));
    }

    @Scheduled(fixedRate = 1000)
    public void makeCall() {
        Try.ofSupplier(() -> TimedCall.time(client::ping))
            .onSuccess(x -> {
                logger.info("Call OK in {} ms", x.elapsed().toMillis());
            })
            .onFailure(Exception.class, e -> {
                logger.error("Call failed: {}", e.getMessage());
            });
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
