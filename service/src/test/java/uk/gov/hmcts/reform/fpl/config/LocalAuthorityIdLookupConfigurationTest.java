package uk.gov.hmcts.reform.fpl.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.reform.fpl.exceptions.UnknownLocalAuthorityException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalAuthorityIdLookupConfigurationTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = " ")
    void shouldLoadEmptyConfig(String config) {
        new LocalAuthorityIdLookupConfiguration(config);
    }

    @Test
    void shouldThrowExceptionWhenMappingKeyNotPresent() {
        assertThatThrownBy(() -> new LocalAuthorityIdLookupConfiguration("=>SA"))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("Mapping key cannot be empty");
    }

    @Test
    void shouldThrowExceptionWhenMappingValueNotPresent() {
        assertThatThrownBy(() -> new LocalAuthorityIdLookupConfiguration("A=>"))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("Mapping value cannot be empty");
    }

    @Test
    void shouldReturnLocalAuthorityId() {
        final LocalAuthorityIdLookupConfiguration underTest =
            new LocalAuthorityIdLookupConfiguration("SA=>ORG001;HN => ORG002");
        assertThat(underTest.getLocalAuthorityId("SA")).isEqualTo("ORG001");
        assertThat(underTest.getLocalAuthorityId("HN")).isEqualTo("ORG002");
    }

    @Test
    void shouldThrowExceptionWhenMappingDoesNotExists() {
        final LocalAuthorityIdLookupConfiguration underTest =
            new LocalAuthorityIdLookupConfiguration("SA=>ORG001;HN => ORG002");

        assertThatThrownBy(() -> underTest.getLocalAuthorityId("X"))
            .isInstanceOf(UnknownLocalAuthorityException.class)
            .hasMessage("Local authority with code X does not have id configured");
    }
}
