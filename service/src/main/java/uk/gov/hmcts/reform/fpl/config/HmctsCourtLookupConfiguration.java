package uk.gov.hmcts.reform.fpl.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
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
            String[] entrySplit = value.split(":", 3);
            return new Court(
                checkNotNull(emptyToNull(entrySplit[0]), "Court name cannot be empty"),
                checkNotNull(emptyToNull(entrySplit[1]), "Court email cannot be empty"),
                checkNotNull(emptyToNull(entrySplit[2]), "Court code cannot be empty")
            );
        });
    }

    public Court getCourt(String localAuthorityCode) {
        checkNotNull(localAuthorityCode, "Local authority code cannot be null");

        return checkNotNull(mapping.get(localAuthorityCode), "Local authority '" + localAuthorityCode + "' not found");
    }

    @Getter
    @RequiredArgsConstructor
    public static class Court {
        private final String name;
        private final String email;
        private final String courtCode;
    }
}

