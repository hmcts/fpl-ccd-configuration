package uk.gov.hmcts.reform.fpl.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AddressTest {
    @Test
    void shouldFormatAddressAsStringWhenProvidePartiallyPopulatedAddress() {
        Address address = Address.builder()
            .addressLine1("1 Main Street")
            .addressLine2("Some town")
            .postcode("BT66 RPJ")
            .build();

        assertThat(address.getAddressAsString()).isEqualTo("1 Main Street, Some town, BT66 RPJ");
    }

    @Test
    void shouldFormatAddressAsStringWhenProvidedPopulatedAddress() {
        Address address = Address.builder()
            .addressLine1("Flat 1")
            .addressLine2("Apartment block 2")
            .addressLine3("Lurgan")
            .postTown("Craigavon")
            .county("Armagh")
            .postcode("BT66 7RR")
            .country("UK")
            .build();

        assertThat(address.getAddressAsString()).isEqualTo("Flat 1, Apartment block 2, Lurgan, Craigavon, Armagh,"
            + " BT66 7RR, UK");
    }
}
