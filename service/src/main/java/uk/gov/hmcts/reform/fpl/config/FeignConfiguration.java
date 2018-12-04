package uk.gov.hmcts.reform.fpl.config;

import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfiguration {

    @Bean
    public Retryer retryer() {
        return new Retryer.Default(200, 2000, 5);
    }

}
