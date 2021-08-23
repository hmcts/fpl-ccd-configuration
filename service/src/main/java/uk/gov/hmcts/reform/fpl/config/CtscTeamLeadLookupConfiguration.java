package uk.gov.hmcts.reform.fpl.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class CtscTeamLeadLookupConfiguration {
    private final String email;

    public CtscTeamLeadLookupConfiguration(@Value("${fpl.ctsc_team_lead_inbox}") String email) {
        this.email = email;
    }
}
