package uk.gov.hmcts.reform.fpl.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.fpl.config.utils.LookupConfigParser;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

@Configuration
public class LocalAuthorityNameLookupConfiguration {

    private final Map<String, String> mapping;

    public LocalAuthorityNameLookupConfiguration(@Value("${fpl.local_authority_code_to_name.mapping}") String config) {
        this.mapping = LookupConfigParser.parseStringValue(config);
    }

    public String getLocalAuthorityName(String localAuthorityCode) {
        checkNotNull(localAuthorityCode, "Local authority code cannot be null");

        return checkNotNull(mapping.get(localAuthorityCode), "Local authority '" + localAuthorityCode + "' not found");
    }

    public Map<String, String> getLocalAuthoritiesNames() {
        return new HashMap<>(mapping);
    }
}
