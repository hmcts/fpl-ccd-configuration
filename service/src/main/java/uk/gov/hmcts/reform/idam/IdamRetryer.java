package uk.gov.hmcts.reform.idam;

import feign.RetryableException;
import feign.Retryer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class IdamRetryer implements Retryer {
    private final int maxNumAttempts;
    private long timeout; // ms
    private final double timeoutMultiplier;
    private int attempt;

    public IdamRetryer(int maxNumAttempts, long timeout, double timeoutMultiplier) {
        this.maxNumAttempts = maxNumAttempts;
        this.timeout = timeout;
        this.timeoutMultiplier = timeoutMultiplier;
        this.attempt = 1;
    }

    public IdamRetryer() {
        this(5, 500, 1);
    }

    @Override
    public void continueOrPropagate(RetryableException e) {
        log.info("Idam retry attempt {} due to {} ", attempt, e.getCause().getMessage());

        if (attempt++ == maxNumAttempts) {
            throw e;
        }

        try {
            Thread.sleep(timeout);
            timeout *= timeoutMultiplier;
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public Retryer clone() {
        return new IdamRetryer();
    }
}

