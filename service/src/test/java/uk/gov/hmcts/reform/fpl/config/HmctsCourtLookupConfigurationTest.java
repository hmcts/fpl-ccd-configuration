package uk.gov.hmcts.reform.fpl.config;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.Court;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HmctsCourtLookupConfigurationTest {

    private static final String LOCAL_AUTHORITY_CODE = "example";
    private static final String COURT_NAME = "Example Court";
    private static final String COURT_EMAIL = "example@court.com";
    private static final String COURT_CODE = "11";

    private static final String CONFIG = String.format("%s=>%s:%s:%s", LOCAL_AUTHORITY_CODE, COURT_NAME, COURT_EMAIL,
        COURT_CODE);

    private HmctsCourtLookupConfiguration configuration = new HmctsCourtLookupConfiguration(CONFIG);

    @Test
    void shouldThrowNullPointerExceptionUponInitialisationWhenMappingValueIsEmpty() {
        Assertions.assertThatThrownBy(() -> new HmctsCourtLookupConfiguration(CONFIG + ";fake=>"))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("Mapping value cannot be empty");
    }

    @Test
    void shouldThrowNullPointerExceptionUponInitialisationWhenCourtNameIsEmpty() {
        Assertions.assertThatThrownBy(() -> new HmctsCourtLookupConfiguration(CONFIG + ";fake=>:fake@example.com"))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("Court name cannot be empty");
    }

    @Test
    void shouldThrowNullPointerExceptionUponInitialisationWhenCourtEmailIsEmpty() {
        Assertions.assertThatThrownBy(() -> new HmctsCourtLookupConfiguration(CONFIG + ";fake=>Fake:"))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("Court email cannot be empty");
    }

    @Test
    void shouldThrowNullPointerExceptionWhenLocalAuthorityCodeIsNull() {
        Assertions.assertThatThrownBy(() -> configuration.getCourts(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("Local authority code cannot be null");
    }

    @Test
    void shouldThrowNullPointerExceptionWhenLocalAuthorityCodeDoesNotExist() {
        Assertions.assertThatThrownBy(() -> configuration.getCourts("FAKE"))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("Local authority 'FAKE' not found");
    }

    @Test
    void shouldReturnCourtInformationWhenLocalAuthorityCodeExists() {
        List<Court> court = configuration.getCourts(LOCAL_AUTHORITY_CODE);

        assertThat(court).containsExactly(new Court(COURT_NAME, COURT_EMAIL, COURT_CODE));
    }

    @Test
    void shouldReturnCourtByItsCode() {

        final Court expectedCourt = Court.builder()
            .code(COURT_CODE)
            .name(COURT_NAME)
            .email(COURT_EMAIL)
            .build();

        assertThat(configuration.getCourtByCode(COURT_CODE)).contains(expectedCourt);
    }

    @Test
    void shouldReturnEmptyWhenCourtWithGivenCodeDoesNotExists() {

        assertThat(configuration.getCourtByCode("NON EXISTING")).isEmpty();
    }

}
