package uk.gov.hmcts.reform.fpl.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.fpl.config.utils.LookupConfigParser;

import java.util.Map;

@Configuration
public class LocalAuthorityNameLookupConfiguration {

    private final Map<String, String> mapping;

    public LocalAuthorityNameLookupConfiguration(@Value("${fpl.local_authority_name.mapping}") String config) {
        this.mapping = LookupConfigParser.parseStringValue(config);
    }

    public Map<String, String> getLookupTable() {
        return mapping;
    }
}

