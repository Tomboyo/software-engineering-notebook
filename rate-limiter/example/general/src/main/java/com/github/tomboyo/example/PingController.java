package com.github.tomboyo.example;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;

@RestController
public class PingController {
    @GetMapping("/")
    @RateLimiter(name = "myratelimiter")
    public String ping() throws InterruptedException {
        Thread.sleep(200);
        return "Ok";
    }
}
