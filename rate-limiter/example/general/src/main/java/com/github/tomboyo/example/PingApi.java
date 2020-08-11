package com.github.tomboyo.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class PingApi {
    
    private WebClient webClient;

    @Autowired
    public PingApi() {
        webClient = WebClient.builder()
            .baseUrl("http://localhost:8080/")
            .defaultHeader("Content-Type", "application/json")
            .build();
    }

    /**
     * Makes a blocking call to the ping endpoint and returns the body as a
     * String.
     */
    public String ping() {
        return webClient.get()
            .exchange()
            .block()
            .bodyToMono(String.class)
            .block();
    }
}
