package uk.gov.hmcts.reform.fpl.service.time;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
public class CurrentTime implements Time {
    @Override
    public LocalDateTime now() {
        return ZonedDateTime.now(ZoneId.of("Europe/London")).toLocalDateTime();
    }
}
