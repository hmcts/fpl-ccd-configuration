package uk.gov.hmcts.reform.fpl.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.fpl.config.utils.LookupConfigParser;
import uk.gov.hmcts.reform.fpl.exceptions.UnknownLocalAuthorityException;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Configuration
public class LocalAuthorityIdLookupConfiguration {

    private final Map<String, String> mapping;

    public LocalAuthorityIdLookupConfiguration(@Value("${fpl.local_authority_code_to_org_id.mapping:}") String config) {
        if (StringUtils.isBlank(config)) {
            mapping = Collections.emptyMap();
            log.warn("Local authority code to organisation id config is missing or empty");
        } else {
            this.mapping = LookupConfigParser.parseStringValue(config);
        }
    }

    public String getLocalAuthorityId(String localAuthorityCode) {
        return Optional.ofNullable(mapping.get(localAuthorityCode))
            .orElseThrow(() -> new UnknownLocalAuthorityException(localAuthorityCode));
    }

}
