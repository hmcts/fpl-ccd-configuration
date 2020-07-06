package uk.gov.hmcts.reform.fpl.config;

import com.sendgrid.SendGrid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SendGridConfiguration {

    @Bean
    public SendGrid sendGrid(@Value("${sendgrid.api_key}") String key) {
        return new SendGrid(key);
    }
}
