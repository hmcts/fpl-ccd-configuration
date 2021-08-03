package uk.gov.hmcts.reform.fpl.config.tranlsation;

import lombok.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Value
@Configuration
@ConfigurationProperties(prefix = "translation.unit.notification")
public class TranslationEmailConfiguration {
    String sender;
    String recipient;
}
