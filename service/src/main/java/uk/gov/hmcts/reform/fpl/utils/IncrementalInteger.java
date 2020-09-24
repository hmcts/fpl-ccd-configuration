package uk.gov.hmcts.reform.fpl.utils;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import javax.annotation.concurrent.NotThreadSafe;

@NoArgsConstructor
@AllArgsConstructor
@NotThreadSafe
public class IncrementalInteger {
    private int value;

    public int getAndIncrement() {
        return value++;
    }

    public int incrementAndGet() {
        return ++value;
    }

    public int getValue() {
        return value;
    }
}
