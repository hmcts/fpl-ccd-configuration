package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType;
import uk.gov.hmcts.reform.fpl.enums.UploadedOrderType;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.OrderTypeAndDocument;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.EmailAddress;
import uk.gov.hmcts.reform.fpl.model.common.Telephone;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.UUID.randomUUID;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testChild;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testChildren;

class ChildrenServiceTest {

    private final ChildrenService service = new ChildrenService();

    @Test
    void shouldBuildExpectedLabelWhenEmptyList() {
        String label = service.getChildrenLabel(List.of(), false);
        assertThat(label).isEqualTo("No children in the case");
    }

    @Test
    void shouldBuildExpectedLabelWhenPopulatedList() {
        String label = service.getChildrenLabel(List.of(childWithConfidentialFields(randomUUID())), false);
        assertThat(label).isEqualTo("Child 1: James\n");
    }

    @Test
    void shouldBuildExpectedLabelWhenPopulatedListAndFinalOrderIssuedOnChild() {
        List<Element<Child>> children = List.of(childWithConfidentialFields(randomUUID()),
            childWithFinalOrderIssued("Jack", "Hill"));
        String label = service.getChildrenLabel(children, true);
        assertThat(label).isEqualTo("Child 1: James\nChild 2: Jack Hill - Care order issued\n");
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldReturnFalseWhenListEmptyOrNull(List<Element<Child>> list) {
        boolean result = service.allChildrenHaveFinalOrder(list);

        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnFalseWhenAtLeastOneChildDoesNotHaveFinalOrder() {
        List<Element<Child>> children = List.of(childWithFinalOrderIssued(), childWithoutFinalOrderIssued());

        boolean result = service.allChildrenHaveFinalOrder(children);

        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnTrueWhenAllChildrenHaveFinalOrder() {
        List<Element<Child>> children = List.of(childWithFinalOrderIssued(), childWithFinalOrderIssued());

        boolean result = service.allChildrenHaveFinalOrder(children);

        assertThat(result).isTrue();
    }

    @Nested
    class UpdateFinalOrderIssued {
        ManageOrdersEventData manageOrdersEventData = ManageOrdersEventData.builder()
            .manageOrdersType(Order.C32A_CARE_ORDER)
            .build();

        @Test
        void shouldUpdateFinalOrderIssuedWhenAppliesToAllChildren() {
            CaseData caseData = CaseData.builder()
                .manageOrdersEventData(manageOrdersEventData)
                .children1(testChildren())
                .orderAppliesToAllChildren("Yes")
                .childSelector(null)
                .remainingChildIndex(null)
                .build();

            List<Element<Child>> result = service.updateFinalOrderIssued(caseData);

            assertThat(result).extracting(element -> element.getValue().getFinalOrderIssued())
                .containsExactly("Yes", "Yes", "Yes");

            assertThat(result).extracting(element -> element.getValue().getFinalOrderIssuedType())
                .containsExactly("Care order", "Care order", "Care order");
        }

        @Test
        void shouldUpdateFinalOrderIssuedWhenAppliesToSelectedChildren() {

            CaseData caseData = CaseData.builder()
                .manageOrdersEventData(manageOrdersEventData)
                .children1(testChildren())
                .orderAppliesToAllChildren("No")
                .childSelector(Selector.builder()
                    .count("1")
                    .selected(List.of(1))
                    .build())
                .remainingChildIndex(null)
                .build();

            List<Element<Child>> result = service.updateFinalOrderIssued(caseData);

            assertThat(result).extracting(element -> element.getValue().getFinalOrderIssued())
                .containsExactly("No", "Yes", "No");

            assertThat(result).extracting(element -> element.getValue().getFinalOrderIssuedType())
                .containsExactly(null, "Care order", null);
        }

        @Test
        void shouldUpdateFinalOrderIssuedWhenAppliesToSelectedChildrenAndAlreadyIssuedForOtherChildren() {

            List<Element<Child>> children = List.of(childWithoutFinalOrderIssued(),
                childWithFinalOrderIssued(),
                childWithoutFinalOrderIssued(), childWithoutFinalOrderIssued(), childWithoutFinalOrderIssued());

            Selector childSelector = Selector.builder()
                .count("5")
                .selected(List.of(0, 2))
                .build();

            CaseData caseData = CaseData.builder()
                .manageOrdersEventData(manageOrdersEventData)
                .children1(children)
                .orderAppliesToAllChildren("No")
                .childSelector(childSelector)
                .remainingChildIndex(null)
                .build();

            List<Element<Child>> result = service.updateFinalOrderIssued(caseData);

            assertThat(result).extracting(element -> element.getValue().getFinalOrderIssued())
                .containsExactly("Yes", "Yes", "Yes", "No", "No");

            assertThat(result).extracting(element -> element.getValue().getFinalOrderIssuedType())
                .containsExactly("Care order", "Care order", "Care order", null, null);
        }

        @Test
        void shouldUpdateFinalOrderIssuedWhenAppliesToSelectedChildrenAndOneRemainingChild() {
            List<Element<Child>> children = List.of(childWithFinalOrderIssued(),
                childWithoutFinalOrderIssued(),
                childWithFinalOrderIssued());

            CaseData caseData = CaseData.builder()
                .manageOrdersEventData(ManageOrdersEventData.builder()
                    .manageOrdersType(Order.C35A_SUPERVISION_ORDER)
                    .build())
                .children1(children)
                .orderAppliesToAllChildren("No")
                .childSelector(null)
                .remainingChildIndex("1")
                .build();

            List<Element<Child>> result = service.updateFinalOrderIssued(caseData);

            assertThat(result).extracting(element -> element.getValue().getFinalOrderIssued())
                .containsExactly("Yes", "Yes", "Yes");

            assertThat(result).extracting(element -> element.getValue().getFinalOrderIssuedType())
                .containsExactly("Care order", "Supervision order", "Care order");
        }
    }

    @Test
    void shouldGetRemainingChildIndexWhenOneRemainingChild() {
        List<Element<Child>> children = List.of(childWithFinalOrderIssued(),
            childWithoutFinalOrderIssued(),
            childWithFinalOrderIssued());

        Optional<Integer> result = service.getRemainingChildIndex(children);

        assertThat(result).contains(1);
    }

    @Test
    void shouldNotGetRemainingChildIndexWhenNoRemainingChild() {
        List<Element<Child>> children = List.of(childWithFinalOrderIssued(), childWithFinalOrderIssued());

        Optional<Integer> result = service.getRemainingChildIndex(children);

        assertThat(result).isNotPresent();
    }

    @Test
    void shouldNotGetRemainingChildIndexWhenMoreThanOneRemainingChild() {
        List<Element<Child>> children = List.of(childWithFinalOrderIssued(), childWithoutFinalOrderIssued(),
            childWithFinalOrderIssued(), childWithoutFinalOrderIssued());

        Optional<Integer> result = service.getRemainingChildIndex(children);

        assertThat(result).isNotPresent();
    }

    @Test
    void shouldReturnNameOfChildWhenAChildDoesNotHaveFinalOrderIssued() {
        List<Element<Child>> children = List.of(childWithFinalOrderIssued("Paul", "Chuckle"),
            childWithoutFinalOrderIssued("Barry", "Chuckle"));

        String childrenNames = service.getRemainingChildrenNames(children);

        assertThat(childrenNames).isEqualTo("Barry Chuckle");
    }

    @Test
    void shouldReturnEmptyStringWhenAllChildrenHaveFinalOrderIssued() {
        List<Element<Child>> children = List.of(childWithFinalOrderIssued("Paul", "Chuckle"),
            childWithFinalOrderIssued("Barry", "Chuckle"));

        String childrenNames = service.getRemainingChildrenNames(children);

        assertThat(childrenNames).isEmpty();
    }

    @Test
    void shouldReturnChildNameWhenChildHasFinalOrderIssued() {
        List<Element<Child>> children = List.of(childWithFinalOrderIssued("Paul", "Chuckle"),
            childWithoutFinalOrderIssued("Barry", "Chuckle"));

        String childrenNames = service.getFinalOrderIssuedChildrenNames(children);

        assertThat(childrenNames).isEqualTo("Paul Chuckle - Care order issued");
    }

    @Test
    void shouldReturnEmptyStringWhenNoChildrenHaveFinalOrderIssued() {
        List<Element<Child>> children = List.of(
            childWithoutFinalOrderIssued("Paul", "Chuckle"),
            childWithoutFinalOrderIssued("Barry", "Chuckle"));

        String childrenNames = service.getFinalOrderIssuedChildrenNames(children);

        assertThat(childrenNames).isEmpty();
    }

    @Nested
    class IndexesOfChildrenWithFinalOrderIssued {

        @Test
        void shouldReturnEmptyListWhenNoChildren() {
            CaseData caseData = CaseData.builder().build();

            List<Integer> indexes = service.getIndexesOfChildrenWithFinalOrderIssued(caseData);

            assertThat(indexes).isEmpty();
        }

        @Test
        void shouldReturnEmptyListWhenNoChildrenWithFinalOrderIssued() {
            CaseData caseData = CaseData.builder()
                .children1(List.of(childWithoutFinalOrderIssued(), childWithoutFinalOrderIssued()))
                .build();

            List<Integer> indexes = service.getIndexesOfChildrenWithFinalOrderIssued(caseData);

            assertThat(indexes).isEmpty();
        }

        @Test
        void shouldReturnIndexesOfChildrenWithFinalOrderIssued() {
            CaseData caseData = CaseData.builder()
                .children1(List.of(childWithFinalOrderIssued(), childWithoutFinalOrderIssued(),
                    childWithFinalOrderIssued()))
                .build();

            List<Integer> indexes = service.getIndexesOfChildrenWithFinalOrderIssued(caseData);

            assertThat(indexes).containsExactly(0, 2);
        }
    }

    @Nested
    class SelectedChildren {

        @Test
        void shouldReturnAllChildrenWhenOrderAppliesToAllChildren() {
            CaseData caseData = CaseData.builder()
                .children1(List.of(testChild(), testChild()))
                .orderAppliesToAllChildren("Yes")
                .build();

            List<Element<Child>> selectedChildren = service.getSelectedChildren(caseData);

            assertThat(selectedChildren).isEqualTo(caseData.getAllChildren());
        }

        @Test
        void shouldReturnAllChildrenWhenNoSpecifiedIfOrderApplyToAllChildren() {
            CaseData caseData = CaseData.builder()
                .children1(List.of(testChild(), testChild()))
                .orderAppliesToAllChildren(null)
                .build();

            List<Element<Child>> selectedChildren = service.getSelectedChildren(caseData);

            assertThat(selectedChildren).isEqualTo(caseData.getAllChildren());
        }

        @Test
        void shouldReturnSelectedChildrenOnly() {
            int selectedChild = 1;
            CaseData caseData = CaseData.builder()
                .children1(List.of(testChild(), testChild(), testChild()))
                .childSelector(Selector.builder().selected(List.of(selectedChild)).build())
                .orderAppliesToAllChildren("No")
                .build();

            List<Element<Child>> selectedChildren = service.getSelectedChildren(caseData);

            assertThat(selectedChildren).containsExactly(caseData.getAllChildren().get(selectedChild));
        }

        @Test
        void shouldReturnEmptyListWhenNoChildrenSelected() {
            CaseData caseData = CaseData.builder()
                .children1(List.of(testChild(), testChild()))
                .childSelector(Selector.builder().selected(emptyList()).build())
                .orderAppliesToAllChildren("No")
                .build();

            List<Element<Child>> selectedChildren = service.getSelectedChildren(caseData);

            assertThat(selectedChildren).isEmpty();
        }
    }

    private Element<Child> childWithConfidentialFields(UUID id) {
        return element(id, Child.builder()
            .party(ChildParty.builder()
                .firstName("James")
                .detailsHidden("Yes")
                .email(EmailAddress.builder().email("email@email.com").build())
                .address(Address.builder().addressLine1("Address Line 1").build())
                .telephoneNumber(Telephone.builder().telephoneNumber("01227 831393").build())
                .build())
            .build());
    }

    private static Element<Child> childWithoutFinalOrderIssued(String firstName, String lastName) {
        return childWithFinalOrderIssued(firstName, lastName, null);
    }

    private static Element<Child> childWithoutFinalOrderIssued() {
        return childWithFinalOrderIssued(randomAlphanumeric(10), randomAlphanumeric(10), null);
    }

    private static Element<Child> childWithFinalOrderIssued() {
        return childWithFinalOrderIssued(randomAlphanumeric(10), randomAlphanumeric(10), CARE_ORDER);
    }

    private static Element<Child> childWithFinalOrderIssued(String firstName, String lastName) {
        return childWithFinalOrderIssued(firstName, lastName, CARE_ORDER);
    }

    private static Element<Child> childWithFinalOrderIssued(String firstName, String lastName,
                                                            GeneratedOrderType orderType) {
        return element(Child.builder()
            .party(ChildParty.builder()
                .firstName(firstName)
                .lastName(lastName)
                .build())
            .finalOrderIssued(ofNullable(orderType).map(o -> YES).orElse(NO).getValue())
            .finalOrderIssuedType(ofNullable(orderType).map(GeneratedOrderType::getLabel).orElse(null))
            .build());
    }

    private static OrderTypeAndDocument orderOfType(GeneratedOrderType type) {
        return orderOfType(type, null);
    }

    private static OrderTypeAndDocument orderOfType(GeneratedOrderType type, UploadedOrderType uploadedOrderType) {
        return OrderTypeAndDocument.builder()
            .type(type)
            .uploadedOrderType(uploadedOrderType)
            .build();
    }

}
