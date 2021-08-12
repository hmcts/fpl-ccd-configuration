package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.components.OptionCountBuilder;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.children.ChildFinalDecisionDetails;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
public class RecordFinalDecisionsServiceTest {
    private static final String CHILDREN_LABEL = "Zero\nOne\nTwo\n";
    private static final String OPTION_COUNT = "12345";

    @Mock
    private ChildrenService childrenService;

    @Mock
    private OptionCountBuilder optionCountBuilder;

    @InjectMocks
    private RecordFinalDecisionsService underTest;

    @Test
    void shouldPopulateExpectedFieldsWhenSingleChildSelected() {
        CaseData caseData = CaseData.builder()
            .orderAppliesToAllChildren("No")
            .build();

        List<Element<Child>> children = wrapElements(Child.builder().build());

        given(childrenService.getRemainingChildren(caseData)).willReturn(children);
        given(childrenService.getChildrenLabel(children, false))
            .willReturn(CHILDREN_LABEL);

        Map<String, Object> actual = underTest.populateFields(caseData);

        Map<String, Object> expected = Map.of(
            "childFinalDecisionDetails00", ChildFinalDecisionDetails.builder().childNameLabel("Zero").build()
        );

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldPopulateExpectedFieldsWhenMultipleChildrenSelected() {
        CaseData caseData = CaseData.builder()
            .orderAppliesToAllChildren("No")
            .build();

        List<Element<Child>> children = wrapElements(Child.builder().build(), Child.builder().build(),
            Child.builder().build());

        given(childrenService.getRemainingChildren(caseData)).willReturn(children);
        given(childrenService.getChildrenLabel(children, false))
            .willReturn(CHILDREN_LABEL);

        Map<String, Object> actual = underTest.populateFields(caseData);

        Map<String, Object> expected = Map.of(
            "childFinalDecisionDetails00", ChildFinalDecisionDetails.builder().childNameLabel("Zero").build(),
            "childFinalDecisionDetails01", ChildFinalDecisionDetails.builder().childNameLabel("One").build(),
            "childFinalDecisionDetails02", ChildFinalDecisionDetails.builder().childNameLabel("Two").build()
        );

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldPopulateExpectedFieldsWhenAppliesToAllChildren() {
        CaseData caseData = CaseData.builder()
            .orderAppliesToAllChildren("Yes")
            .build();

        List<Element<Child>> children = wrapElements(Child.builder().build(), Child.builder().build(),
            Child.builder().build());

        given(childrenService.getRemainingChildren(caseData)).willReturn(children);
        given(childrenService.getChildrenLabel(children, false))
            .willReturn(CHILDREN_LABEL);
        given(optionCountBuilder.generateCode(children)).willReturn(OPTION_COUNT);

        Map<String, Object> actual = underTest.populateFields(caseData);

        Map<String, Object> expected = Map.of(
            "childFinalDecisionDetails00", ChildFinalDecisionDetails.builder().childNameLabel("Zero").build(),
            "childFinalDecisionDetails01", ChildFinalDecisionDetails.builder().childNameLabel("One").build(),
            "childFinalDecisionDetails02", ChildFinalDecisionDetails.builder().childNameLabel("Two").build(),
            "optionCount", OPTION_COUNT
        );

        assertThat(actual).isEqualTo(expected);
    }
}
