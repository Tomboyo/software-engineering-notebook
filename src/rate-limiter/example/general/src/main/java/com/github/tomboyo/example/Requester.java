package com.github.tomboyo.example;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.vavr.control.Try;

@RestController
@RequestMapping(path = "requester")
public class Requester implements SchedulingConfigurer {
    private static final Logger logger =
        LoggerFactory.getLogger(Requester.class);

    private final PingApi client;
    private final ExecutorService requestPool;

    private volatile Duration betweenRequests;

    @Autowired
    public Requester(
        PingApi client
    ) {
        this.client = client;

        this.requestPool = Executors.newSingleThreadScheduledExecutor();
        this.betweenRequests = Duration.ofSeconds(1);
    }

    @PostMapping
    public void configure(
        int delayBetweenRequests
    ) {
        if (delayBetweenRequests < 0) {
            throw new IllegalArgumentException();
        }

        this.betweenRequests = Duration.ofMillis(delayBetweenRequests);
    }

    /**
     * Register a task which submits `this::makeCall` to the request pool, then
     * waits `betweenRequests` before triggering the task again.
     * 
     * The delay between triggering is dynamic and reflects the value of
     * `betweenRequests` as it changs (see {@link #configure(int)}).
     * 
     * We submit `this::makeCall` to a thread pool so that the trigger does not
     * block on `makeCall` in addition to waiting for the `betweenRequests`
     * delay. The effect is that the trigger appears to initiate requests on a
     * steady schedule, regardless of how long they take to finish.
     */
    @Override
    public void configureTasks(
        ScheduledTaskRegistrar taskRegistrar
    ) {
        taskRegistrar.addTriggerTask(
            () -> requestPool.submit(this::makeCall),
            context -> Date.from(Instant.now().plus(betweenRequests)));
    }

    private void makeCall() {
        Try.ofSupplier(() -> TimedCall.time(client::ping))
            .onSuccess(x ->
                logger.info(
                    "Call {} in {} ms",
                    x.value.getStatusCodeValue(),
                    x.elapsed.toMillis()))
            .onFailure(Exception.class, e ->
                logger.error("Call failed: {}", e.getMessage()));
    }
}
