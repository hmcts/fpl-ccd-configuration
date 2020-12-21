package uk.gov.hmcts.reform.fpl.config.feign;

import feign.RetryableException;
import feign.Retryer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FPLRetryer extends Retryer.Default {

    int attempt;

    public FPLRetryer(int period, int maxPeriod, int maxAttempts) {
        super(period, maxPeriod, maxAttempts);
        this.attempt = 1;
    }

    public FPLRetryer() {
        this(500, 2000, 3);
    }

    @Override
    public void continueOrPropagate(RetryableException e) {
        log.info("Feign retry attempt {} due to {} ", attempt++, e.getCause().getMessage());
        super.continueOrPropagate(e);
    }

    @Override
    public Retryer clone() {
        return new FPLRetryer();
    }
}

