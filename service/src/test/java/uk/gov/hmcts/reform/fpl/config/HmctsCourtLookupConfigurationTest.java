package uk.gov.hmcts.reform.fpl.config;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration.Court;

import static org.assertj.core.api.Assertions.assertThat;

class HmctsCourtLookupConfigurationTest {

    private static final String LOCAL_AUTHORITY_CODE = "example";
    private static final String COURT_NAME = "Example Court";
    private static final String COURT_EMAIL = "example@court.com";

    private static final String CONFIG = String.format("%s=>%s:%s", LOCAL_AUTHORITY_CODE, COURT_NAME, COURT_EMAIL);

    private HmctsCourtLookupConfiguration configuration = new HmctsCourtLookupConfiguration(CONFIG);

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
        Assertions.assertThatThrownBy(() -> configuration.getCourt(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("Case does not have local authority assigned");
    }

    @Test
    void shouldThrowNullPointerExceptionWhenCourtIsNull() {
        Assertions.assertThatThrownBy(() -> configuration.getCourt("FAKE"))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("Court information not found");
    }

    @Test
    void shouldReturnCourtInformationWhenLocalAuthorityExist() {
        Court court = configuration.getCourt("example");

        assertThat(court).isEqualToComparingFieldByField(new Court(COURT_NAME, COURT_EMAIL));
    }

}
