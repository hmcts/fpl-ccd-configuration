package uk.gov.hmcts.reform.fpl.config;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Configuration
public class UserEmailLookupConfiguration {

    private static final String MAPPINGS_SEPARATOR = ";";
    private static final String LA_SEPARATOR = "=>";
    private static final String USER_SEPARATOR = ",";
    private final Map<String, List<String>> mapping;

    public UserEmailLookupConfiguration(@Value("${fpl.local_authority_code_to_hmcts_email.mapping}") String config) {
        ImmutableMap.Builder<String, List<String>> emails = ImmutableMap.builder();

        Arrays.stream(config.split(MAPPINGS_SEPARATOR)).forEach(entry -> {
            String[] localAuthorityData = entry.split(LA_SEPARATOR);
            List<String> ids = ImmutableList.<String>builder().add(localAuthorityData[1].split(USER_SEPARATOR)).build();
            emails.put(localAuthorityData[0], ids);
        });
        this.mapping = emails.build();
    }

    public Map<String, List<String>> getLookupTable() {
        return mapping;
    }
}

