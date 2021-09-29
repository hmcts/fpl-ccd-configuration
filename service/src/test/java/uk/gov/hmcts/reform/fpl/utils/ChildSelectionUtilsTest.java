package uk.gov.hmcts.reform.fpl.utils;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.OrderTempQuestions;

import java.util.List;
import java.util.UUID;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.buildDynamicList;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testChild;

class ChildSelectionUtilsTest {

    private final ChildSelectionUtils underTest = new ChildSelectionUtils();

    @Test
    void shouldIdentifyCaseDataWhereOnlyOneChildCanBeSelected() {
        CaseData caseData = createCaseDataWithGivenOrderTempQuestions(
            OrderTempQuestions.builder()
                .selectSingleChild(YES.getValue())
                .build()
        );

        assertThat(underTest.canOnlyOneChildBeSelected(caseData)).isTrue();
    }

    @Test
    void shouldIdentifyCaseDataWhereMultipleChildrenCanBeSelected() {
        CaseData caseData = createCaseDataWithGivenOrderTempQuestions(
            OrderTempQuestions.builder()
                .selectSingleChild(NO.getValue())
                .build()
        );

        assertThat(underTest.canOnlyOneChildBeSelected(caseData)).isFalse();
    }

    @Test
    void shouldIdentifyCaseDataWhereChildIsSelectedByPlacement() {
        CaseData caseData = createCaseDataWithGivenOrderTempQuestions(
            OrderTempQuestions.builder()
                .childPlacementApplications(YES.getValue())
                .build()
        );

        assertThat(underTest.isChildSelectedByPlacementApplication(caseData)).isTrue();
    }

    @Test
    void shouldIdentifyCaseDataWhereChildIsNotSelectedByPlacement() {
        CaseData caseData = createCaseDataWithGivenOrderTempQuestions(
            OrderTempQuestions.builder()
                .childPlacementApplications(NO.getValue())
                .build());

        assertThat(underTest.isChildSelectedByPlacementApplication(caseData)).isFalse();
    }

    @Test
    void getSelectedChildFromSingleSelectionComponent() {
        Element<Child> secondChild = testChild();
        List<Element<Child>> children = asList(testChild(), secondChild, testChild());
        List<Pair<UUID, String>> dynamicListItems = ChildrenTestHelper.buildPairsFromChildrenList(children);
        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .orderTempQuestions(OrderTempQuestions.builder()
                    .selectSingleChild(YES.getValue())
                    .build())
                .whichChildIsTheOrderFor(buildDynamicList(1, dynamicListItems))
                .build())
            .children1(children)
            .build();

        List<Element<Child>> selectedChildFromSingleSelectionComponent =
            underTest.getSelectedChildFromSingleSelectionComponent(caseData);

        assertThat(selectedChildFromSingleSelectionComponent)
            .hasSize(1)
            .containsExactly(secondChild);
    }

    private CaseData createCaseDataWithGivenOrderTempQuestions(OrderTempQuestions orderTempQuestions) {
        return CaseData.builder()
            .manageOrdersEventData(
                ManageOrdersEventData.builder()
                    .orderTempQuestions(orderTempQuestions)
                    .build()
            )
            .build();
    }

}
