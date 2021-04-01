package uk.gov.hmcts.reform.fpl.config;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LocalAuthorityCodeLookupConfigurationTest {

    private static final String LOCAL_AUTHORITY_EMAIL_DOMAIN = "example.gov.uk";
    private static final String LOCAL_AUTHORITY_CODE = "example";

    private static final String CONFIG = String.format("%s=>%s", LOCAL_AUTHORITY_EMAIL_DOMAIN, LOCAL_AUTHORITY_CODE);

    private LocalAuthorityCodeLookupConfiguration underTest = new LocalAuthorityCodeLookupConfiguration(CONFIG);

    @Test
    void shouldThrowNullPointerExceptionUponInitialisationWhenMappingValueIsEmpty() {
        Assertions.assertThatThrownBy(() -> new LocalAuthorityCodeLookupConfiguration(CONFIG + ";fake=>"))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("Mapping value cannot be empty");
    }

    @Test
    void shouldThrowNullPointerExceptionWhenLocalAuthorityEmailDomainIsNull() {
        Assertions.assertThatThrownBy(() -> underTest.getLocalAuthorityCode(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("Email domain cannot be null");
    }

    @Test
    void shouldReturnEmptyLocalAuthorityCodeIdNoLocalAuthorityWithGivenDomain() {
        assertThat(underTest.getLocalAuthorityCode("fake@example.com")).isEmpty();
    }

    @Test
    void shouldReturnLocalAuthorityCodeWhenLocalAuthorityEmailDomainExists() {
        assertThat(underTest.getLocalAuthorityCode(LOCAL_AUTHORITY_EMAIL_DOMAIN)).contains(LOCAL_AUTHORITY_CODE);
    }

}
