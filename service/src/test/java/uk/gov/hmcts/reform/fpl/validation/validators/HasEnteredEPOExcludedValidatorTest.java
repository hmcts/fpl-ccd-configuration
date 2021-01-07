package uk.gov.hmcts.reform.fpl.validation.validators;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.validation.AbstractValidationTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.config.utils.EmergencyProtectionOrderDirectionsType.EXCLUSION_REQUIREMENT;
import static uk.gov.hmcts.reform.fpl.enums.EPOType.REMOVE_TO_ACCOMMODATION;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class, FeatureToggleService.class})
class HasEnteredEPOExcludedValidatorTest extends AbstractValidationTest {

    @MockBean
    private FeatureToggleService featureToggleService;

    @Test
    void shouldNotReturnAnErrorWhenWhoIsExcludedIsEnteredAndToggledOn() {
        given(featureToggleService.isEpoOrderTypeAndExclusionEnabled()).willReturn(true);

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
    void shouldReturnAnErrorWhenWhoIsExcludedIsNotEnteredAndToggledOn() {
        given(featureToggleService.isEpoOrderTypeAndExclusionEnabled()).willReturn(true);

        Orders orders = Orders.builder()
            .epoType(REMOVE_TO_ACCOMMODATION)
            .orderType(List.of(OrderType.EMERGENCY_PROTECTION_ORDER))
            .emergencyProtectionOrderDirections(List.of(EXCLUSION_REQUIREMENT))
            .build();

        List<String> errorMessages = validate(orders);

        assertThat(errorMessages).contains("Enter who you want excluded.");
    }

    @Test
    void shouldNotReturnAnyErrorsWhenEpoOrderTypeAndExclusionIsToggledOff() {
        given(featureToggleService.isEpoOrderTypeAndExclusionEnabled()).willReturn(false);

        Orders orders = Orders.builder()
            .epoType(REMOVE_TO_ACCOMMODATION)
            .orderType(List.of(OrderType.EMERGENCY_PROTECTION_ORDER))
            .emergencyProtectionOrderDirections(List.of(EXCLUSION_REQUIREMENT))
            .build();

        List<String> errorMessages = validate(orders);

        assertThat(errorMessages).isEmpty();
    }
}
