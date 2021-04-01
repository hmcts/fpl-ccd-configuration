package uk.gov.hmcts.reform.fpl.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.fpl.config.utils.LookupConfigParser;

import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;


@Configuration
public class LocalAuthorityCodeLookupConfiguration {

    private final Map<String, String> mapping;

    public LocalAuthorityCodeLookupConfiguration(@Value("${fpl.local_authority_email_to_code.mapping}") String config) {
        this.mapping = LookupConfigParser.parseStringValue(config);
    }

    public Optional<String> getLocalAuthorityCode(String emailDomain) {
        requireNonNull(emailDomain, "Email domain cannot be null");

        return Optional.ofNullable(mapping.getOrDefault(emailDomain, null));
    }

}
