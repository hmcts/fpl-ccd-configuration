package uk.gov.hmcts.reform.fpl.service.validators;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import uk.gov.hmcts.reform.fpl.config.utils.EmergencyProtectionOrdersType;
import uk.gov.hmcts.reform.fpl.enums.EPOType;
import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.enums.SecureAccommodationOrderSection;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;

import java.util.List;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.config.utils.EmergencyProtectionOrderDirectionsType.EXCLUSION_REQUIREMENT;
import static uk.gov.hmcts.reform.fpl.config.utils.EmergencyProtectionOrderDirectionsType.OTHER;
import static uk.gov.hmcts.reform.fpl.enums.EPOType.REMOVE_TO_ACCOMMODATION;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.EMERGENCY_PROTECTION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.SECURE_ACCOMMODATION_ORDER;
import static uk.gov.hmcts.reform.fpl.model.tasklist.TaskState.COMPLETED_FINISHED;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {OrdersSoughtChecker.class, LocalValidatorFactoryBean.class})
class OrdersSoughtCheckerTest {

    @Autowired
    private OrdersSoughtChecker ordersSoughtChecker;

    @MockBean
    private FeatureToggleService featureToggleService;

    @Test
    void testCompletedState() {
        assertThat(ordersSoughtChecker.completedState()).isEqualTo(COMPLETED_FINISHED);
    }

    @Nested
    class Validate {

        @Test
        void shouldReturnErrorWhenNoNeededOrders() {
            final CaseData caseData = CaseData.builder().build();

            final List<String> errors = ordersSoughtChecker.validate(caseData);

            assertThat(errors).containsExactly("Add the orders and directions sought");
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

            assertThat(errors).containsExactly("Select at least one type of order");
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

            assertThat(errors).isEmpty();
        }

        @Test
        void shouldReturnErrorWhenSectionNotSelectedForSAO() {
            final Orders orders = Orders.builder()
                .orderType(List.of(SECURE_ACCOMMODATION_ORDER))
                .build();
            final CaseData caseData = CaseData.builder()
                .orders(orders)
                .build();

            final List<String> errors = ordersSoughtChecker.validate(caseData);

            assertThat(errors).containsExactly("Select under which section are you applying");
        }
    }

    @Nested
    class IsCompleted {

        @ParameterizedTest
        @NullSource
        @MethodSource("uk.gov.hmcts.reform.fpl.service.validators.OrdersSoughtCheckerTest#incompleteOrders")
        void shouldReturnEmptyErrorsAndNonCompletedState(Orders orders) {
            final CaseData caseData = CaseData.builder().orders(orders).build();

            final boolean isCompleted = ordersSoughtChecker.isCompleted(caseData);

            assertThat(isCompleted).isFalse();
        }

        @ParameterizedTest
        @MethodSource("uk.gov.hmcts.reform.fpl.service.validators.OrdersSoughtCheckerTest#completeOrders")
        void shouldReturnEmptyErrorsAndCompletedState(Orders orders) {
            final CaseData caseData = CaseData.builder().orders(orders).build();

            final boolean isCompleted = ordersSoughtChecker.isCompleted(caseData);

            assertThat(isCompleted).isTrue();
        }
    }

    private static Stream<Arguments> incompleteOrders() {
        return Stream.of(

            completedOrder()
                .orderType(null)
                .build(),
            completedOrder()
                .orderType(emptyList())
                .build(),
            completedOrder()
                .orderType(emptyList())
                .build(),

            completedOrder()
                .directions(null)
                .build(),
            completedOrder()
                .directions("")
                .build(),
            completedOrder()
                .directions("Yes")
                .directionDetails(null)
                .build(),
            completedOrder()
                .directions("Yes")
                .directionDetails("")
                .build(),
            completedOrder()
                .court(null)
                .build(),

            completedEPO()
                .epoType(null)
                .build(),
            completedEPO()
                .emergencyProtectionOrders(List.of(EmergencyProtectionOrdersType.OTHER))
                .emergencyProtectionOrderDetails(null)
                .build(),
            completedEPO()
                .emergencyProtectionOrders(List.of(EmergencyProtectionOrdersType.OTHER))
                .emergencyProtectionOrderDetails("")
                .build(),

            completedEPO()
                .emergencyProtectionOrderDirections(List.of(EXCLUSION_REQUIREMENT))
                .excluded(null)
                .build(),
            completedEPO()
                .emergencyProtectionOrderDirections(List.of(EXCLUSION_REQUIREMENT))
                .excluded("")
                .build(),

            completedEPO()
                .emergencyProtectionOrderDirections(List.of(OTHER))
                .emergencyProtectionOrderDirectionDetails(null)
                .build(),
            completedEPO()
                .emergencyProtectionOrderDirections(List.of(OTHER))
                .emergencyProtectionOrderDirectionDetails("")
                .build(),
            completedEPO()
                .epoType(EPOType.PREVENT_REMOVAL)
                .address(null)
                .build(),
            completedEPO()
                .epoType(EPOType.PREVENT_REMOVAL)
                .address(Address.builder().addressLine1("test address").build())
                .build(),
            completedEPO()
                .epoType(EPOType.PREVENT_REMOVAL)
                .address(Address.builder().postcode("test post code").build())
                .build(),

            completedOrder()
                .orderType(List.of(OrderType.OTHER))
                .otherOrder(null)
                .build(),
            completedOrder()
                .orderType(List.of(OrderType.OTHER))
                .otherOrder("")
                .build(),
            completedSAO()
                .secureAccommodationOrderSection(null)
                .build()
        ).map(Arguments::of);
    }

    private static Stream<Arguments> completeOrders() {
        return Stream.of(
            Orders.builder()
                .court("777")
                .orderType(singletonList(CARE_ORDER))
                .directions("No")
                .build(),
            Orders.builder()
                .court("777")
                .orderType(singletonList(CARE_ORDER))
                .directions("Yes")
                .directionDetails("Test")
                .build(),
            completedOrder()
                .build(),
            completedEPO()
                .build(),
            completedEPO()
                .epoType(EPOType.PREVENT_REMOVAL)
                .address(Address.builder().addressLine1("test address").postcode("test post code").build())
                .build(),
            completedSAO()
                .build()
        ).map(Arguments::of);
    }

    private static Orders.OrdersBuilder completedOrder() {
        return Orders.builder()
            .court("777")
            .orderType(singletonList(CARE_ORDER))
            .directions("Yes")
            .directionDetails("Test");
    }

    private static Orders.OrdersBuilder completedEPO() {
        return Orders.builder()
            .court("777")
            .orderType(List.of(EMERGENCY_PROTECTION_ORDER, OrderType.OTHER))
            .otherOrder("Test")
            .epoType(REMOVE_TO_ACCOMMODATION)
            .emergencyProtectionOrders(List.of(EmergencyProtectionOrdersType.OTHER))
            .emergencyProtectionOrderDirections(List.of(EXCLUSION_REQUIREMENT, OTHER))
            .excluded("Test")
            .emergencyProtectionOrderDetails("Test")
            .emergencyProtectionOrderDirectionDetails("Test")
            .directions("Yes")
            .directionDetails("Test");
    }

    private static Orders.OrdersBuilder completedSAO() {
        return Orders.builder()
            .court("777")
            .orderType(List.of(SECURE_ACCOMMODATION_ORDER))
            .secureAccommodationOrderSection(SecureAccommodationOrderSection.ENGLAND)
            .directions("Yes")
            .directionDetails("Test");
    }
}
