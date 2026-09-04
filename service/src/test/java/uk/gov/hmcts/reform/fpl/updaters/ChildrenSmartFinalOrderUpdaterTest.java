package uk.gov.hmcts.reform.fpl.updaters;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.model.order.OrderTempQuestions;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;
import uk.gov.hmcts.reform.fpl.service.ChildrenService;
import uk.gov.hmcts.reform.fpl.utils.ChildSelectionUtils;

import java.util.List;
import java.util.UUID;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ChildrenTestHelper.buildPairsFromChildrenList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.buildDynamicList;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testChildren;

class ChildrenSmartFinalOrderUpdaterTest {

    private static final UUID childId1 = UUID.randomUUID();
    private static final UUID childId2 = UUID.randomUUID();
    private static final UUID childId3 = UUID.randomUUID();
    private static final UUID childId4 = UUID.randomUUID();
    private static final UUID childId5 = UUID.randomUUID();

    private static final DynamicMultiSelectListElement selectedChildEle1 = DynamicMultiSelectListElement.builder()
        .code(childId1.toString()).label("first1 last1").build();
    private static final DynamicMultiSelectListElement selectedChildEle2 = DynamicMultiSelectListElement.builder()
        .code(childId2.toString()).label("first2 last2").build();
    private static final DynamicMultiSelectListElement selectedChildEle3 = DynamicMultiSelectListElement.builder()
        .code(childId3.toString()).label("first3 last3").build();
    private static final DynamicMultiSelectListElement childEle1 = DynamicMultiSelectListElement.builder()
        .code(childId4.toString()).label("first4 last4").build();
    private static final DynamicMultiSelectListElement childEle2 = DynamicMultiSelectListElement.builder()
        .code(childId5.toString()).label("first5 last5").build();

    ChildrenSmartFinalOrderUpdater underTest =
        new ChildrenSmartFinalOrderUpdater(new ChildSelectionUtils(), new ChildrenService());

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

        List<Element<Child>> result = underTest.updateFinalOrderIssued(caseData);

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

        List<Element<Child>> result = underTest.updateFinalOrderIssued(caseData);

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

        List<Element<Child>> result = underTest.updateFinalOrderIssued(caseData);

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

        List<Element<Child>> result = underTest.updateFinalOrderIssued(caseData);

        assertThat(result).extracting(element -> element.getValue().getFinalOrderIssued())
            .containsExactly("Yes", "Yes", "Yes");

        assertThat(result).extracting(element -> element.getValue().getFinalOrderIssuedType())
            .containsExactly("Care order", "Supervision order", "Care order");
    }

    @Test
    void shouldUpdateFinalOrderIssuedWhenAppliesToSingleSelectedChild() {
        List<Element<Child>> children = List.of(childWithFinalOrderIssued(),
            childWithoutFinalOrderIssued(),
            childWithFinalOrderIssued());

        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersType(Order.C35A_SUPERVISION_ORDER)
                .whichChildIsTheOrderFor(buildDynamicList(1, buildPairsFromChildrenList(children)))
                .orderTempQuestions(OrderTempQuestions.builder().selectSingleChild("YES").build())
                .build())
            .children1(children)
            .build();

        List<Element<Child>> result = underTest.updateFinalOrderIssued(caseData);

        assertThat(result).extracting(element -> element.getValue().getFinalOrderIssued())
            .containsExactly("Yes", "Yes", "Yes");

        assertThat(result).extracting(element -> element.getValue().getFinalOrderIssuedType())
            .containsExactly("Care order", "Supervision order", "Care order");
    }

    @Test
    void shouldUpdateFinalOrderIssuedWhenAppliesToSelectedChildrenAndOneRemainingChildDMSL() {
        List<Element<Child>> children = List.of(
            childForFinalOrderIssuedDynamicMultiSelectList("first1", "last1",
                CARE_ORDER, childId1),
            childForFinalOrderIssuedDynamicMultiSelectList("first4", "last4",
                null, childId4),
            childForFinalOrderIssuedDynamicMultiSelectList("first2", "last2",
                CARE_ORDER, childId2));

        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersType(Order.C35A_SUPERVISION_ORDER)
                .build())
            .children1(children)
            .orderAppliesToAllChildren("No")
            .childSelectorV2(
                DynamicMultiSelectList.builder()
                    .value(List.of(childEle1))
                    .listItems(List.of(selectedChildEle1, childEle1, selectedChildEle2))
                    .build())
            .build();

        List<Element<Child>> result = underTest.updateFinalOrderIssued(caseData);

        assertThat(result).extracting(element -> element.getValue().getFinalOrderIssued())
            .containsExactly("Yes", "Yes", "Yes");

        assertThat(result).extracting(element -> element.getValue().getFinalOrderIssuedType())
            .containsExactly("Care order", "Supervision order", "Care order");
    }

    @Test
    void shouldUpdateFinalOrderIssuedWhenAppliesToSelectedChildrenAndAlreadyIssuedForOtherChildrenDMSL() {

        List<Element<Child>> children = List.of(
            childForFinalOrderIssuedDynamicMultiSelectList("first1", "last1", CARE_ORDER, childId1),
            childForFinalOrderIssuedDynamicMultiSelectList("first2", "last2", null, childId2),
            childForFinalOrderIssuedDynamicMultiSelectList("first3", "last3", null, childId3),
            childForFinalOrderIssuedDynamicMultiSelectList("first4", "last4", null, childId4),
            childForFinalOrderIssuedDynamicMultiSelectList("first5", "last5", null, childId5)
        );

        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(manageOrdersEventData)
            .children1(children)
            .orderAppliesToAllChildren("No")
            .childSelectorV2(
                DynamicMultiSelectList.builder()
                    .value(List.of(selectedChildEle2, selectedChildEle1, selectedChildEle3))
                    .listItems(List.of(selectedChildEle1, selectedChildEle2, selectedChildEle3, childEle1, childEle2))
                    .build())
            .build();

        List<Element<Child>> result = underTest.updateFinalOrderIssued(caseData);

        assertThat(result).extracting(element -> element.getValue().getFinalOrderIssued())
            .containsExactly("Yes", "Yes", "Yes", "No", "No");

        assertThat(result).extracting(element -> element.getValue().getFinalOrderIssuedType())
            .containsExactly("Care order", "Care order", "Care order", null, null);
    }

    @Test
    void shouldUpdateFinalOrderIssuedWhenAppliesToSelectedChildrenDMSL() {
        List<Element<Child>> children = List.of(
            childForFinalOrderIssuedDynamicMultiSelectList("first1", "last1",
                null, childId1),
            childForFinalOrderIssuedDynamicMultiSelectList("first2", "last2",
                null, childId2),
            childForFinalOrderIssuedDynamicMultiSelectList("first3", "last3",
                null, childId3));

        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(manageOrdersEventData)
            .children1(children)
            .orderAppliesToAllChildren("No")
            .childSelectorV2(
                DynamicMultiSelectList.builder()
                    .value(List.of(selectedChildEle2))
                    .listItems(List.of(selectedChildEle1, selectedChildEle2, selectedChildEle3))
                    .build())
            .build();

        List<Element<Child>> result = underTest.updateFinalOrderIssued(caseData);

        assertThat(result).extracting(element -> element.getValue().getFinalOrderIssued())
            .containsExactly("No", "Yes", "No");

        assertThat(result).extracting(element -> element.getValue().getFinalOrderIssuedType())
            .containsExactly(null, "Care order", null);
    }

    private static Element<Child> childWithoutFinalOrderIssued() {
        return childWithFinalOrderIssued(randomAlphanumeric(10), randomAlphanumeric(10), null);
    }

    private static Element<Child> childWithFinalOrderIssued() {
        return childWithFinalOrderIssued(randomAlphanumeric(10), randomAlphanumeric(10), CARE_ORDER);
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

    // Use when need to support both ChildSelector and ChildSelectorV2
    private static Element<Child> childForFinalOrderIssuedDynamicMultiSelectList(String firstName,
                                                                                 String lastName,
                                                                                 GeneratedOrderType orderType,
                                                                                 UUID id) {
        Child child = Child.builder()
            .party(ChildParty.builder()
                .firstName(firstName)
                .lastName(lastName)
                .build())
            .finalOrderIssued(ofNullable(orderType).map(o -> YES).orElse(NO).getValue())
            .finalOrderIssuedType(ofNullable(orderType).map(GeneratedOrderType::getLabel).orElse(null))
            .build();

        return Element.<Child>builder()
            .id(id)
            .value(child)
            .build();
    }

}
