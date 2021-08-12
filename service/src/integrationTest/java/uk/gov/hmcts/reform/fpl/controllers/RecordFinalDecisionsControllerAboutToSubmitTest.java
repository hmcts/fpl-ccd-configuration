package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.ChildFinalDecisionReason;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.CloseCase;
import uk.gov.hmcts.reform.fpl.model.children.ChildFinalDecisionDetails;
import uk.gov.hmcts.reform.fpl.model.event.RecordChildrenFinalDecisionsEventData;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.State.CLOSED;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@WebMvcTest(RecordFinalDecisionsController.class)
@OverrideAutoConfiguration(enabled = true)
class RecordFinalDecisionsControllerAboutToSubmitTest extends AbstractCallbackTest {

    private static final String DATE_KEY = "finalDecisionDate";
    private static final String OPTION_COUNT_KEY = "optionCount";
    private static final String CLOSE_CASE_LABEL_KEY = "close_case_label";
    private static final String CHILDREN_LABEL_KEY = "children_label";
    private static final String ORDER_APPLIES_TO_ALL_CHILDREN_KEY = "orderAppliesToAllChildren";
    private static final LocalDate FINAL_DECISION_DATE = LocalDate.now();

    RecordFinalDecisionsControllerAboutToSubmitTest() {
        super("record-final-decisions");
    }

    @Test
    void shouldCleanCaseDataOfTransientFields() {
        ChildFinalDecisionDetails childFinalDecisionDetails = ChildFinalDecisionDetails.builder().build();

        Map<String, Object> data = Map.ofEntries(
            Map.entry("childFinalDecisionDetails00", childFinalDecisionDetails),
            Map.entry("childFinalDecisionDetails01", childFinalDecisionDetails),
            Map.entry("childFinalDecisionDetails02", childFinalDecisionDetails),
            Map.entry("childFinalDecisionDetails03", childFinalDecisionDetails),
            Map.entry("childFinalDecisionDetails04", childFinalDecisionDetails),
            Map.entry("childFinalDecisionDetails05", childFinalDecisionDetails),
            Map.entry("childFinalDecisionDetails06", childFinalDecisionDetails),
            Map.entry("childFinalDecisionDetails07", childFinalDecisionDetails),
            Map.entry("childFinalDecisionDetails08", childFinalDecisionDetails),
            Map.entry("childFinalDecisionDetails09", childFinalDecisionDetails),
            Map.entry("childFinalDecisionDetails10", childFinalDecisionDetails),
            Map.entry("childFinalDecisionDetails11", childFinalDecisionDetails),
            Map.entry("childFinalDecisionDetails12", childFinalDecisionDetails),
            Map.entry("childFinalDecisionDetails13", childFinalDecisionDetails),
            Map.entry("childFinalDecisionDetails14", childFinalDecisionDetails),
            Map.entry(DATE_KEY, LocalDate.of(2013, 2, 26)),
            Map.entry(OPTION_COUNT_KEY, "01234567891011121314"),
            Map.entry(CLOSE_CASE_LABEL_KEY, "close case label"),
            Map.entry(CHILDREN_LABEL_KEY, "children label"),
            Map.entry(ORDER_APPLIES_TO_ALL_CHILDREN_KEY, YES.getValue())

        );

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(CaseDetails.builder()
            .data(data)
            .build());

        assertThat(response.getData()).doesNotContainKeys("childFinalDecisionDetails00", "childFinalDecisionDetails01",
            "childFinalDecisionDetails02", "childFinalDecisionDetails03", "childFinalDecisionDetails04",
            "childFinalDecisionDetails05", "childFinalDecisionDetails06", "childFinalDecisionDetails07",
            "childFinalDecisionDetails08", "childFinalDecisionDetails09", "childFinalDecisionDetails10",
            "childFinalDecisionDetails11", "childFinalDecisionDetails12", "childFinalDecisionDetails13",
            "childFinalDecisionDetails14", DATE_KEY, OPTION_COUNT_KEY, CLOSE_CASE_LABEL_KEY, CHILDREN_LABEL_KEY,
            ORDER_APPLIES_TO_ALL_CHILDREN_KEY);
    }

    @Test
    void shouldCloseCaseWhenAllChildrenHaveFinalOrderOrDecision() {
        CaseData caseData = CaseData.builder()
            .children1(wrapElements(
                Child.builder().finalDecisionReason("Application refused").build()))
            .recordChildrenFinalDecisionsEventData(RecordChildrenFinalDecisionsEventData.builder()
                .finalDecisionDate(FINAL_DECISION_DATE)
                .build())
            .build();

        CaseData responseCaseData = extractCaseData(postAboutToSubmitEvent(caseData));

        assertThat(responseCaseData.getState()).isEqualTo(CLOSED);
        assertThat(responseCaseData.getCloseCaseTabField())
            .isEqualTo(CloseCase.builder().date(FINAL_DECISION_DATE).build());
    }

    @Test
    void shouldNotCloseCaseWhenNotAllChildrenHaveFinalOrderOrDecision() {
        CaseData caseData = CaseData.builder()
            .children1(wrapElements(
                Child.builder().build(),
                Child.builder().build()))
            .recordChildrenFinalDecisionsEventData(RecordChildrenFinalDecisionsEventData.builder()
                .childFinalDecisionDetails00(ChildFinalDecisionDetails.builder()
                    .finalDecisionReason(ChildFinalDecisionReason.REFUSAL.name()).build())
                .childFinalDecisionDetails01(ChildFinalDecisionDetails.builder().build())
                .finalDecisionDate(FINAL_DECISION_DATE)
                .build())
            .build();

        CaseData responseCaseData = extractCaseData(postAboutToSubmitEvent(caseData));

        assertThat(responseCaseData.getState()).isEqualTo(null);
        assertThat(responseCaseData.getCloseCaseTabField()).isEqualTo(null);
    }
}
