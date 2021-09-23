package uk.gov.hmcts.reform.fpl.selectors;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.OrderTempQuestions;
import uk.gov.hmcts.reform.fpl.service.ChildrenService;
import uk.gov.hmcts.reform.fpl.service.PlacementService;
import uk.gov.hmcts.reform.fpl.utils.ChildSelectionUtils;

import java.util.List;
import java.util.UUID;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.buildDynamicList;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testChild;

class ChildrenSmartSelectorTest {

    private ChildrenService mockChildrenService;

    private PlacementService mockPlacementService;

    private ChildrenSmartSelector underTest;

    @BeforeEach
    void setUp() {
        mockChildrenService = mock(ChildrenService.class);
        mockPlacementService = mock(PlacementService.class);
        underTest = new ChildrenSmartSelector(new ChildSelectionUtils(),
            mockChildrenService,
            mockPlacementService);
    }

    @Test
    void shouldCallChildrenServiceWhenSingleChildSelectorIsDisabled() {
        CaseData incomingCaseData = CaseData.builder().build();
        List<Element<Child>> expectedSelectedChildren = asList(element(Child.builder().build()));
        when(mockChildrenService.getSelectedChildren(incomingCaseData)).thenReturn(expectedSelectedChildren);

        List<Element<Child>> actualSelectedChildren = underTest.getSelectedChildren(incomingCaseData);

        assertThat(actualSelectedChildren).isEqualTo(expectedSelectedChildren);
        verify(mockChildrenService).getSelectedChildren(incomingCaseData);
    }

    @Test
    void shouldReturnListWithSingleSelectedChildWhenSingleChildSelectionIsOn() {
        Element<Child> firstChild = testChild();
        Element<Child> secondChild = testChild();

        CaseData incomingCaseData = CaseData.builder()
            .children1(asList(
                firstChild,
                secondChild
            ))
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .whichChildIsTheOrderFor(
                    buildDynamicList(0, buildPairFromChildElement(firstChild), buildPairFromChildElement(secondChild))
                )
                .orderTempQuestions(OrderTempQuestions.builder()
                    .selectSingleChild(YES.getValue())
                    .build())
                .build())
            .build();

        List<Element<Child>> actualSelectedChildren = underTest.getSelectedChildren(incomingCaseData);

        assertThat(actualSelectedChildren)
            .hasSize(1)
            .contains(firstChild);
        verify(mockChildrenService, never()).getSelectedChildren(incomingCaseData);
    }

    @Test
    void shouldRetrieveChildFromSelectedPlacement_WhenThisChildPlacementSelectionIsOn() {
        UUID selectedPlacementId = UUID.randomUUID();
        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(
                ManageOrdersEventData.builder()
                    .orderTempQuestions(OrderTempQuestions.builder().childPlacementApplications(YES.getValue()).build())
                    .manageOrdersChildPlacementApplication(
                        buildDynamicList(0, Pair.of(selectedPlacementId, "selected placement application"))
                    ).build()
            )
            .build();
        Element<Child> testChild = testChild();
        when(mockPlacementService.getChildByPlacementId(caseData, selectedPlacementId)).thenReturn(testChild);

        List<Element<Child>> selectedChildren = underTest.getSelectedChildren(caseData);

        assertThat(selectedChildren).hasSize(1).contains(testChild);
    }

    private Pair<UUID, String> buildPairFromChildElement(Element<Child> child) {
        return Pair.of(child.getId(), child.getValue().getParty().getFullName());
    }

}
