package com.github.tomboyo.reactive;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

@RestController
public class MockController {

    private Random rand = new Random();

    @GetMapping("/")
    public ResponseEntity<Void> handleRequest() {
        // var mode = "5xx";
        var mode = "timeout";

        switch (mode) {
            case "4xx":
                pause(100, 1_000);
                return ResponseEntity.badRequest().build();
            case "5xx":
                pause(100, 1_000);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            case "timeout":
                pause(2_000, 1_000);
                return ResponseEntity.ok().build();
            default:
                pause(100, 1_000);
                return ResponseEntity.ok().build();
        }
    }

    private void pause(long delay, int jitter) {
        try {
            if (jitter > 0) {
                Thread.sleep(delay + rand.nextInt(jitter));
            } else {
                Thread.sleep(delay);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
