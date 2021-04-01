package uk.gov.hmcts.reform.fpl.validation.validators;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.validation.AbstractValidationTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.EPOType.PREVENT_REMOVAL;
import static uk.gov.hmcts.reform.fpl.enums.EPOType.REMOVE_TO_ACCOMMODATION;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class, FeatureToggleService.class})
class HasEPOAddressValidatorTest extends AbstractValidationTest {

    @MockBean
    private FeatureToggleService featureToggleService;

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
