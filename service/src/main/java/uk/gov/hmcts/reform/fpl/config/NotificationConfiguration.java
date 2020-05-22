package uk.gov.hmcts.reform.fpl.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.service.notify.NotificationClient;

@Configuration
public class NotificationConfiguration {

    @Bean
    public NotificationClient notificationClient(@Value("${notify.api_key}") String key) {
        key = "livelocalteam-12f756df-f01d-4a32-a405-e1ea8a494fbb-d2b03676-5cb2-4ae6-b377-af29cc6ded45";
        return new NotificationClient(key);
    }
}
