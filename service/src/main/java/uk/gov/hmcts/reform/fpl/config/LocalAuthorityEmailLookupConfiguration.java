package uk.gov.hmcts.reform.fpl.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.fpl.config.utils.LookupConfigParser;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.emptyToNull;

@Configuration
public class LocalAuthorityEmailLookupConfiguration {

    private final Map<String, LocalAuthority> mapping;

    public LocalAuthorityEmailLookupConfiguration(@Value("${fpl.local_authority_code_to_local_authority.mapping}")
                                                      String config) {
        this.mapping = LookupConfigParser.parse(config, value -> {
            String[] entrySplit = value.split(":", 2);
            return new LocalAuthority(
                checkNotNull(emptyToNull(entrySplit[0]), "Local Authority name cannot be empty"),
                checkNotNull(emptyToNull(entrySplit[1]), "Local Authority email cannot be empty")
            );
        });
    }

    public LocalAuthority getLocalAuthority(String localAuthorityCode) {
        checkNotNull(localAuthorityCode, "Local authority code cannot be null");

        return checkNotNull(mapping.get(localAuthorityCode), "Local authority '" + localAuthorityCode + "' not found");
    }

    public static class LocalAuthority {
        private final String name;
        private final String email;

        public LocalAuthority(String name, String email) {
            this.name = name;
            this.email = email;
        }

        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
        }
    }
}
