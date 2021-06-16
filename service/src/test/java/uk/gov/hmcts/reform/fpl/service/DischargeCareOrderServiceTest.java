package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testChildParty;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testChildren;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testEmail;

@ExtendWith(SpringExtension.class)
class DischargeCareOrderServiceTest {

    private DischargeCareOrderService dischargeCareOrderService = new DischargeCareOrderService();

    @Nested
    class CareOrders {

        @Test
        void shouldReturnEmptyListOfCareOrders() {
            CaseData caseData = caseWithOrders(emptyList());

            List<GeneratedOrder> actualCareOrders = dischargeCareOrderService.getCareOrders(caseData);

            assertThat(actualCareOrders).isEmpty();
        }

        @Test
        void shouldReturnAllCareOrders() {
            GeneratedOrder order1 = manageOrder(Order.C32_CARE_ORDER.name(), "1 May 2019");
            GeneratedOrder order2 = order("Interim care order", "2 May 2019");
            GeneratedOrder order3 = order("Final care order", "2 May 2019");
            GeneratedOrder order4 = order("Final supervision order", "3 May 2019");
            GeneratedOrder order5 = order("Interim supervision order", "3 May 2019");
            GeneratedOrder order6 = order("Emergency protection order", "4 May 2019");
            GeneratedOrder order7 = order("Blank order", "4 May 2019");
            GeneratedOrder order8 = order("Discharge of care order", "4 May 2019");

            CaseData caseData = caseWithOrders(List.of(order1, order2, order3, order4, order5, order6, order7, order8));

            List<GeneratedOrder> actualCareOrders = dischargeCareOrderService.getCareOrders(caseData);

            assertThat(actualCareOrders).containsExactly(order1, order2, order3);
        }
    }

    @Nested
    class SelectedCareOrders {

        @Test
        void shouldReturnSelectedCareOrders() {
            GeneratedOrder order1 = manageOrder(Order.C32_CARE_ORDER.name(), "1 May 2019");
            GeneratedOrder order2 = order("Interim care order", "2 May 2019");
            GeneratedOrder order3 = order("Final supervision order", "3 May 2019");
            GeneratedOrder order4 = order("Final care order", "2 May 2019");

            List<Integer> selected = List.of(0, 2);

            CaseData caseData = caseWithOrders(List.of(order1, order2, order3, order4), selected);

            List<GeneratedOrder> actualCareOrders = dischargeCareOrderService.getSelectedCareOrders(caseData);

            assertThat(actualCareOrders).containsExactly(order1, order4);
        }

        @Test
        void shouldReturnEmptyListWhenNoCareOrderSelected() {
            GeneratedOrder order1 = order("Interim care order", "1 May 2019");
            GeneratedOrder order2 = order("Final care order", "2 May 2019");

            List<Integer> selected = emptyList();

            CaseData caseData = caseWithOrders(List.of(order1, order2), selected);

            List<GeneratedOrder> actualCareOrders = dischargeCareOrderService.getSelectedCareOrders(caseData);

            assertThat(actualCareOrders).isEmpty();
        }

        @Test
        void shouldReturnAllCareOrdersWhenNoSelectionPresent() {
            GeneratedOrder order1 = order("Interim care order", "1 May 2019");
            GeneratedOrder order2 = order("Blank order", "1 May 2019");
            GeneratedOrder order3 = order("Final care order", "3 May 2019");

            List<Integer> selected = null;

            CaseData caseData = caseWithOrders(List.of(order1, order2, order3), selected);

            List<GeneratedOrder> actualCareOrders = dischargeCareOrderService.getSelectedCareOrders(caseData);

            assertThat(actualCareOrders).containsExactly(order1, order3);
        }
    }

    @Nested
    class SelectedCareOrdersChildren {

        @Test
        void shouldReturnCareOrderChildren() {
            Child child1 = child(testChildParty());
            Child child2 = child(testChildParty());
            Child child3 = child(testChildParty());
            Child child4 = child(testChildParty());

            GeneratedOrder order1 = order("Interim care order", "1 May 2019", child1);
            GeneratedOrder order2 = order("Final supervision order", "3 May 2019", child2);
            GeneratedOrder order3 = order("Final care order", "2 May 2019", child3, child4);

            List<Integer> selected = List.of(0);

            CaseData caseData = caseWithOrders(List.of(order1, order2, order3), selected);

            List<Child> actualChildren = dischargeCareOrderService.getChildrenInSelectedCareOrders(caseData);

            assertThat(actualChildren).containsExactly(child1);
        }

        @Test
        void shouldReturnAllSelectedCareOrdersChildren() {
            Child child1 = child(testChildParty());
            Child child2 = child(testChildParty());
            Child child3 = child(testChildParty());
            Child child4 = child(testChildParty());
            Child child5 = child(testChildParty());

            GeneratedOrder order1 = order("Interim care order", "1 May 2019", child1);
            GeneratedOrder order2 = order("Final supervision order", "3 May 2019", child2);
            GeneratedOrder order3 = order("Final care order", "2 May 2019", child3, child4);
            GeneratedOrder order4 = manageOrder(Order.C32_CARE_ORDER.name(), "2 May 2019", child5);

            List<Integer> selected = List.of(0, 1, 2);

            CaseData caseData = caseWithOrders(List.of(order1, order2, order3, order4), selected);

            List<Child> actualChildren = dischargeCareOrderService.getChildrenInSelectedCareOrders(caseData);

            assertThat(actualChildren).containsExactly(child1, child3, child4, child5);
        }

        @Test
        void shouldReturnChildrenWithUniqueNameAndDob() {
            ChildParty.ChildPartyBuilder party = ChildParty.builder()
                .firstName("Alex")
                .lastName("Smith")
                .dateOfBirth(LocalDate.now().minusYears(10));

            Child child1 = child(party.build());
            Child child2 = child(party.firstName("Alexander").build());
            Child child3 = child(party.lastName("Smithy").build());
            Child child4 = child(party.dateOfBirth(LocalDate.now().minusYears(8)).build());

            Child child5 = child(party.fathersName("Green").build());
            Child child6 = child(party.email(testEmail()).build());

            GeneratedOrder order1 = order("Interim care order", "1 May 2019", child1);
            GeneratedOrder order2 = order("Interim care order", "2 May 2019", child2);
            GeneratedOrder order3 = order("Interim care order", "3 May 2019", child3);
            GeneratedOrder order4 = order("Interim care order", "4 May 2019", child4);
            GeneratedOrder order5 = order("Interim care order", "5 May 2019", child5);
            GeneratedOrder order6 = order("Interim care order", "6 May 2019", child6);

            List<Integer> selected = List.of(0, 1, 2, 3, 4, 5);

            CaseData caseData = caseWithOrders(List.of(order1, order2, order3, order4, order5, order6), selected);

            List<Child> actualChildren = dischargeCareOrderService.getChildrenInSelectedCareOrders(caseData);

            assertThat(actualChildren).containsExactly(child1, child2, child3, child4);
        }

        @Test
        void shouldReturnAllCaseChildrenIfCareOrderDoesNotHaveChildren() {
            GeneratedOrder order = order("Interim care order", "1 May 2019");

            CaseData caseData = caseWithOrders(List.of(order), null);

            List<Child> actualChildren = dischargeCareOrderService.getChildrenInSelectedCareOrders(caseData);

            assertThat(actualChildren).isEqualTo(unwrapElements(caseData.getAllChildren()));
        }
    }

    @Nested
    class CareOrdersLabel {

        @Test
        void shouldReturnLabelForSingleCareOrder() {
            Child child = child("John", "Smith");

            GeneratedOrder order = order("Interim care order", "1 May 2019", child);

            String dischargeCareOrderLabel = dischargeCareOrderService.getOrdersLabel(List.of(order));

            assertThat(dischargeCareOrderLabel).isEqualTo("Create discharge of care order for John Smith");
        }

        @Test
        void shouldReturnLabelForMultipleCareOrders() {
            Child child1 = child("John", "Smith");
            Child child2 = child("Alex", "Green");
            Child child3 = child("Emma", "Johnson");
            Child child4 = child("James", "Black");

            GeneratedOrder order1 = order("Interim care order", "1 May 2019", child1);
            GeneratedOrder order2 = order("Final care order", "2 May 2019", child2, child3, child4);

            String dischargeCareOrderLabel = dischargeCareOrderService.getOrdersLabel(List.of(order1, order2));

            assertThat(dischargeCareOrderLabel).isEqualTo("Order 1: John Smith, 1 May 2019\n"
                + "Order 2: Alex Green and Emma Johnson and James Black, 2 May 2019");
        }

        @Test
        void shouldReturnLabelForApprovalDateWhenPresent() {
            Child child1 = child(testChildParty());
            Child child2 = child(testChildParty());

            GeneratedOrder order1 = GeneratedOrder.builder()
                .orderType(Order.C32_CARE_ORDER.name())
                .approvalDate(LocalDate.of(2019, 5, 1))
                .children(wrapElements(child1))
                .build();

            GeneratedOrder order2 = GeneratedOrder.builder()
                .orderType(Order.C32B_DISCHARGE_OF_CARE_ORDER.name())
                .approvalDate(LocalDate.of(2019, 5, 1))
                .children(wrapElements(child2))
                .build();

            String dischargeCareOrderLabel = dischargeCareOrderService.getOrdersLabel(List.of(order1, order2));


            assertThat(dischargeCareOrderLabel).contains("1st May 2019");
        }
    }

    private static GeneratedOrder order(String type, String issueDate, Child... children) {
        return GeneratedOrder.builder()
            .type(type)
            .dateOfIssue(issueDate)
            .children(wrapElements(children))
            .build();
    }

    private static GeneratedOrder manageOrder(String orderType, String approvalDate, Child... children) {
        return GeneratedOrder.builder()
            .orderType(orderType)
            .dateOfIssue(approvalDate)
            .children(wrapElements(children))
            .build();
    }

    private static Child child(String firstName, String lastName) {
        return child(testChildParty(firstName, lastName, null, null));
    }

    private static Child child(ChildParty childParty) {
        return Child.builder().party(childParty).build();
    }

    private static CaseData caseWithOrders(List<GeneratedOrder> orders) {
        return CaseData.builder()
            .orderCollection(orders.stream().map(ElementUtils::element).collect(Collectors.toList()))
            .build();
    }

    private static CaseData caseWithOrders(List<GeneratedOrder> orders, List<Integer> selectedCareOrders) {
        return CaseData.builder()
            .orderCollection(orders.stream().map(ElementUtils::element).collect(Collectors.toList()))
            .children1(testChildren())
            .careOrderSelector(ofNullable(selectedCareOrders)
                .map(selected -> Selector.builder().selected(selected).build()).orElse(null))
            .build();
    }
}
