package uk.gov.hmcts.reform.fpl.service.orders.validator;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Orders;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class OrdersNeededValidatorTest {

    private final OrdersNeededValidator underTest = new OrdersNeededValidator();

    @Test
    void shouldPassValidationIfOnlySecureAccommodationOrderIsSelected() {
        CaseData caseData = CaseData.builder()
            .orders(Orders.builder()
                .orderType(List.of(OrderType.SECURE_ACCOMMODATION_ORDER))
                .build())
            .build();

        assertThat(underTest.validate(caseData)).isEmpty();
    }

    @Test
    void shouldFailValidationIfMoreThanOneOrdersAreSelectedWhenSecureAccommodationOrderIsSelected() {
        CaseData caseData = CaseData.builder()
            .orders(Orders.builder()
                .orderType(List.of(OrderType.SECURE_ACCOMMODATION_ORDER, OrderType.CARE_ORDER))
                .build())
            .build();

        assertThat(underTest.validate(caseData))
            .containsExactly("If secure accommodation order is selected, this should be the only order selected");
    }
}
