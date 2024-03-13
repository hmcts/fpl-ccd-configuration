package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.components.OptionCountBuilder;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.children.ChildFinalDecisionDetails;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.RecordChildrenFinalDecisionsEventData;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.ChildFinalDecisionReason.NO_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.ChildFinalDecisionReason.REFUSAL;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.ChildFinalDecisionReason.WITHDRAWN;
import static uk.gov.hmcts.reform.fpl.model.order.selector.Selector.newSelector;
import static uk.gov.hmcts.reform.fpl.service.RecordFinalDecisionsService.CLOSE_CASE_WARNING;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
public class RecordFinalDecisionsServiceTest {
    private static final String CHILDREN_LABEL = "Zero\nOne\nTwo\n";
    private static final String OPTION_COUNT = "12345";
    private static final LocalDate DATE = LocalDate.of(2021, 1, 1);
    private static final String FORMATTED_DATE = "1 January 2021";

    @Mock
    private ChildrenService childrenService;

    @Mock
    private OptionCountBuilder optionCountBuilder;

    @InjectMocks
    private RecordFinalDecisionsService underTest;

    @Nested
    class PrePopulateFields {
        @Test
        void shouldPrePopulateExpectedFieldsWhenSingleUnresolvedChildInCase() {
            List<Element<Child>> children = wrapElements(Child.builder().build());

            CaseData caseData = CaseData.builder()
                .children1(children)
                .build();

            given(childrenService.getRemainingChildren(caseData)).willReturn(children);
            given(childrenService.getChildrenLabel(children, false)).willReturn(CHILDREN_LABEL);

            Map<String, Object> expected = Map.of(
                "childSelector", newSelector(1),
                "children_label", CHILDREN_LABEL,
                "close_case_label", CLOSE_CASE_WARNING
            );

            Map<String, Object> actual = underTest.prePopulateFields(caseData);

            assertThat(actual).isEqualTo(expected);
        }

        @Test
        void shouldPrePopulateExpectedFieldsWhenMultipleUnresolvedChildInCase() {
            List<Element<Child>> children = wrapElements(
                Child.builder().build(),
                Child.builder().build(),
                Child.builder().build()
            );

            CaseData caseData = CaseData.builder()
                .children1(children)
                .build();

            given(childrenService.getRemainingChildren(caseData)).willReturn(children);
            given(childrenService.getChildrenLabel(children, false)).willReturn(CHILDREN_LABEL);

            Map<String, Object> expected = Map.of(
                "childSelector", newSelector(3),
                "children_label", CHILDREN_LABEL,
                "close_case_label", CLOSE_CASE_WARNING
            );

            Map<String, Object> actual = underTest.prePopulateFields(caseData);

            assertThat(actual).isEqualTo(expected);
        }
    }

    @Nested
    class PopulateFields {

        @Test
        void shouldPrePopulateExpectedFieldsWhenNoUnresolvedChildInCase() {
            List<Element<Child>> children = emptyList();

            CaseData caseData = CaseData.builder()
                .children1(children)
                .build();

            given(childrenService.getRemainingChildren(caseData)).willReturn(children);
            given(childrenService.getChildrenLabel(children, false)).willReturn(CHILDREN_LABEL);

            Map<String, Object> expected = Map.of(
                "childSelector", newSelector(0),
                "children_label", CHILDREN_LABEL,
                "close_case_label", CLOSE_CASE_WARNING
            );

            Map<String, Object> actual = underTest.prePopulateFields(caseData);

            assertThat(actual).isEqualTo(expected);
        }

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

    @Nested
    class UpdateChildren {
        @Test
        void shouldUpdateChildrenWhenSingleChild() {
            List<Element<Child>> children = wrapElements(
                buildChild("Bart", "Simpson", null, null)
            );

            RecordChildrenFinalDecisionsEventData eventData = RecordChildrenFinalDecisionsEventData.builder()
                .finalDecisionDate(DATE)
                .childFinalDecisionDetails00(ChildFinalDecisionDetails.builder()
                    .finalDecisionReason(REFUSAL)
                    .build())
                .build();

            CaseData caseData = CaseData.builder()
                .children1(children)
                .recordChildrenFinalDecisionsEventData(eventData)
                .build();

            given(childrenService.getRemainingChildren(caseData)).willReturn(children);

            List<Child> expectedChildren = List.of(
                buildChild("Bart", "Simpson", FORMATTED_DATE, REFUSAL.getLabel())
            );

            List<Child> actualChildren = unwrapElements(underTest.updateChildren(caseData));

            assertThat(actualChildren).isEqualTo(expectedChildren);
        }

        @Test
        void shouldUpdateChildrenWhenMultipleChildren() {
            List<Element<Child>> allChildren = wrapElements(
                buildChild("Bart", "Simpson", null, null),
                buildChild("Lisa", "Simpson", null, null),
                buildChild("Maggie", "Simpson", FORMATTED_DATE, NO_ORDER.getLabel())
            );

            List<Element<Child>> remainingChildren = allChildren.subList(0, 2);

            RecordChildrenFinalDecisionsEventData eventData = RecordChildrenFinalDecisionsEventData.builder()
                .finalDecisionDate(DATE)
                .childFinalDecisionDetails00(ChildFinalDecisionDetails.builder().finalDecisionReason(REFUSAL).build())
                .childFinalDecisionDetails01(ChildFinalDecisionDetails.builder().finalDecisionReason(WITHDRAWN).build())
                .build();

            CaseData caseData = CaseData.builder()
                .children1(allChildren)
                .recordChildrenFinalDecisionsEventData(eventData)
                .build();

            given(childrenService.getRemainingChildren(caseData)).willReturn(remainingChildren);

            List<Child> expectedChildren = List.of(
                buildChild("Bart", "Simpson", FORMATTED_DATE, REFUSAL.getLabel()),
                buildChild("Lisa", "Simpson", FORMATTED_DATE, WITHDRAWN.getLabel()),
                buildChild("Maggie", "Simpson", FORMATTED_DATE, NO_ORDER.getLabel()));

            List<Child> actualChildren = unwrapElements(underTest.updateChildren(caseData));

            assertThat(actualChildren).isEqualTo(expectedChildren);
        }

        @Test
        void shouldUpdateChildrenWhenNoFinalDecisionDetails() {
            List<Element<Child>> allChildren = wrapElements(
                buildChild("Bart", "Simpson", null, null),
                buildChild("Lisa", "Simpson", null, null),
                buildChild("Maggie", "Simpson", null, null)
            );

            List<Element<Child>> remainingChildren = allChildren.subList(0, 2);

            RecordChildrenFinalDecisionsEventData eventData = RecordChildrenFinalDecisionsEventData.builder()
                .finalDecisionDate(DATE)
                .build();

            CaseData caseData = CaseData.builder()
                .children1(allChildren)
                .recordChildrenFinalDecisionsEventData(eventData)
                .build();

            given(childrenService.getRemainingChildren(caseData)).willReturn(remainingChildren);

            List<Child> expectedChildren = List.of(
                buildChild("Bart", "Simpson", null, null),
                buildChild("Lisa", "Simpson", null, null),
                buildChild("Maggie", "Simpson", null, null)
            );

            List<Child> actualChildren = unwrapElements(underTest.updateChildren(caseData));

            assertThat(actualChildren).isEqualTo(expectedChildren);
        }
    }

    Child buildChild(String firstName, String lastName, String decisionDate, String decisionReason) {
        return Child.builder()
            .party(ChildParty.builder()
                .firstName(firstName)
                .lastName(lastName)
                .build())
            .finalDecisionDate(decisionDate)
            .finalDecisionReason(decisionReason)
            .build();
    }

}
