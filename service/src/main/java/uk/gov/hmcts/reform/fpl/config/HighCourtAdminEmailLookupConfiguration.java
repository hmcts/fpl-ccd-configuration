package uk.gov.hmcts.reform.fpl.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class HighCourtAdminEmailLookupConfiguration {
    private final String email;

    public HighCourtAdminEmailLookupConfiguration(@Value("${fpl.rcj_family_high_court_inbox}") String email) {
        this.email = email;
    }
}
