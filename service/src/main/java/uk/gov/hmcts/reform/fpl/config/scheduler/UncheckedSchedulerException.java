package uk.gov.hmcts.reform.fpl.config.scheduler;

import org.quartz.SchedulerException;

class UncheckedSchedulerException extends RuntimeException {
    UncheckedSchedulerException(SchedulerException ex) {
        super(ex);
    }
}
