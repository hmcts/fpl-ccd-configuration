package uk.gov.hmcts.reform.fpl.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.fpl.config.utils.LookupConfigParser;

import java.util.Map;

@Configuration
public class HmctsCourtLookupConfiguration {

    private final Map<String, Court> mapping;

    public HmctsCourtLookupConfiguration(@Value("${fpl.local_authority_code_to_hmcts_court.mapping}") String config) {
        this.mapping = LookupConfigParser.parse(config, value -> {
            String[] entrySplit = value.split(":");
            return new Court(entrySplit[0], entrySplit[1]);
        });
    }

    public Map<String, Court> getLookupTable() {
        return mapping;
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

