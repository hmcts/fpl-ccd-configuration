package uk.gov.hmcts.reform.rd.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.ExceptionPropagationPolicy;
import feign.Retryer;
import feign.codec.Decoder;
import feign.codec.ErrorDecoder;
import feign.jackson.JacksonDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

public class PrdFeignConfiguration {

    @Bean
    public Retryer retryer() {
        return new Retryer.Default(500, 2000, 3);
    }

    @Bean
    @Primary
    Decoder feignDecoder(ObjectMapper objectMapper) {
        return new JacksonDecoder(objectMapper);
    }

    @Bean
    ErrorDecoder errorDecoder() {
        return new PrdFeignCustomDecoder();
    }

    @Bean
    ExceptionPropagationPolicy eee() {
        return ExceptionPropagationPolicy.UNWRAP;
    }
}
