package uk.gov.hmcts.reform.fpl.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.fpl.config.utils.LookupConfigParser;

import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.emptyToNull;
import static java.util.function.Predicate.not;

@Configuration
public class CafcassLookupConfiguration {
    private static final String WELSH_CAFCASS = "Cafcass Cymru";
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

    public Optional<Cafcass> getCafcassWelsh(String localAuthorityCode) {
        return Optional.of(getCafcass(localAuthorityCode))
                .filter(cafcass -> WELSH_CAFCASS.equals(cafcass.name));
    }

    public Optional<Cafcass> getCafcassEngland(String localAuthorityCode) {
        return Optional.of(getCafcass(localAuthorityCode))
                .filter(not(cafcass -> WELSH_CAFCASS.equals(cafcass.name)));
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
