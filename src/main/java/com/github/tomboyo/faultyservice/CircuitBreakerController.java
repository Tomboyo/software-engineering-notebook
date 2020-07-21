package com.github.tomboyo.faultyservice;

import static java.lang.String.format;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreaker.State;

@RestController
public class CircuitBreakerController {
    private static final Logger logger =
        LoggerFactory.getLogger(CircuitBreakerController.class);

    private final CircuitBreaker pingBreaker;

    @Autowired
    public CircuitBreakerController(
        CircuitBreakerRegistry registry
    ) {
        this.pingBreaker = registry.circuitBreaker("faultyservice-ping");
    }

    @PostMapping("/circuitbreakers/faultyservice-ping")
    public void forceBreakerState(
        @RequestBody BreakerStateUpdate update
    ) {
        State state = update.state();
        forceTransition(state, pingBreaker);
        logger.info("Forced FaultyService::ping breaker state to {}", state);
    }

    private static void forceTransition(
        State state,
        CircuitBreaker breaker
    ) {
        switch (state) {
            case CLOSED:
                breaker.transitionToClosedState();
                break;
            case FORCED_OPEN:
                breaker.transitionToForcedOpenState();
                break;
            case DISABLED:
                breaker.transitionToDisabledState();
                break;
            default:
                throw new IllegalArgumentException(
                    "Expected one of CLOSED, FORCED_OPEN, or DISABLED.");
        }
    }

    private static final class BreakerStateUpdate {
        private final State state;

        @JsonCreator
        public BreakerStateUpdate(
            @JsonProperty("updateState") String arg
        ) {
            this.state = State.valueOf(arg);
            
            if (arg == null)
                throw new HttpClientErrorException(
                    HttpStatus.BAD_REQUEST,
                    "State must be one of CLOSED, FORCED_OPEN, or DISABLED.");
        }

        public State state() {
            return state;
        }
    }
}
