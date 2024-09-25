package uk.gov.hmcts.reform.fpl.utils;

import jakarta.validation.ClockProvider;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Configuration
@Import(TestConfiguration.class)
public class FixedTimeConfiguration {
    private static final ZonedDateTime NOW = ZonedDateTime.now();

    @Bean
    public ClockProvider fixedClockProvider() {
        return () -> Clock.fixed(NOW.toInstant(), NOW.getZone());
    }

    @Bean
    @Primary
    public Time stoppedTime() {
        return new FixedTime(NOW.toLocalDateTime());
    }

    public Time fixedDateTime(LocalDateTime fixed) {
        return () -> fixed;
    }

    @Bean
    public LocalValidatorFactoryBean localValidatorFactoryBean() {
        return new LocalValidatorFactoryBean() {
            @Override
            protected void postProcessConfiguration(jakarta.validation.Configuration<?> configuration) {
                configuration.clockProvider(fixedClockProvider());
            }
        };
    }

}
