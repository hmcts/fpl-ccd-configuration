package uk.gov.hmcts.reform.fpl.utils;

import lombok.AllArgsConstructor;
import javax.annotation.concurrent.NotThreadSafe;

@AllArgsConstructor
@NotThreadSafe
public class IncrementalInteger {
    private int currentValue;

    public IncrementalInteger() {
        currentValue = 0;
    }

    public int getAndIncrement() {
        return currentValue++;
    }

    public int incrementAndGet() {
        return ++currentValue;
    }
}
