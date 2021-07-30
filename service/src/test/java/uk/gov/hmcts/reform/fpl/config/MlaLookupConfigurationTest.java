package uk.gov.hmcts.reform.fpl.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MlaLookupConfigurationTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = " ")
    void shouldLoadEmptyConfig(String config) {
        final MlaLookupConfiguration underTest = new MlaLookupConfiguration(config);
        assertThat(underTest.getLocalAuthorities("A")).isEmpty();
    }

    @Test
    void shouldReturnLocalAuthorities() {
        final MlaLookupConfiguration underTest = new MlaLookupConfiguration("A=>SA| CFG|RED;B => PLO| CDA");
        assertThat(underTest.getLocalAuthorities("A")).containsExactlyInAnyOrder("SA", "CFG", "RED");
        assertThat(underTest.getLocalAuthorities("B")).containsExactlyInAnyOrder("PLO", "CDA");
    }

    @Test
    void shouldReturnEmptyListIfRequestedOrgDoesNotHaveConfig() {
        final MlaLookupConfiguration underTest = new MlaLookupConfiguration("A=>SA");
        assertThat(underTest.getLocalAuthorities("B")).isEmpty();
    }

    @Test
    void shouldReturnEmptyListIfRequestedOrgIsNull() {
        final MlaLookupConfiguration underTest = new MlaLookupConfiguration("A=>SA");
        assertThat(underTest.getLocalAuthorities(null)).isEmpty();
    }

    @Test
    void shouldThrowExceptionWhenMappingKeyNotPresent() {
        assertThatThrownBy(() -> new MlaLookupConfiguration("=>SA"))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("Mapping key cannot be empty");
    }

    @Test
    void shouldThrowExceptionWhenMappingValueNotPresent() {
        assertThatThrownBy(() -> new MlaLookupConfiguration("A=>"))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("Mapping value cannot be empty");
    }

}
