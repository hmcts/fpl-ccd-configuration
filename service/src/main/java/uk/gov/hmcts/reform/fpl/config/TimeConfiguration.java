package uk.gov.hmcts.reform.fpl.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Configuration
public class TimeConfiguration {

    public static final ZoneId LONDON_TIMEZONE = ZoneId.of("Europe/London");

    @Bean
    @Lazy
    public Time currentTime() {
        return () -> ZonedDateTime.now(LONDON_TIMEZONE).toLocalDateTime();
    }
}
