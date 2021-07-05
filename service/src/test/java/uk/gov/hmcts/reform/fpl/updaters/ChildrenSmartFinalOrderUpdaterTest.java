package uk.gov.hmcts.reform.fpl.updaters;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.model.order.OrderTempQuestions;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;
import uk.gov.hmcts.reform.fpl.service.ChildrenService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.buildDynamicList;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testChildren;

class ChildrenSmartFinalOrderUpdaterTest {

    ChildrenSmartFinalOrderUpdater underTest = new ChildrenSmartFinalOrderUpdater(new ChildrenService());

    ManageOrdersEventData manageOrdersEventData = ManageOrdersEventData.builder()
        .manageOrdersType(Order.C32_CARE_ORDER)
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
        List<Pair<UUID, String>> dynamicListItems = children.stream()
            .map(childElement -> Pair.of(childElement.getId(), childElement.getValue().getParty().getFullName()))
            .collect(Collectors.toList());

        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersType(Order.C35A_SUPERVISION_ORDER)
                .whichChildIsTheOrderFor(buildDynamicList(1, dynamicListItems))
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

}
