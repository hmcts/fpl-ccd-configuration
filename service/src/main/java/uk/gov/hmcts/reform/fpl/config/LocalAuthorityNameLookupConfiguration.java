package uk.gov.hmcts.reform.fpl.config;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.Map;

@Configuration
public class LocalAuthorityNameLookupConfiguration {

    private static final String MAPPINGS_SEPARATOR = ";";
    private static final String ENTRY_SEPARATOR = "=>";

    private Map<String, String> mapping;

    public LocalAuthorityNameLookupConfiguration(@Value("${fpl.local_authority_name.mapping}") String config) {
        ImmutableMap.Builder<String, String> localAuthorities = ImmutableMap.builder();

        Arrays.stream(config.split(MAPPINGS_SEPARATOR)).forEach(entry -> {
            String[] localAuthorityData = entry.split(ENTRY_SEPARATOR);
            localAuthorities.put(localAuthorityData[0], localAuthorityData[1]);
        });

        this.mapping = localAuthorities.build();
    }

    public Map<String, String> getLookupTable() {
        return mapping;
    }
}

