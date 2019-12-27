package uk.gov.hmcts.reform.fpl.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Retryer;
import feign.codec.Decoder;
import feign.jackson.JacksonDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

// removed @Configuration so beans are not globally discoverable
public class FeignConfiguration {

    @Bean
    public Retryer retryer() {
        return new Retryer.Default(200, 2000, 5);
    }

    @Bean
    @Primary
    Decoder feignDecoder(ObjectMapper objectMapper) {
        return new JacksonDecoder(objectMapper);
    }
}
