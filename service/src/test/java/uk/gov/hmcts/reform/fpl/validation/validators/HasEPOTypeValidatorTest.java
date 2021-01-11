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
import static uk.gov.hmcts.reform.fpl.enums.EPOType.REMOVE_TO_ACCOMMODATION;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class, FeatureToggleService.class})
class HasEPOTypeValidatorTest extends AbstractValidationTest {

    @MockBean
    private FeatureToggleService featureToggleService;

    @Test
    void shouldNotReturnAnErrorWhenEpoOrderAndEpoTypeSelectedAndToggledOn() {
        given(featureToggleService.isEpoOrderTypeAndExclusionEnabled()).willReturn(true);

        Orders orders = Orders.builder()
            .epoType(REMOVE_TO_ACCOMMODATION)
            .orderType(List.of(OrderType.EMERGENCY_PROTECTION_ORDER))
            .build();

        List<String> errorMessages = validate(orders);

        assertThat(errorMessages).isEmpty();
    }

    @Test
    void shouldReturnAnErrorWhenEpoOrderSelectedWithoutEPOTypeAndToggledOn() {
        given(featureToggleService.isEpoOrderTypeAndExclusionEnabled()).willReturn(true);

        Orders orders = Orders.builder()
            .orderType(List.of(OrderType.EMERGENCY_PROTECTION_ORDER))
            .build();

        List<String> errorMessages = validate(orders);

        assertThat(errorMessages).contains("Select the type of EPO you need.");
    }

    @Test
    void shouldNotReturnAnyErrorsWhenEpoOrderIsNotSelectedAndToggledOn() {
        given(featureToggleService.isEpoOrderTypeAndExclusionEnabled()).willReturn(true);

        Orders orders = Orders.builder()
            .orderType(List.of(OrderType.CARE_ORDER))
            .build();

        List<String> errorMessages = validate(orders);

        assertThat(errorMessages).isEmpty();
    }

    @Test
    void shouldNotReturnAnyErrorsWhenEpoExclusionIsToggledOff() {
        given(featureToggleService.isEpoOrderTypeAndExclusionEnabled()).willReturn(false);

        Orders orders = Orders.builder()
            .orderType(List.of(OrderType.EMERGENCY_PROTECTION_ORDER))
            .build();

        List<String> errorMessages = validate(orders);

        assertThat(errorMessages).isEmpty();
    }
}
