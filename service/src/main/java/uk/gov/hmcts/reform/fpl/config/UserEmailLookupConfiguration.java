package uk.gov.hmcts.reform.fpl.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.fpl.config.utils.LookupConfigParser;

import java.util.List;
import java.util.Map;

@Configuration
public class UserEmailLookupConfiguration {

    private final Map<String, List<String>> mapping;

    public UserEmailLookupConfiguration(@Value("${fpl.local_authority_code_to_hmcts_email.mapping}") String config) {
        this.mapping = LookupConfigParser.parseStringListValue(config);
    }

    public Map<String, List<String>> getLookupTable() {
        return mapping;
    }
}

