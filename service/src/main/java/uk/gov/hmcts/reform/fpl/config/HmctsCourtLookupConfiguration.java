package uk.gov.hmcts.reform.fpl.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.fpl.config.utils.LookupConfigParser;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.emptyToNull;

@Configuration
public class HmctsCourtLookupConfiguration {

    private final Map<String, Court> mapping;

    public HmctsCourtLookupConfiguration(@Value("${fpl.local_authority_code_to_hmcts_court.mapping}") String config) {
        this.mapping = LookupConfigParser.parse(config, value -> {
            String[] entrySplit = value.split(":", 2);
            return new Court(
                checkNotNull(emptyToNull(entrySplit[0]), "Court name cannot be empty"),
                checkNotNull(emptyToNull(entrySplit[1]), "Court email cannot be empty")
            );
        });
    }

    public Court getCourt(String localAuthorityCode) {
        checkNotNull(localAuthorityCode, "Case does not have local authority assigned");

        return checkNotNull(mapping.get(localAuthorityCode), "Court information not found");
    }

    public static class Court {
        private final String name;
        private final String email;

        public Court(String name, String email) {
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

