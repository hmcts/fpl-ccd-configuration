package uk.gov.hmcts.reform.fpl.validation.validators;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.validation.AbstractValidationTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.config.utils.EmergencyProtectionOrderDirectionsType.EXCLUSION_REQUIREMENT;
import static uk.gov.hmcts.reform.fpl.enums.EPOType.REMOVE_TO_ACCOMMODATION;

class HasEnteredEPOExcludedValidatorTest extends AbstractValidationTest {

    @Test
    void shouldNotReturnAnErrorWhenWhoIsExcludedIsEntered() {
        Orders orders = Orders.builder()
            .epoType(REMOVE_TO_ACCOMMODATION)
            .orderType(List.of(OrderType.EMERGENCY_PROTECTION_ORDER))
            .emergencyProtectionOrderDirections(List.of(EXCLUSION_REQUIREMENT))
            .excluded("John Doe")
            .build();

        List<String> errorMessages = validate(orders);

        assertThat(errorMessages).isEmpty();
    }

    @Test
    void shouldReturnAnErrorWhenWhoIsExcludedIsNotEntered() {
        Orders orders = Orders.builder()
            .epoType(REMOVE_TO_ACCOMMODATION)
            .orderType(List.of(OrderType.EMERGENCY_PROTECTION_ORDER))
            .emergencyProtectionOrderDirections(List.of(EXCLUSION_REQUIREMENT))
            .build();

        List<String> errorMessages = validate(orders);

        assertThat(errorMessages).contains("Enter who you want excluded.");
    }
}
