package uk.gov.hmcts.reform.fpl.utils;

import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.time.LocalDateTime;

public class FixedTime implements Time {

    private final LocalDateTime fixed;

    public FixedTime() {
        this.fixed = LocalDateTime.now();
    }

    public FixedTime(LocalDateTime time) {
        this.fixed = time;
    }

    @Override
    public LocalDateTime now() {
        return fixed;
    }
}
