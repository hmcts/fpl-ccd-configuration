package uk.gov.hmcts.reform.fpl.service.validators;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Orders;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {OrdersNeededValidator.class, LocalValidatorFactoryBean.class})
class OrdersNeededValidatorTest {

    @Autowired
    private OrdersNeededValidator ordersNeededValidator;

    @Test
    void shouldReturnErrorWhenNoNeededOrders() {
        final CaseData caseData = CaseData.builder().build();

        final List<String> errors = ordersNeededValidator.validate(caseData);

        assertThat(errors).containsExactly("You need to add details to orders and directions needed");
    }

    @Test
    void shouldReturnErrorWhenNoNeededOrdersSelected() {
        final Orders orders = Orders.builder().build();

        final CaseData caseData = CaseData.builder()
            .orders(orders)
            .build();

        final List<String> errors = ordersNeededValidator.validate(caseData);

        assertThat(errors).containsExactly("Select at least one type of order");
    }

    @Test
    void shouldReturnEmptyErrorsWhenSpecifiedWhichOrdersAreNeeded() {
        final Orders orders = Orders.builder()
            .orderType(List.of(OrderType.CARE_ORDER))
            .build();

        final CaseData caseData = CaseData.builder()
            .orders(orders)
            .build();

        final List<String> errors = ordersNeededValidator.validate(caseData);

        assertThat(errors).isEmpty();
    }
}
