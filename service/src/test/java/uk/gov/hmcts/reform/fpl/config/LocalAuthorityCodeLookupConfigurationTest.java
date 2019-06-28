package uk.gov.hmcts.reform.fpl.config;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.exceptions.UnknownLocalAuthorityDomainException;

import static org.assertj.core.api.Assertions.assertThat;

class LocalAuthorityCodeLookupConfigurationTest {

    private static final String LOCAL_AUTHORITY_EMAIL_DOMAIN = "example.gov.uk";
    private static final String LOCAL_AUTHORITY_CODE = "example";

    private static final String CONFIG = String.format("%s=>%s", LOCAL_AUTHORITY_EMAIL_DOMAIN, LOCAL_AUTHORITY_CODE);

    private LocalAuthorityCodeLookupConfiguration configuration = new LocalAuthorityCodeLookupConfiguration(CONFIG);

    @Test
    void shouldThrowNullPointerExceptionUponInitialisationWhenMappingValueIsEmpty() {
        Assertions.assertThatThrownBy(() -> new LocalAuthorityCodeLookupConfiguration(CONFIG + ";fake=>"))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("Mapping value cannot be empty");
    }

    @Test
    void shouldThrowNullPointerExceptionWhenLocalAuthorityEmailDomainIsNull() {
        Assertions.assertThatThrownBy(() -> configuration.getLocalAuthorityCode(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("EmailAddress domain cannot be null");
    }

    @Test
    void shouldThrowUnknownLocalAuthorityDomainExceptionWhenLocalAuthorityEmailDomainDoesNotExist() {
        Assertions.assertThatThrownBy(() -> configuration.getLocalAuthorityCode("fake@example.com"))
            .isInstanceOf(UnknownLocalAuthorityDomainException.class)
            .hasMessage("fake@example.com not found");
    }

    @Test
    void shouldReturnLocalAuthorityCodeWhenLocalAuthorityEmailDomainExists() {
        String localAuthorityCode = configuration.getLocalAuthorityCode(LOCAL_AUTHORITY_EMAIL_DOMAIN);

        assertThat(localAuthorityCode).isEqualTo(LOCAL_AUTHORITY_CODE);
    }

}
