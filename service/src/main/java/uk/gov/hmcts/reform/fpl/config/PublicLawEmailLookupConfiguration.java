package uk.gov.hmcts.reform.fpl.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class PublicLawEmailLookupConfiguration {
    private final String emailAddress;

    public PublicLawEmailLookupConfiguration(@Value("${fpl.public_law_email.mapping}") String emailAddress) {
        this.emailAddress = emailAddress;
    }
}
