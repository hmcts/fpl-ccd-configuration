package uk.gov.hmcts.reform.fpl.config;

import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("default")
@Configuration
@EnableCircuitBreaker
public class CircuitBreakerConfiguration {
}
