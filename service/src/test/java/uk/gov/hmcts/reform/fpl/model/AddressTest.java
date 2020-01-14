package uk.gov.hmcts.reform.fpl.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AddressTest {

    private Address.AddressBuilder builder;

    @BeforeEach
    void setup() {
        builder = Address.builder();
    }

    @Test
    void shouldFormatAddressAsStringWhenProvidePartiallyPopulatedAddress() {
        Address address = builder
            .addressLine1("1 Main Street")
            .addressLine2("Some town")
            .postcode("BT66 RPJ")
            .build();

        assertThat(address.getAddressAsString()).isEqualTo("1 Main Street, Some town, BT66 RPJ");
    }

    @Test
    void shouldFormatAddressAsStringWhenProvidedPopulatedAddress() {
        Address address = getPopulatedAddress();
        assertThat(address.getAddressAsString()).isEqualTo("Flat 1, Apartment block 2, Lurgan, BT66 7RR," +
            " Craigavon, Armagh, UK");
    }

    private Address getPopulatedAddress() {
        return builder
            .addressLine1("Flat 1")
            .addressLine2("Apartment block 2")
            .addressLine3("Lurgan")
            .postcode("BT66 7RR")
            .postTown("Craigavon")
            .county("Armagh")
            .country("UK")
            .build();
    }
}
