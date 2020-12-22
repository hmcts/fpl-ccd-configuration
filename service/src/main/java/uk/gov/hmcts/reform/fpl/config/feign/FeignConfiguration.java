package uk.gov.hmcts.reform.fpl.config.feign;

import feign.ExceptionPropagationPolicy;
import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfiguration {

    @Bean
    public Retryer retryer() {
        return new FPLRetryer();
    }

    @Bean
    public ExceptionPropagationPolicy propagationPolicy() {
        return ExceptionPropagationPolicy.UNWRAP;
    }
}
