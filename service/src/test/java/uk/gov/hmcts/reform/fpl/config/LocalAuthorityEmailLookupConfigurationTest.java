package uk.gov.hmcts.reform.fpl.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LocalAuthorityEmailLookupConfigurationTest {

    private static final String LOCAL_AUTHORITY_CODE = "example";
    private static final String LOCAL_AUTHORITY_EMAIL = "cafcass@example.com";

    private static final String CONFIG = String.format("%s=>%s", LOCAL_AUTHORITY_CODE, LOCAL_AUTHORITY_EMAIL);

    private LocalAuthorityEmailLookupConfiguration configuration = new LocalAuthorityEmailLookupConfiguration(CONFIG);

    @Test
    void shouldReturnLocalAuthorityDetailsWhenLocalAuthorityCodeExists() {
        LocalAuthorityEmailLookupConfiguration.LocalAuthority localauthority =
            configuration.getLocalAuthority(LOCAL_AUTHORITY_CODE);

        assertThat(localauthority).isEqualToComparingFieldByField(
            new LocalAuthorityEmailLookupConfiguration.LocalAuthority(LOCAL_AUTHORITY_EMAIL));
    }
}
