package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableMap;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.DirectionAssignee;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.Order;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.CAFCASS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.COURT;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.OTHERS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.PARENTS_AND_RESPONDENTS;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBooking;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ActiveProfiles("integration-test")
@WebMvcTest(DraftOrdersController.class)
@OverrideAutoConfiguration(enabled = true)
class DraftOrdersControllerAboutToStartTest extends AbstractControllerTest {
    @Autowired
    private Time time;

    DraftOrdersControllerAboutToStartTest() {
        super("draft-standard-directions");
    }

    @Test
    void aboutToStartCallbackShouldSplitDirectionsIntoSeparateCollectionsAndShowEmptyPlaceHolderForHearingDate() {
        List<Direction> directions = createDirections();

        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of("standardDirectionOrder", Order.builder().directions(buildDirections(directions)).build()))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseDetails);

        CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

        assertThat(extractDirections(caseData.getAllParties())).containsOnly(directions.get(0));
        assertThat(extractDirections(caseData.getLocalAuthorityDirections())).containsOnly(directions.get(1));
        assertThat(extractDirections(caseData.getRespondentDirections())).containsOnly(directions.get(2));
        assertThat(extractDirections(caseData.getCafcassDirections())).containsOnly(directions.get(3));
        assertThat(extractDirections(caseData.getOtherPartiesDirections())).containsOnly(directions.get(4));
        assertThat(extractDirections(caseData.getCourtDirections())).containsOnly(directions.get(5)).hasSize(1);
        assertThat(caseData.getDateOfIssue()).isEqualTo(time.now().toLocalDate());

        Stream.of(DirectionAssignee.values()).forEach(assignee ->
            assertThat(callbackResponse.getData().get(assignee.toHearingDateField()))
                .isEqualTo("Please enter a hearing date"));
    }

    @Test
    void aboutToStartCallbackShouldPopulateCorrectHearingDate() {
        LocalDateTime date = LocalDateTime.of(2020, 1, 1, 0, 0, 0);

        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of("hearingDetails", wrapElements(createHearingBooking(date, date.plusDays(1)))))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseDetails);

        Stream.of(DirectionAssignee.values()).forEach(assignee ->
            assertThat(callbackResponse.getData().get(assignee.toHearingDateField()))
                .isEqualTo("1 January 2020, 12:00am"));
    }

    @Test
    void aboutToStartShouldUpdateAllocatedJudgeLabelOnCurrentJudgeAndLegalAdvisorWhenExists() {
        List<Direction> directions = createDirections();

        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of(
                "allocatedJudge", buildAllocatedJudge(),
                "standardDirectionOrder", Order.builder()
                    .directions(buildDirections(directions))
                    .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                        .judgeTitle(HIS_HONOUR_JUDGE)
                        .judgeLastName("Davidson")
                        .allocatedJudgeLabel("Case assigned to: His Honour Judge Davidson")
                        .build())
                    .build()))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseDetails);

        CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

        JudgeAndLegalAdvisor judgeAndLegalAdvisor = caseData.getJudgeAndLegalAdvisor();

        AssertionsForClassTypes.assertThat(judgeAndLegalAdvisor.getAllocatedJudgeLabel()).isEqualTo(
            "Case assigned to: His Honour Judge Richards");
        assertThat(judgeAndLegalAdvisor.getJudgeTitle()).isEqualTo(HIS_HONOUR_JUDGE);
        AssertionsForClassTypes.assertThat(judgeAndLegalAdvisor.getJudgeLastName()).isEqualTo("Davidson");
    }

    @Test
    void aboutToStartCallbackShouldSetAssignJudgeLabelWhenAllocatedJudgeIsPopulated() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(ImmutableMap.of(
                "allocatedJudge", buildAllocatedJudge()
            )).build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseDetails);
        CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);
        JudgeAndLegalAdvisor judgeAndLegalAdvisor = caseData.getJudgeAndLegalAdvisor();

        AssertionsForClassTypes.assertThat(judgeAndLegalAdvisor.getAllocatedJudgeLabel())
            .isEqualTo("Case assigned to: His Honour Judge Richards");
    }

    @Test
    void aboutToStartCallbackShouldNotSetAssignedJudgeLabelIfAllocatedJudgeNotSet() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(ImmutableMap.of(
                "judgeAndLegalAdvisor", JudgeAndLegalAdvisor.builder().build()
            ))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseDetails);
        CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);
        JudgeAndLegalAdvisor judgeAndLegalAdvisor = caseData.getJudgeAndLegalAdvisor();

        AssertionsForClassTypes.assertThat(judgeAndLegalAdvisor.getAllocatedJudgeLabel()).isNull();
    }

    private Judge buildAllocatedJudge() {
        return Judge.builder()
            .judgeTitle(HIS_HONOUR_JUDGE)
            .judgeLastName("Richards")
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

    private List<Direction> extractDirections(List<Element<Direction>> directions) {
        return directions.stream().map(Element::getValue).collect(toList());
    }
}
