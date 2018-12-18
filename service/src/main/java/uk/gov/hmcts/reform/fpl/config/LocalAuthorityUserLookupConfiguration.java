package uk.gov.hmcts.reform.fpl.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.fpl.config.utils.LookupConfigParser;
import uk.gov.hmcts.reform.fpl.exceptions.UnknownLocalAuthorityCodeException;

import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

@Configuration
public class LocalAuthorityUserLookupConfiguration {

    private final Map<String, List<String>> mapping;

    public LocalAuthorityUserLookupConfiguration(@Value("${fpl.local_authority_user.mapping}") String config) {
        this.mapping = LookupConfigParser.parseStringListValue(config);
    }

    public List<String> getUserIds(String localAuthorityCode) {
        checkNotNull(localAuthorityCode, "Local authority code cannot be null");

        List<String> userIds = mapping.get(localAuthorityCode);

        if (userIds == null) {
            throw new UnknownLocalAuthorityCodeException("Local authority '" + localAuthorityCode + "' was not found");
        }

        return userIds;
    }
}

