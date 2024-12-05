package uk.gov.hmcts.reform.fpl.config;

import org.junit.jupiter.api.Nested;
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
        assertThat(new LocalAuthorityIdLookupConfiguration(config))
            .isInstanceOf(LocalAuthorityIdLookupConfiguration.class);
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

    @Nested
    class LocalAuthorityCode {

        @Test
        void shouldReturnLocalAuthorityCode() {
            final LocalAuthorityIdLookupConfiguration underTest =
                new LocalAuthorityIdLookupConfiguration("SA=>ORG001;HN => ORG002");

            assertThat(underTest.getLocalAuthorityCode("ORG001")).contains("SA");
            assertThat(underTest.getLocalAuthorityCode("ORG002")).contains("HN");
        }

        @Test
        void shouldReturnEmptyLocalAuthorityCodeWhenMappingIsNotPresent() {
            final LocalAuthorityIdLookupConfiguration underTest =
                new LocalAuthorityIdLookupConfiguration("SA=>ORG001");

            assertThat(underTest.getLocalAuthorityCode("ORG002")).isEmpty();
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldReturnEmptyLocalAuthorityCodeOrganisationIdIsNullOrEmpty(String organisationId) {
            final LocalAuthorityIdLookupConfiguration underTest =
                new LocalAuthorityIdLookupConfiguration("SA=>ORG001");

            assertThat(underTest.getLocalAuthorityCode(organisationId)).isEmpty();
        }

        @Test
        void shouldThrowExceptionWhenMultipleLAsConfiguredForSameOrganisation() {
            final LocalAuthorityIdLookupConfiguration underTest =
                new LocalAuthorityIdLookupConfiguration("SA=>ORG001;HN=>ORG001");

            assertThatThrownBy(() -> underTest.getLocalAuthorityCode("ORG001"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Multiple local authorities [SA, HN] configured for organisation ORG001");
        }
    }
}
