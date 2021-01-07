package uk.gov.hmcts.reform.fpl.validation.validators;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.validation.AbstractValidationTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.EPOType.PREVENT_REMOVAL;
import static uk.gov.hmcts.reform.fpl.enums.EPOType.REMOVE_TO_ACCOMMODATION;

class HasEPOAddressValidatorTest extends AbstractValidationTest {

    @Test
    void shouldNotReturnAnErrorWhenEpoTypeIsRemoveToAccommodation() {
        Orders orders = Orders.builder()
            .epoType(REMOVE_TO_ACCOMMODATION)
            .orderType(List.of(OrderType.EMERGENCY_PROTECTION_ORDER))
            .build();

        List<String> errorMessages = validate(orders);

        assertThat(errorMessages).isEmpty();
    }

    @Test
    void shouldReturnAnErrorWhenAddressIsNotEntered() {
        Orders orders = Orders.builder()
            .epoType(PREVENT_REMOVAL)
            .orderType(List.of(OrderType.EMERGENCY_PROTECTION_ORDER))
            .address(Address.builder().build())
            .build();

        List<String> errorMessages = validate(orders);

        assertThat(errorMessages).contains("Enter the postcode and select the address.");
    }

    @Test
    void shouldReturnAnErrorWhenPostcodeIsNotEntered() {
        Orders orders = Orders.builder()
            .epoType(PREVENT_REMOVAL)
            .orderType(List.of(OrderType.EMERGENCY_PROTECTION_ORDER))
            .address(Address.builder()
                .addressLine1("45 Ethel Street")
                .build())
            .build();

        List<String> errorMessages = validate(orders);

        assertThat(errorMessages).contains("Enter the postcode and select the address.");
    }
}
