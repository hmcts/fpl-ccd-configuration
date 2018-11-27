package uk.gov.hmcts.reform.fpl.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class ClockConfiguration {

    @Bean
    public Clock getClock() {
        return Clock.systemDefaultZone();
    }

}
