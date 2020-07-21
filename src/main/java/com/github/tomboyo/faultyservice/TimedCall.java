package com.github.tomboyo.faultyservice;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.function.Supplier;

public class TimedCall<T> {
    private final Duration elapsed;
    private final T value;

    private TimedCall(Duration elapsed, T value) {
        this.elapsed = elapsed;
        this.value = value;
    }

    public static <T> TimedCall<T> time(Supplier<T> supplier) {
        Instant start = Instant.now();
        T value = supplier.get();
        Duration elapsed = Duration.between(start, Instant.now());
        return new TimedCall<>(elapsed, value);
    }

    public Duration elapsed() {
        return elapsed;
    }

    public T value() {
        return value;
    }
}
