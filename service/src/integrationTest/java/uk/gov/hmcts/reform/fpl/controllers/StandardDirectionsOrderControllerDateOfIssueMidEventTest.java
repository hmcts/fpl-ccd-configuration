package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.DirectionAssignee;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.SDORoute;
import uk.gov.hmcts.reform.fpl.model.Allocation;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.CAFCASS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.COURT;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.OTHERS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.PARENTS_AND_RESPONDENTS;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBooking;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testJudge;

@WebMvcTest(StandardDirectionsOrderController.class)
@OverrideAutoConfiguration(enabled = true)
class StandardDirectionsOrderControllerDateOfIssueMidEventTest extends AbstractCallbackTest {

    StandardDirectionsOrderControllerDateOfIssueMidEventTest() {
        super("draft-standard-directions");
    }

    @Test
    void shouldPopulateCorrectHearingDate() {
        LocalDateTime hearingDate = LocalDateTime.of(2020, 1, 1, 0, 0, 0);

        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of("hearingDetails", wrapElements(createHearingBooking(hearingDate,
                hearingDate.plusDays(1)))))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "date-of-issue");

        Stream.of(DirectionAssignee.values()).forEach(assignee ->
            assertThat(callbackResponse.getData().get(assignee.toHearingDateField()))
                .isEqualTo("1 January 2020, 12:00am"));
    }

    @Test
    void shouldShowEmptyPlaceHolderForHearingDateWhenNoHearingDate() {
        CaseDetails caseDetails = CaseDetails.builder().data(new HashMap<>()).build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "date-of-issue");

        Stream.of(DirectionAssignee.values()).forEach(assignee ->
            assertThat(callbackResponse.getData().get(assignee.toHearingDateField()))
                .isEqualTo("Please enter a hearing date"));
    }

    @Test
    void shouldUpdateAllocatedJudgeLabelOnCurrentJudgeAndLegalAdvisorWhenExists() {
        List<Direction> directions = createDirections();

        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of(
                "allocatedJudge", testJudge(),
                "standardDirectionOrder", StandardDirectionOrder.builder()
                    .directions(buildDirections(directions))
                    .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                        .judgeTitle(HIS_HONOUR_JUDGE)
                        .judgeLastName("Davidson")
                        .allocatedJudgeLabel("Case assigned to: His Honour Judge Davidson")
                        .build())
                    .build()))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "date-of-issue");

        CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

        JudgeAndLegalAdvisor judgeAndLegalAdvisor = caseData.getJudgeAndLegalAdvisor();

        assertThat(judgeAndLegalAdvisor.getAllocatedJudgeLabel()).isEqualTo(
            "Case assigned to: Magistrates (JP) Brandon Stark");
        assertThat(judgeAndLegalAdvisor.getJudgeTitle()).isEqualTo(HIS_HONOUR_JUDGE);
        assertThat(judgeAndLegalAdvisor.getJudgeLastName()).isEqualTo("Davidson");
    }

    @Test
    void shouldSetAssignJudgeLabelWhenAllocatedJudgeIsPopulated() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of("allocatedJudge", testJudge()))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "date-of-issue");
        CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);
        JudgeAndLegalAdvisor judgeAndLegalAdvisor = caseData.getJudgeAndLegalAdvisor();

        assertThat(judgeAndLegalAdvisor.getAllocatedJudgeLabel())
            .isEqualTo("Case assigned to: Magistrates (JP) Brandon Stark");
    }

    @Test
    void shouldPopulateUseAllocatedJudgeWithYesWhenJudgeAndAllocatedJudgeAreEqual() {
        CaseDetails caseDetails = buildSameJudgeCaseDetails(testJudge());

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "date-of-issue");
        CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);
        JudgeAndLegalAdvisor judgeAndLegalAdvisor = caseData.getJudgeAndLegalAdvisor();

        assertThat(judgeAndLegalAdvisor.getUseAllocatedJudge()).isEqualTo("Yes");
    }

    @Test
    void shouldNotSetAssignedJudgeLabelIfAllocatedJudgeNotSet() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of("judgeAndLegalAdvisor", JudgeAndLegalAdvisor.builder().build()))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "date-of-issue");
        CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);
        JudgeAndLegalAdvisor judgeAndLegalAdvisor = caseData.getJudgeAndLegalAdvisor();

        assertThat(judgeAndLegalAdvisor.getAllocatedJudgeLabel()).isNull();
    }

    private CaseDetails buildSameJudgeCaseDetails(Judge judge) {
        return CaseDetails.builder()
            .data(Map.of(
                "allocatedJudge", judge,
                "standardDirectionOrder", StandardDirectionOrder.builder()
                    .directions(buildDirections(createDirections()))
                    .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                        .judgeTitle(judge.getJudgeTitle())
                        .judgeLastName(judge.getJudgeLastName())
                        .judgeFullName(judge.getJudgeFullName())
                        .build())
                    .build()))
            .build();
    }

    private List<Direction> createDirections() {
        String title = "example direction";

        return List.of(
            Direction.builder().directionType(title).assignee(ALL_PARTIES).build(),
            Direction.builder().directionType(title).assignee(LOCAL_AUTHORITY).build(),
            Direction.builder().directionType(title).assignee(PARENTS_AND_RESPONDENTS).build(),
            Direction.builder().directionType(title).assignee(CAFCASS).build(),
            Direction.builder().directionType(title).assignee(OTHERS).build(),
            Direction.builder().directionType(title).assignee(COURT).build(),
            Direction.builder().directionType(title).custom("Yes").assignee(COURT).build()
        );
    }

    private List<Element<Direction>> buildDirections(List<Direction> directions) {
        return directions.stream().map(ElementUtils::element).collect(toList());
    }

    private JudgeAndLegalAdvisor buildJudgeAndLegalAdvisor() {
        return JudgeAndLegalAdvisor.builder()
            .judgeTitle(HIS_HONOUR_JUDGE)
            .judgeLastName("Davidson")
            .build();
    }

    @Nested
    class PrePopulation {
        @Test
        void shouldPopulateDateOfIssue() {
            CaseData caseData = CaseData.builder()
                .sdoRouter(SDORoute.SERVICE)
                .build();

            CaseData responseData = extractCaseData(postMidEvent(caseData, "pre-populate"));

            assertThat(responseData.getDateOfIssue()).isEqualTo(now().toLocalDate());
        }

        @Test
        void shouldRemoveEmptyPreparedStandardDirectionOrderDocumentIfPresent() {
            CaseData caseData = CaseData.builder()
                .preparedSDO(DocumentReference.builder().build())
                .build();

            AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, "pre-populate");

            assertThat(response.getData()).isNotEmpty()
                .doesNotContainKey("preparedSDO");
        }

        @Test
        void shouldPrePopulateExistingJudgeAndLegalAdvisor() {
            JudgeAndLegalAdvisor judgeAndLegalAdvisor = buildJudgeAndLegalAdvisor();

            CaseData caseData = CaseData.builder()
                .standardDirectionOrder(StandardDirectionOrder.builder()
                    .judgeAndLegalAdvisor(judgeAndLegalAdvisor)
                    .build())
                .build();

            CaseData responseCaseData = extractCaseData(postMidEvent(caseData, "pre-populate"));

            assertThat(responseCaseData.getJudgeAndLegalAdvisor()).isEqualTo(judgeAndLegalAdvisor);
        }

        @Test
        void shouldReturnUrgentHearingOrderSelectedInNonGatekeepingState() {
            CaseData caseData = CaseData.builder().state(State.CASE_MANAGEMENT).sdoRouter(SDORoute.URGENT).build();

            AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, "pre-populate");

            assertThat(response.getErrors()).isEqualTo(List.of(
                "An urgent hearing order has already been added to this case. You can still add a gatekeeping "
                + "order, if needed."
            ));
        }

        @Test
        void shouldPrePopulateAllocationDecision() {
            CaseData caseData = CaseData.builder()
                .state(State.GATEKEEPING)
                .allocationProposal(Allocation.builder().proposal("District judge").build())
                .sdoRouter(SDORoute.URGENT)
                .build();

            CaseData data = extractCaseData(postMidEvent(caseData, "pre-populate"));

            Allocation expectedAllocation = Allocation.builder()
                .allocationProposalPresent("Yes")
                .build();
            assertThat(data.getGatekeepingOrderEventData().getUrgentHearingAllocation()).isEqualTo(expectedAllocation);
            assertThat(data.getGatekeepingOrderEventData().getShowUrgentHearingAllocation()).isEqualTo(YesNo.YES);
        }

        @Test
        void shouldPrePopulateFieldToHideAllocationPageWhenAllocationDecisionAlreadyMade() {
            CaseData caseData = CaseData.builder()
                .state(State.GATEKEEPING)
                .allocationDecision(Allocation.builder()
                    .judgeLevelRadio("District judge")
                    .proposalReason("blah")
                    .build())
                .sdoRouter(SDORoute.URGENT)
                .build();

            CaseData data = extractCaseData(postMidEvent(caseData, "pre-populate"));

            assertThat(data.getGatekeepingOrderEventData().getUrgentHearingAllocation()).isNull();
            assertThat(data.getGatekeepingOrderEventData().getShowUrgentHearingAllocation()).isEqualTo(YesNo.NO);
        }
    }

    @Nested
    class DateOfIssueValidation {

        @Test
        void shouldReturnErrorsWhenTheDateOfIssueIsInFuture() {
            CaseDetails caseDetails = caseDetails(now().plusDays(1));
            AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseDetails, "date-of-issue");
            assertThat(response.getErrors()).containsOnlyOnce("Date of issue cannot be in the future");
        }

        @Test
        void shouldNotReturnErrorsWhenDateOfIssueIsToday() {
            CaseDetails caseDetails = caseDetails(now());
            AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseDetails, "date-of-issue");
            assertThat(response.getErrors()).isNull();
        }

        @Test
        void shouldNotReturnErrorsWhenDateOfIssueIsInPast() {
            CaseDetails caseDetails = caseDetails(now().minusDays(1));
            AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseDetails, "date-of-issue");
            assertThat(response.getErrors()).isNull();
        }

        private CaseDetails caseDetails(LocalDateTime localDateTime) {
            return CaseDetails.builder().data(Map.of("dateOfIssue", localDateTime.toLocalDate())).build();
        }
    }
}
