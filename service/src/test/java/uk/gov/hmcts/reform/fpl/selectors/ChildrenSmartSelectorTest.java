package uk.gov.hmcts.reform.fpl.selectors;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.OrderTempQuestions;
import uk.gov.hmcts.reform.fpl.service.ChildrenService;

import java.util.List;
import java.util.UUID;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.buildDynamicList;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testChild;

@ExtendWith({MockitoExtension.class})
class ChildrenSmartSelectorTest {

    @Mock
    private ChildrenService mockChildrenService;

    @InjectMocks
    private ChildrenSmartSelector childrenSmartSelector;

    @Test
    void shouldCallChildrenServiceWhenSingleChildSelectorIsDisabled() {
        CaseData incomingCaseData = CaseData.builder().build();
        List<Element<Child>> expectedSelectedChildren = asList(element(Child.builder().build()));
        when(mockChildrenService.getSelectedChildren(incomingCaseData)).thenReturn(expectedSelectedChildren);

        List<Element<Child>> actualSelectedChildren = childrenSmartSelector.getSelectedChildren(incomingCaseData);

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
                    .selectSingleChild("YES")
                    .build())
                .build())
            .build();

        List<Element<Child>> actualSelectedChildren = childrenSmartSelector.getSelectedChildren(incomingCaseData);

        assertThat(actualSelectedChildren)
            .hasSize(1)
            .contains(firstChild);
        verify(mockChildrenService, never()).getSelectedChildren(incomingCaseData);
    }

    private Pair<UUID, String> buildPairFromChildElement(Element<Child> child) {
        return Pair.of(child.getId(), child.getValue().getParty().getFullName());
    }

}
