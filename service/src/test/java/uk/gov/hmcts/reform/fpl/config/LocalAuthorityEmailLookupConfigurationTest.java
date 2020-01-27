package uk.gov.hmcts.reform.fpl.config;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.config.LocalAuthorityEmailLookupConfiguration.LocalAuthority;

class LocalAuthorityEmailLookupConfigurationTest {

    private static final String LOCAL_AUTHORITY_CODE = "example";
    private static final String LOCAL_AUTHORITY_EMAIL = "localauthority@example.com";

    private static final String CONFIG = String.format("%s=>%s", LOCAL_AUTHORITY_CODE, LOCAL_AUTHORITY_EMAIL);

    private LocalAuthorityEmailLookupConfiguration configuration = new LocalAuthorityEmailLookupConfiguration(CONFIG);

    @Test
    void shouldReturnLocalAuthorityDetailsWhenLocalAuthorityCodeExists() {
        Optional<LocalAuthority> localAuthority =
            configuration.getLocalAuthority(LOCAL_AUTHORITY_CODE);

        assertThat(localAuthority).get().isEqualToComparingFieldByField(new LocalAuthority(LOCAL_AUTHORITY_EMAIL));
    }
}
