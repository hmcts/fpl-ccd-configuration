package uk.gov.hmcts.reform.fpl.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.fpl.config.utils.LookupConfigParser;
import uk.gov.hmcts.reform.fpl.model.Court;

import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.emptyToNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Configuration
public class HmctsCourtLookupConfiguration {

    private final Map<String, List<Court>> mapping;

    public HmctsCourtLookupConfiguration(@Value("${fpl.local_authority_code_to_hmcts_court.mapping}") String config) {

        this.mapping = LookupConfigParser.parseStringListValue(config).entrySet().stream()
            .collect(toMap(Map.Entry::getKey, this::convert));
    }

    private List<Court> convert(Map.Entry<String, List<String>> entry) {
        checkNotNull(entry.getValue(), "Court config is null for " + entry.getKey());

        return entry.getValue().stream()
            .map(this::convert)
            .collect(toList());
    }

    private Court convert(String value) {
        String[] entrySplit = value.split(":", 3);
        return new Court(
            checkNotNull(emptyToNull(entrySplit[0]), "Court name cannot be empty"),
            checkNotNull(emptyToNull(entrySplit[1]), "Court email cannot be empty"),
            checkNotNull(emptyToNull(entrySplit[2]), "Court code cannot be empty")
        );
    }

    public List<Court> getCourt(String localAuthorityCode) {
        checkNotNull(localAuthorityCode, "Local authority code cannot be null");

        return checkNotNull(mapping.get(localAuthorityCode),
            "Local authority '" + localAuthorityCode + "' not found");
    }

}

