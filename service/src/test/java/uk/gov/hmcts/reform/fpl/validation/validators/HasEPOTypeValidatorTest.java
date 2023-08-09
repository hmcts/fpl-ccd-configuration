package uk.gov.hmcts.reform.fpl.validation.validators;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.validation.AbstractValidationTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.EPOType.REMOVE_TO_ACCOMMODATION;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class})
class HasEPOTypeValidatorTest extends AbstractValidationTest {

    @Test
    void shouldNotReturnAnErrorWhenEpoOrderAndEpoTypeSelected() {
        Orders orders = Orders.builder()
            .epoType(REMOVE_TO_ACCOMMODATION)
            .orderType(List.of(OrderType.EMERGENCY_PROTECTION_ORDER))
            .build();

        List<String> errorMessages = validate(orders);

        assertThat(errorMessages).isEmpty();
    }

    @Test
    void shouldReturnAnErrorWhenEpoOrderSelectedWithoutEPOType() {
        Orders orders = Orders.builder()
            .orderType(List.of(OrderType.EMERGENCY_PROTECTION_ORDER))
            .build();

        List<String> errorMessages = validate(orders);

        assertThat(errorMessages).contains("Select the type of EPO you need.");
    }

    @Test
    void shouldNotReturnAnyErrorsWhenEpoOrderIsNotSelected() {
        Orders orders = Orders.builder()
            .orderType(List.of(OrderType.CARE_ORDER))
            .build();

        List<String> errorMessages = validate(orders);

        assertThat(errorMessages).isEmpty();
    }
}
