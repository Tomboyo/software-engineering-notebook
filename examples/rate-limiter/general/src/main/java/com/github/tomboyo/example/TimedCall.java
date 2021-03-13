package com.github.tomboyo.example;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Supplier;

public class TimedCall<T> {
    public final Duration elapsed;
    public final T value;

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
}
