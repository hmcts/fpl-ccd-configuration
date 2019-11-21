package uk.gov.hmcts.reform.fpl.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class DefaultEmailLookupConfiguration {
    private final String emailAddress;

    public DefaultEmailLookupConfiguration(@Value("${fpl.default_email.mapping}") String emailAddress) {
        this.emailAddress = emailAddress;
    }
}
