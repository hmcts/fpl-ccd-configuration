package uk.gov.hmcts.reform.fpl.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.fpl.config.utils.LookupConfigParser;
import uk.gov.hmcts.reform.fpl.exceptions.UnknownLocalAuthorityDomainException;

import java.util.Map;

import static org.assertj.core.util.Preconditions.checkNotNull;

@Configuration
public class LocalAuthorityCodeLookupConfiguration {

    private final Map<String, String> mapping;

    public LocalAuthorityCodeLookupConfiguration(@Value("${fpl.local_authority_email_to_code.mapping}") String config) {
        this.mapping = LookupConfigParser.parseStringValue(config);
    }

    public String getLocalAuthorityCode(String emailDomain) {
        checkNotNull(emailDomain, "Email domain cannot be null");

        if (mapping.get(emailDomain) == null) {
            throw new UnknownLocalAuthorityDomainException(emailDomain + " not found");
        }

        return mapping.get(emailDomain);
    }

}
