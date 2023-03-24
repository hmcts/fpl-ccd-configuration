package uk.gov.hmcts.reform.fpl.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.fpl.config.utils.LookupConfigParser;

import java.util.Map;
import java.util.Optional;

@Configuration
public class HmctsCourtToCourtAdminLookupConfiguration {

    private final Map<String, String> mapping;

    public HmctsCourtToCourtAdminLookupConfiguration(@Value("${fpl.court_to_court_admin.mapping}") String config) {
        this.mapping = LookupConfigParser.parseStringValue(config);
    }

    public String getEmail(String courtCode) {
        return Optional.ofNullable(mapping.get(courtCode))
            .orElseThrow(() -> new IllegalArgumentException("Court admin email not found for court code " + courtCode));
    }
}
