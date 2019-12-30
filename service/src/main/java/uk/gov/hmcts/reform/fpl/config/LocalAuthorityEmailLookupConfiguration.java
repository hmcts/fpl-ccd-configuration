package uk.gov.hmcts.reform.fpl.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.fpl.config.utils.LookupConfigParser;

import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.emptyToNull;

@Configuration
public class LocalAuthorityEmailLookupConfiguration {

    private final Map<String, LocalAuthority> mapping;

    public LocalAuthorityEmailLookupConfiguration(@Value("${fpl.local_authority_code_to_shared_inbox.mapping}")
                                                      String config) {
        this.mapping = LookupConfigParser.parse(config, value -> new LocalAuthority(
            checkNotNull(emptyToNull(value), "Local Authority name cannot be empty")
        ));
    }

    public Optional<LocalAuthority> getLocalAuthority(String localAuthorityCode) {
        checkNotNull(localAuthorityCode, "Local authority code cannot be null");
        return Optional.ofNullable(mapping.get(localAuthorityCode));
    }

    public static class LocalAuthority {
        private final String email;

        public LocalAuthority(String email) {
            this.email = email;
        }

        public String getEmail() {
            return email;
        }
    }
}
