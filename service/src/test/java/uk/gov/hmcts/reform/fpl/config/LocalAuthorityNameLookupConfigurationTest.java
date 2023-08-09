package uk.gov.hmcts.reform.fpl.config;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalAuthorityNameLookupConfigurationTest {

    private static final String LOCAL_AUTHORITY_CODE = "example";
    private static final String LOCAL_AUTHORITY_NAME = "Example Local Authority";

    private static final String CONFIG = String.format("%s=>%s", LOCAL_AUTHORITY_CODE, LOCAL_AUTHORITY_NAME);

    private LocalAuthorityNameLookupConfiguration configuration = new LocalAuthorityNameLookupConfiguration(CONFIG);

    @Test
    void shouldThrowNullPointerExceptionUponInitialisationWhenMappingValueIsEmpty() {
        Assertions.assertThatThrownBy(() -> new LocalAuthorityNameLookupConfiguration(CONFIG + ";fake=>"))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("Mapping value cannot be empty");
    }

    @Test
    void shouldThrowNullPointerExceptionWhenLocalAuthorityCodeIsNull() {
        assertThatThrownBy(() -> configuration.getLocalAuthorityName(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("Local authority code cannot be null");
    }

    @Test
    void shouldThrowNullPointerExceptionWhenLocalAuthorityCodeDoesNotExist() {
        assertThatThrownBy(() -> configuration.getLocalAuthorityName("FAKE"))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("Local authority 'FAKE' not found");
    }

    @Test
    void shouldReturnLocalAuthorityNameWhenLocalAuthorityCodeExists() {
        String localAuthorityName = configuration.getLocalAuthorityName(LOCAL_AUTHORITY_CODE);

        assertThat(localAuthorityName).isEqualTo(LOCAL_AUTHORITY_NAME);
    }

    @Test
    void shouldReturnAllLocalAuthorities() {
        assertThat(configuration.getLocalAuthoritiesNames())
            .isEqualTo(Map.of(LOCAL_AUTHORITY_CODE, LOCAL_AUTHORITY_NAME));
    }

}
