package com.github.tomboyo.example;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;

@RestController
public class PingController {

    /*
     * Our rate-limited endpoint will throw RequestNotPermitted exceptions when
     * allowed request volume is exceeded. This function converts those into a
     * 429 (Too Many Requests) so that they do not instead resolve in a 500.
     */
    @ExceptionHandler({ RequestNotPermitted.class })
    public ResponseEntity<Void> handleException() {
        return ResponseEntity
            .status(HttpStatus.TOO_MANY_REQUESTS)
            .build();
    }

    // tag::ratelimiter[]
    @GetMapping("/")
    @RateLimiter(name = "myratelimiter")
    public String ping() throws InterruptedException {
        Thread.sleep(200);
        return "Ok";
    }
    // end::ratelimiter[]
}
