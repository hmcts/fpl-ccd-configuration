package uk.gov.hmcts.reform.fpl.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("default")
@Configuration
public class CircuitBreakerConfiguration {
}
