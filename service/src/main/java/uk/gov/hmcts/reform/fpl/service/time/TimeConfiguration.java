package uk.gov.hmcts.reform.fpl.service.time;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Configuration
public class TimeConfiguration {

    @Bean
    @Lazy
    public Time currentTime() {
        return () -> ZonedDateTime.now(ZoneId.of("Europe/London")).toLocalDateTime();
    }
}
