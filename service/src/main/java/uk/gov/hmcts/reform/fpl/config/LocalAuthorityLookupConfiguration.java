package uk.gov.hmcts.reform.fpl.config;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.Map;

@Configuration
public class LocalAuthorityLookupConfiguration {

    @Value("${fpl.local_authority.mapping}")
    private String mapping;

    public Map<String, String> getLookupTable() {
        String stuff = mapping;

        return parse(stuff);
    }

    private Map<String, String> parse(String config) {
        ImmutableMap.Builder<String, String> localAuthorities = ImmutableMap.builder();

        Arrays.stream(config.split(";")).forEach(mapping -> {
            String[] localAuthorityData = mapping.split("=>");
            localAuthorities.put(localAuthorityData[0], localAuthorityData[1]);
        });

        return localAuthorities.build();
    }
}
