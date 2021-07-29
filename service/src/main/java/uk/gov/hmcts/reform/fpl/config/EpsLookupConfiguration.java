package uk.gov.hmcts.reform.fpl.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.fpl.config.utils.LookupConfigParser;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

@Slf4j
@Configuration
public class EpsLookupConfiguration {

    private final Map<String, List<String>> mapping;

    public EpsLookupConfiguration(@Value("${fpl.eps_to_local_authorities.mapping:}") String config) {
        if (StringUtils.isBlank(config)) {
            mapping = emptyMap();
            log.warn("External professional solicitor to local authorities config is missing or empty");
        } else {
            this.mapping = LookupConfigParser.parse(config, value -> Stream.of(value.split("\\|"))
                .map(StringUtils::trim)
                .filter(StringUtils::isNotBlank)
                .collect(toList()));
        }
    }

    public List<String> getLocalAuthorities(String epsOrgId) {
        return defaultIfNull(mapping.get(epsOrgId), emptyList());
    }

}

