package com.github.tomboyo.faultyservice.controller;

import java.util.concurrent.atomic.AtomicReference;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class FaultyController {
    public static final class Configuration {
        private final long delayMillis;

        @JsonCreator
        protected Configuration(
            @JsonProperty("delay_millis") long delayMillis
        ) {
            this.delayMillis = delayMillis;
        }

        public long delayMillis() {
            return delayMillis;
        }
    }

    private AtomicReference<Configuration> config;

    public FaultyController() {
        config = new AtomicReference<>(new Configuration(1_000));
    }

    @PostMapping("/")
    public void config(
        @RequestBody Configuration config
    ) {
        if (config.delayMillis() <= 0) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "delay_millis must be > 0");
        }

        this.config.set(config);
    }

    @GetMapping("/")
    public String serve() throws InterruptedException {
        Thread.sleep(config.get().delayMillis());
        return "Ok";
    }
}