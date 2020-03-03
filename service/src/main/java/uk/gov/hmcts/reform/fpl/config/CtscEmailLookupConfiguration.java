package uk.gov.hmcts.reform.fpl.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class CtscEmailLookupConfiguration {
    private final String email;

    public CtscEmailLookupConfiguration(@Value("${fpl.ctsc_inbox}") String email) {
        this.email = email;
    }
}
