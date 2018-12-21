package uk.gov.hmcts.reform.fpl.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.fpl.config.utils.LookupConfigParser;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.emptyToNull;

@Configuration
public class CafcassLookupConfiguration {

    private final Map<String, Cafcass> mapping;

    public CafcassLookupConfiguration(@Value("${fpl.local_authority_code_to_cafcass.mapping}") String config) {
        this.mapping = LookupConfigParser.parse(config, value -> {
            String[] entrySplit = value.split(":", 2);
            return new Cafcass(
                checkNotNull(emptyToNull(entrySplit[0]), "Cafcass name cannot be empty"),
                checkNotNull(emptyToNull(entrySplit[1]), "Cafcass email cannot be empty")
            );
        });
    }

    public Cafcass getCafcass(String localAuthorityCode) {
        checkNotNull(localAuthorityCode, "Local authority code cannot be null");

        return checkNotNull(mapping.get(localAuthorityCode), "Local authority '" + localAuthorityCode + "' not found");
    }

    public static class Cafcass {
        private final String name;
        private final String email;

        public Cafcass(String name, String email) {
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
