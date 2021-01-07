package uk.gov.hmcts.reform.fpl.service.validators;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {OrdersSoughtChecker.class, LocalValidatorFactoryBean.class})
class OrdersSoughtCheckerTest {

    @Autowired
    private OrdersSoughtChecker ordersSoughtChecker;

    @MockBean
    private FeatureToggleService featureToggleService;

    @Test
    void shouldReturnErrorWhenNoNeededOrders() {
        final CaseData caseData = CaseData.builder().build();

        final List<String> errors = ordersSoughtChecker.validate(caseData);
        final boolean isCompleted = ordersSoughtChecker.isCompleted(caseData);

        assertThat(errors).containsExactly("Add the orders and directions sought");
        assertThat(isCompleted).isFalse();
    }

    @Test
    void shouldReturnErrorWhenNoNeededOrdersSelected() {
        final Orders orders = Orders.builder()
            .orderType(emptyList())
            .build();
        final CaseData caseData = CaseData.builder()
                .orders(orders)
                .build();

        final List<String> errors = ordersSoughtChecker.validate(caseData);
        final boolean isCompleted = ordersSoughtChecker.isCompleted(caseData);

        assertThat(errors).containsExactly("Select at least one type of order");
        assertThat(isCompleted).isFalse();
    }

    @Test
    void shouldReturnEmptyErrorsWhenSpecifiedWhichOrdersAreNeeded() {
        final Orders orders = Orders.builder()
                .orderType(List.of(OrderType.CARE_ORDER))
                .build();
        final CaseData caseData = CaseData.builder()
                .orders(orders)
                .build();

        final List<String> errors = ordersSoughtChecker.validate(caseData);
        final boolean isCompleted = ordersSoughtChecker.isCompleted(caseData);

        assertThat(errors).isEmpty();
        assertThat(isCompleted).isTrue();
    }
}
