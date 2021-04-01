package uk.gov.hmcts.reform.fpl.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.quality.Strictness.LENIENT;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
class RecipientTest {

    @Spy
    private Recipient recipient;

    @Test
    void shouldReturnTrueWhenRecipientNameIsNotBlankAndAddressHasAtLeastLine1AndPostcode() {
        when(recipient.getFullName()).thenReturn("Name");
        when(recipient.getAddress()).thenReturn(Address.builder()
            .addressLine1("L1")
            .postcode("PC 1")
            .build());

        assertThat(recipient.isDeliverable()).isTrue();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = " ")
    void shouldReturnFalseWhenRecipientNameIsBlank(String recipientName) {
        when(recipient.getFullName()).thenReturn(recipientName);
        when(recipient.getAddress()).thenReturn(Address.builder()
            .addressLine1("L1")
            .postcode("PC 1")
            .build());

        assertThat(recipient.isDeliverable()).isFalse();
    }

    @ParameterizedTest
    @NullSource
    @MethodSource("invalidAddresses")
    void shouldReturnFalseWhenAddressDoesNotHaveLine1AndPostcode(Address recipientAddress) {
        when(recipient.getFullName()).thenReturn("Test");
        when(recipient.getAddress()).thenReturn(recipientAddress);

        assertThat(recipient.isDeliverable()).isFalse();
    }

    private static Stream<Address> invalidAddresses() {
        return Stream.of(
            Address.builder().build(),
            Address.builder().addressLine1("L1").addressLine2("L2").addressLine3("L3").build(),
            Address.builder().postcode("PC 1").build(),
            Address.builder().addressLine2("L2").postcode("PC 1").build());
    }
}
