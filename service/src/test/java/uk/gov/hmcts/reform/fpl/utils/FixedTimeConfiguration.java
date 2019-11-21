package uk.gov.hmcts.reform.fpl.utils;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Configuration
@Import(TestConfiguration.class)
public class FixedTimeConfiguration {
    public static final LocalDateTime NOW = ZonedDateTime.now(ZoneId.of("Europe/London")).toLocalDateTime();

    @Bean
    @Primary
    public Time stoppedTime() {
        return () -> NOW;
    }
}
