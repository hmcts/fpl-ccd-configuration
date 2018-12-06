package uk.gov.hmcts.reform.fpl.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.fpl.config.utils.LookupConfigParser;

import java.util.Map;

@Configuration
public class LocalAuthorityCodeLookupConfiguration {

    private final Map<String, String> mapping;

    public LocalAuthorityCodeLookupConfiguration(@Value("${fpl.local_authority_email_to_code.mapping}") String config) {
        this.mapping = LookupConfigParser.parseStringValue(config);
    }

    public Map<String, String> getLookupTable() {
        return mapping;
    }
}

