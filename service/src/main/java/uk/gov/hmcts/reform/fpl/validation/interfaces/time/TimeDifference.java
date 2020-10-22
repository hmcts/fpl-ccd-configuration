package uk.gov.hmcts.reform.fpl.validation.interfaces.time;

import java.time.temporal.ChronoUnit;

public @interface TimeDifference {
    long amount();
    ChronoUnit unit();
}
