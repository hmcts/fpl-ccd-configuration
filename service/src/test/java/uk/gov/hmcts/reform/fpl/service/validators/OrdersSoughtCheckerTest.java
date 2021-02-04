package uk.gov.hmcts.reform.fpl.service.validators;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import uk.gov.hmcts.reform.fpl.config.utils.EmergencyProtectionOrderDirectionsType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.config.utils.EmergencyProtectionOrderDirectionsType.EXCLUSION_REQUIREMENT;
import static uk.gov.hmcts.reform.fpl.enums.EPOType.REMOVE_TO_ACCOMMODATION;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.EMERGENCY_PROTECTION_ORDER;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {OrdersSoughtChecker.class, LocalValidatorFactoryBean.class})
class OrdersSoughtCheckerTest {

    @Autowired
    private OrdersSoughtChecker ordersSoughtChecker;

    @MockBean
    private FeatureToggleService featureToggleService;

    @ParameterizedTest
    @MethodSource("incompleteOrders")
    void shouldReturnEmptyErrorsAndNonCompletedState(Orders orders) {
        final CaseData caseData = CaseData.builder().orders(orders).build();

        final List<String> errors = ordersSoughtChecker.validate(caseData);
        final boolean isCompleted = ordersSoughtChecker.isCompleted(caseData);

        assertThat(errors).isEmpty();
        assertThat(isCompleted).isFalse();
    }

    @ParameterizedTest
    @MethodSource("completeOrders")
    void shouldReturnEmptyErrorsAndCompletedState(Orders orders) {
        final CaseData caseData = CaseData.builder().orders(orders).build();

        final List<String> errors = ordersSoughtChecker.validate(caseData);
        final boolean isCompleted = ordersSoughtChecker.isCompleted(caseData);

        assertThat(errors).isEmpty();
        assertThat(isCompleted).isTrue();
    }

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
            .orderType(List.of(CARE_ORDER))
            .build();
        final CaseData caseData = CaseData.builder()
            .orders(orders)
            .build();

        final List<String> errors = ordersSoughtChecker.validate(caseData);
        final boolean isCompleted = ordersSoughtChecker.isCompleted(caseData);

        assertThat(errors).isEmpty();
        assertThat(isCompleted).isFalse();
    }

    private static Stream<Arguments> incompleteOrders() {
        return Stream.of(
            Orders.builder()
                .orderType(Collections.singletonList(CARE_ORDER))
                .directions("Yes")
                .build(),
            Orders.builder()
                .orderType(Collections.singletonList(EMERGENCY_PROTECTION_ORDER))
                .emergencyProtectionOrderDirections(Collections.singletonList(
                    EmergencyProtectionOrderDirectionsType.OTHER))
                .build(),
            Orders.builder()
                .orderType(Collections.singletonList(EMERGENCY_PROTECTION_ORDER))
                .emergencyProtectionOrderDirections(Collections.singletonList(
                    EXCLUSION_REQUIREMENT))
                .build()
            )
            .map(Arguments::of);
    }

    private static Stream<Arguments> completeOrders() {
        return Stream.of(
            Orders.builder()
                .orderType(Collections.singletonList(CARE_ORDER))
                .directions("No")
                .build(),
            Orders.builder()
                .orderType(Collections.singletonList(CARE_ORDER))
                .directions("Yes")
                .directionDetails("Test")
                .build(),
            Orders.builder()
                .orderType(Collections.singletonList(EMERGENCY_PROTECTION_ORDER))
                .epoType(REMOVE_TO_ACCOMMODATION)
                .emergencyProtectionOrderDirections(Arrays
                    .asList(EXCLUSION_REQUIREMENT, EmergencyProtectionOrderDirectionsType.OTHER))
                .excluded("Test")
                .emergencyProtectionOrderDetails("Test")
                .emergencyProtectionOrderDirectionDetails("Test")
                .otherOrder("Test")
                .directions("Yes")
                .directionDetails("Test")
                .build()
        )
            .map(Arguments::of);
    }
}
