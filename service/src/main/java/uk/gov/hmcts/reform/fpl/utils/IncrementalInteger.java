package uk.gov.hmcts.reform.fpl.utils;

import com.launchdarkly.shaded.javax.annotation.concurrent.NotThreadSafe;
import lombok.AllArgsConstructor;

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
