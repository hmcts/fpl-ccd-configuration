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
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;

import java.util.Collections;
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
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testChild;

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
        boolean result = service.allChildrenHaveFinalOrderOrDecision(list);

        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnFalseWhenAtLeastOneChildDoesNotHaveFinalOrder() {
        List<Element<Child>> children = List.of(childWithFinalOrderIssued(), childWithoutFinalOrderIssued());

        boolean result = service.allChildrenHaveFinalOrderOrDecision(children);

        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnFalseWhenAtLeastOneChildDoesNotHaveFinalDecision() {
        List<Element<Child>> children = List.of(childWithFinalDecision(), childWithoutFinalDecision());

        boolean result = service.allChildrenHaveFinalOrderOrDecision(children);

        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnTrueWhenAllChildrenHaveFinalOrder() {
        List<Element<Child>> children = List.of(childWithFinalOrderIssued(), childWithFinalOrderIssued());

        boolean result = service.allChildrenHaveFinalOrderOrDecision(children);

        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnTrueWhenAllChildrenHaveFinalDecision() {
        List<Element<Child>> children = List.of(childWithFinalDecision(), childWithFinalDecision());

        boolean result = service.allChildrenHaveFinalOrderOrDecision(children);

        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnTrueWhenAllChildrenHaveFinalOrderOrFinalDecision() {
        List<Element<Child>> children = List.of(childWithFinalDecision(), childWithFinalDecision());

        boolean result = service.allChildrenHaveFinalOrderOrDecision(children);

        assertThat(result).isTrue();
    }

    @Test
    void shouldGetRemainingChildIndexWhenOneRemainingChild() {
        List<Element<Child>> children = List.of(childWithFinalOrderIssued(),
            childWithoutFinalOrderIssued(),
            childWithFinalDecision());

        Optional<Integer> result = service.getRemainingChildIndex(children);

        assertThat(result).contains(1);
    }

    @Test
    void shouldNotGetRemainingChildIndexWhenNoRemainingChild() {
        List<Element<Child>> children = List.of(childWithFinalOrderIssued(), childWithFinalDecision());

        Optional<Integer> result = service.getRemainingChildIndex(children);

        assertThat(result).isNotPresent();
    }

    @Test
    void shouldNotGetRemainingChildIndexWhenMoreThanOneRemainingChild() {
        List<Element<Child>> children = List.of(childWithFinalOrderIssued(), childWithoutFinalOrderIssued(),
            childWithFinalDecision(), childWithoutFinalDecision());

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
    void shouldReturnNameOfChildWhenAChildDoesNotHaveFinalDecisionIssued() {
        List<Element<Child>> children = List.of(childWithFinalDecision("Paul", "Chuckle", "Reason"),
            childWithoutFinalDecision("Barry", "Chuckle"));

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
    void shouldReturnEmptyStringWhenAllChildrenHaveFinalDecision() {
        List<Element<Child>> children = List.of(childWithFinalDecision("Paul", "Chuckle", "Reason"),
            childWithFinalDecision("Barry", "Chuckle", "Reason"));

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

    @Test
    void shouldReturnTrueWhenAddressChange() {
        List<Element<Child>> childrenBefore = wrapElements(Child.builder()
            .party(ChildParty.builder().address(Address.builder().addressLine1("33 Testing Court")
                .addressLine2("Testing").postcode("XX1 BBB").build()).build()).build());
        List<Element<Child>> childrenAfter = wrapElements(Child.builder()
            .party(ChildParty.builder().address(Address.builder().addressLine1("90 Testing Court")
                .addressLine2("Testing").postcode("KK1 BBB").build()).build()).build());
        assertThat(service.hasAddressChange(Collections.unmodifiableList(childrenAfter),
            Collections.unmodifiableList(childrenBefore))).isTrue();
    }

    @Test
    void shouldReturnFalseWhenAddressNoChange() {
        List<Element<Child>> childrenBefore = wrapElements(Child.builder()
            .party(ChildParty.builder().address(Address.builder().addressLine1("33 Testing Court")
                .addressLine2("Testing").postcode("XX1 BBB").build()).build()).build());
        List<Element<Child>> childrenAfter = wrapElements(Child.builder()
            .party(ChildParty.builder().address(Address.builder().addressLine1("33 Testing Court")
                .addressLine2("Testing").postcode("XX1 BBB").build()).build()).build());
        assertThat(service.hasAddressChange(Collections.unmodifiableList(childrenAfter),
            Collections.unmodifiableList(childrenBefore))).isFalse();
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
                .isAddressConfidential("Yes")
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

    private static Element<Child> childWithoutFinalDecision(String firstName, String lastName) {
        return childWithFinalDecision(firstName, lastName, null);
    }

    private static Element<Child> childWithoutFinalDecision() {
        return childWithFinalDecision(randomAlphanumeric(10), randomAlphanumeric(10), null);
    }

    private static Element<Child> childWithFinalDecision() {
        return childWithFinalDecision(randomAlphanumeric(10), randomAlphanumeric(10), "reason");
    }

    private static Element<Child> childWithFinalDecision(String firstName, String lastName, String reason) {
        return element(Child.builder()
            .party(ChildParty.builder()
                .firstName(firstName)
                .lastName(lastName)
                .build())
            .finalDecisionDate("1 Jan 2021")
            .finalDecisionReason(reason)
            .build());
    }

    private static OrderTypeAndDocument orderOfType(GeneratedOrderType type, UploadedOrderType uploadedOrderType) {
        return OrderTypeAndDocument.builder()
            .type(type)
            .uploadedOrderType(uploadedOrderType)
            .build();
    }

}
