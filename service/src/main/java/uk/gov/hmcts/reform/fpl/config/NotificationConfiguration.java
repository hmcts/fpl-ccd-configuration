package uk.gov.hmcts.reform.fpl.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.service.notify.NotificationClient;

@Configuration
public class NotificationConfiguration {

    @Bean
    public NotificationClient notificationClient(@Value("${notify.api_key}") String key) {
        return new NotificationClient(key);
    }
}
