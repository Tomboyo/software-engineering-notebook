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
        pause(100, 2_000);

        double k = rand.nextDouble();
        if (k < 0.1) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } else {
            return ResponseEntity.ok().build();
        }
//            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//            return ResponseEntity.badRequest().build();
//            return ResponseEntity.ok().build();
    }

    private void pause(long delay, int jitter) {
        try {
            Thread.sleep(delay + rand.nextInt(jitter));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
