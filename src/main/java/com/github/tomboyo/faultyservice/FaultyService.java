package com.github.tomboyo.faultyservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@Service
public class FaultyService {
    
    private WebClient webClient;

    @Autowired
    public FaultyService() {
        webClient = WebClient.builder()
            .baseUrl("http://localhost:8080/")
            .defaultHeader("Content-Type", "application/json")
            .build();
    }

    /**
     * Makes a blocking call to the ping endpoint and returns the body as a
     * String.
     */
    @CircuitBreaker(name = "faultyservice-ping")
    public String ping() {
        return webClient.get()
            .exchange()
            .block()
            .bodyToMono(String.class)
            .block();
    }

    /**
     * Makes a blocking call to the ratelimited ping endpoint and returns the
     * body as a String.
     */
    public String ratelimitedPing() {
        return webClient.get()
            .uri("ratelimited")
            .exchange()
            .block()
            .bodyToMono(String.class)
            .block();
    }
}
