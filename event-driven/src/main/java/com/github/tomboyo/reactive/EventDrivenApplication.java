package com.github.tomboyo.reactive;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@SpringBootApplication
public class EventDrivenApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventDrivenApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(EventDrivenApplication.class, args);
    }

    @Bean
    public Supplier<Flux<Long>> clock() {
        return () -> Flux
                .<Long, Long>generate(
                        () -> 0L,
                        (state, sink) -> {
                            sink.next(state);
                            return state + 1L;
                        })
                .doOnNext(x -> LOGGER.info("clock generate - {}", x))
                .delayElements(Duration.ofMillis(500));
    }

    @Bean
    public Consumer<Flux<Long>> request() {
        RestTemplate template = new RestTemplate();

        return flux -> flux
                .parallel(2)
                .runOn(Schedulers.boundedElastic())
                .flatMap(x -> Mono
                        .fromSupplier(() -> {
                            template.getForObject("http://localhost:8080/", String.class);
                            return x + " - OK";
                        })
                        .doOnError(e -> LOGGER.error("Error {}", e.getMessage()))
                        .onErrorReturn("ERROR"))
                .doOnNext(x -> LOGGER.info("request - {}", x))
                .subscribe();
    }
}
