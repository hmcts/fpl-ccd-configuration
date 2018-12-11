package uk.gov.hmcts.reform.fpl.config;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CafcassEmailLookupConfigurationTest {

    private static final String LOCAL_AUTHORITY_CODE = "example";
    private static final String CAFCASS_NAME = "example";
    private static final String CAFCASS_EMAIL = "cafcass@example.com";

    private static final String CONFIG = String.format("%s=>%s:%s", LOCAL_AUTHORITY_CODE, CAFCASS_NAME, CAFCASS_EMAIL);

    private CafcassEmailLookupConfiguration configuration = new CafcassEmailLookupConfiguration(CONFIG);

    @Test
    void shouldThrowNullPointerExceptionUponInitialisationWhenMappingValueIsEmpty() {
        Assertions.assertThatThrownBy(() -> new CafcassEmailLookupConfiguration(CONFIG + ";fake=>"))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("Mapping value cannot be empty");
    }

    @Test
    void shouldThrowNullPointerExceptionUponInitialisationWhenCafcassNameIsEmpty() {
        Assertions.assertThatThrownBy(() -> new CafcassEmailLookupConfiguration(CONFIG + ";fake=>:fake@example.com"))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("Cafcass name cannot be empty");
    }

    @Test
    void shouldThrowNullPointerExceptionUponInitialisationWhenCafcassEmailIsEmpty() {
        Assertions.assertThatThrownBy(() -> new CafcassEmailLookupConfiguration(CONFIG + ";fake=>example:"))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("Cafcass email cannot be empty");
    }

    @Test
    void shouldThrowNullPointerExceptionWhenLocalAuthorityCodeIsNull() {
        assertThatThrownBy(() -> configuration.getCafcass(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("Local authority code cannot be null");
    }

    @Test
    void shouldThrowNullPointerExceptionWhenLocalAuthorityCodeDoesNotExist() {
        assertThatThrownBy(() -> configuration.getCafcass("FAKE"))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("Local authority 'FAKE' not found");
    }

    @Test
    void shouldReturnCafcassEmailWhenLocalAuthorityCodeExists() {
        CafcassEmailLookupConfiguration.Cafcass cafcass = configuration.getCafcass(LOCAL_AUTHORITY_CODE);

        assertThat(cafcass).isEqualToComparingFieldByField(
            new CafcassEmailLookupConfiguration.Cafcass(CAFCASS_NAME, CAFCASS_EMAIL));
    }
}
