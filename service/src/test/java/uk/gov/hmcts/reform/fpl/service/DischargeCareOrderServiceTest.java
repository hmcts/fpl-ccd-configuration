package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testChild;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testChildren;

@SuppressWarnings("unchecked")
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
            GeneratedOrder order1 = order("Interim care order", "1 May 2019");
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
            GeneratedOrder order1 = order("Interim care order", "1 May 2019");
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
            Element<Child> child1 = testChild();
            Element<Child> child2 = testChild();
            Element<Child> child3 = testChild();
            Element<Child> child4 = testChild();

            GeneratedOrder order1 = order("Interim care order", "1 May 2019", child1);
            GeneratedOrder order2 = order("Final supervision order", "3 May 2019", child2);
            GeneratedOrder order3 = order("Final care order", "2 May 2019", child3, child4);

            List<Integer> selected = List.of(0);

            CaseData caseData = caseWithOrders(List.of(order1, order2, order3), selected);

            List<Element<Child>> actualChildren = dischargeCareOrderService.getChildrenInSelectedCareOrders(caseData);

            assertThat(actualChildren).containsExactly(child1);
        }

        @Test
        void shouldReturnAllSelectedCareOrdersChildren() {
            Element<Child> child1 = testChild();
            Element<Child> child2 = testChild();
            Element<Child> child3 = testChild();
            Element<Child> child4 = testChild();

            GeneratedOrder order1 = order("Interim care order", "1 May 2019", child1);
            GeneratedOrder order2 = order("Final supervision order", "3 May 2019", child2);
            GeneratedOrder order3 = order("Final care order", "2 May 2019", child3, child4);

            List<Integer> selected = List.of(0, 1);

            CaseData caseData = caseWithOrders(List.of(order1, order2, order3), selected);

            List<Element<Child>> actualChildren = dischargeCareOrderService.getChildrenInSelectedCareOrders(caseData);

            assertThat(actualChildren).containsExactly(child1, child3, child4);
        }

        @Test
        void shouldReturnAllCaseChildrenIfCareOrderDoesNotHaveChildren() {
            GeneratedOrder order = order("Interim care order", "1 May 2019");

            CaseData caseData = caseWithOrders(List.of(order), null);

            List<Element<Child>> actualChildren = dischargeCareOrderService.getChildrenInSelectedCareOrders(caseData);

            assertThat(actualChildren).isEqualTo(caseData.getAllChildren());
        }
    }

    @Nested
    class CareOrdersLabel {

        @Test
        void shouldReturnLabelForSingleCareOrder() {
            Element<Child> child = child("John", "Smith");

            GeneratedOrder order = order("Interim care order", "1 May 2019", child);

            String dischargeCareOrderLabel = dischargeCareOrderService.getOrdersLabel(List.of(order));

            assertThat(dischargeCareOrderLabel).isEqualTo("Create discharge of care order for John Smith");
        }

        @Test
        void shouldReturnLabelForMultipleCareOrders() {
            Element<Child> child1 = child("John", "Smith");
            Element<Child> child2 = child("Alex", "Green");
            Element<Child> child3 = child("Emma", "Johnson");
            Element<Child> child4 = child("James", "Black");

            GeneratedOrder order1 = order("Interim care order", "1 May 2019", child1);
            GeneratedOrder order2 = order("Final care order", "2 May 2019", child2, child3, child4);

            String dischargeCareOrderLabel = dischargeCareOrderService.getOrdersLabel(List.of(order1, order2));

            assertThat(dischargeCareOrderLabel).isEqualTo("Order 1: John Smith, 1 May 2019\n"
                + "Order 2: Alex Green and Emma Johnson and James Black, 2 May 2019");
        }
    }

    private static GeneratedOrder order(String type, String issueDate, Element<Child>... children) {
        return GeneratedOrder.builder()
            .type(type)
            .dateOfIssue(issueDate)
            .children(Arrays.asList(children))
            .build();
    }

    private static Element<Child> child(String firstName, String lastName) {
        return testChild(firstName, lastName, null, null);
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
