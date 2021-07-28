package uk.gov.hmcts.reform.fpl.config;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.exceptions.UnknownLocalAuthorityCodeException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalAuthorityUserLookupConfigurationTest {

    private static final String LOCAL_AUTHORITY_CODE = "example";
    private static final String LOCAL_AUTHORITY_USER_IDS = "1|2|3";

    private static final String CONFIG = String.format("%s=>%s", LOCAL_AUTHORITY_CODE, LOCAL_AUTHORITY_USER_IDS);

    private LocalAuthorityUserLookupConfiguration configuration = new LocalAuthorityUserLookupConfiguration(CONFIG);

    @Test
    void shouldThrowNullPointerExceptionUponInitialisationWhenMappingValueIsEmpty() {
        Assertions.assertThatThrownBy(() -> new LocalAuthorityUserLookupConfiguration(CONFIG + ";fake=>"))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("Mapping value cannot be empty");
    }

    @Test
    void shouldThrowNullPointerExceptionWhenLocalAuthorityCodeIsNull() throws IllegalArgumentException {
        assertThatThrownBy(() ->
            configuration.getUserIds(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("Local authority code cannot be null");
    }

    @Test
    void shouldThrowCustomExceptionWhenLocalAuthorityCodeDoesNotExist() throws IllegalArgumentException {
        assertThatThrownBy(() ->
            configuration.getUserIds("FAKE"))
            .isInstanceOf(UnknownLocalAuthorityCodeException.class)
            .hasMessage("Local authority 'FAKE' was not found");
    }

    @Test
    void shouldReturnLocalAuthorityUserIdsWhenLocalAuthorityCodeExists() {
        List<String> localAuthorityUserIds = configuration.getUserIds(LOCAL_AUTHORITY_CODE);

        assertThat(localAuthorityUserIds).containsExactly(LOCAL_AUTHORITY_USER_IDS.split("\\|"));
    }

}
