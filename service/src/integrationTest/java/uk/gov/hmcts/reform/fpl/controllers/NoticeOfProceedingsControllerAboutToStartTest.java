package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.NoticeOfProceedings;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.service.HearingBookingService.HEARING_DETAILS_KEY;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBooking;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;

@ActiveProfiles("integration-test")
@WebMvcTest(NoticeOfProceedingsController.class)
@OverrideAutoConfiguration(enabled = true)
class NoticeOfProceedingsControllerAboutToStartTest extends AbstractControllerTest {

    NoticeOfProceedingsControllerAboutToStartTest() {
        super("notice-of-proceedings");
    }

    @Test
    void shouldReturnErrorsWhenFamilymanNumberIsNotProvided() {
        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345L)
            .data(Map.of(HEARING_DETAILS_KEY, createHearingBookings()))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseDetails);

        assertThat(callbackResponse.getErrors()).containsOnlyOnce("Enter Familyman case number");
    }

    @Test
    void shouldUpdateProceedingLabelToIncludeHearingBookingDetailsDate() {
        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345L)
            .data(Map.of(
                HEARING_DETAILS_KEY, createHearingBookings(),
                "familyManCaseNumber", "123"
            ))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseDetails);

        String proceedingLabel = callbackResponse.getData().get("proceedingLabel").toString();

        String expectedContent = String.format("The case management hearing will be on the %s.",
            formatLocalDateTimeBaseUsingFormat(timeNow(), DATE));

        assertThat(proceedingLabel).isEqualTo(expectedContent);
    }

    @Test
    void shouldSetAssignJudgeLabelOnNoticeOfProceedingWhenAllocatedJudgeIsPopulated() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(ImmutableMap.of(
                "allocatedJudge", Judge.builder()
                    .judgeTitle(HIS_HONOUR_JUDGE)
                    .judgeLastName("Richards")
                    .build()
            )).build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseDetails);
        CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);
        JudgeAndLegalAdvisor judgeAndLegalAdvisor = caseData.getNoticeOfProceedings().getJudgeAndLegalAdvisor();

        assertThat(judgeAndLegalAdvisor.getAllocatedJudgeLabel())
            .isEqualTo("Case assigned to: His Honour Judge Richards");
    }

    @Test
    void shouldNotSetAssignedJudgeLabelOnNoticeOfProceedingIfAllocatedJudgeNotSet() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(ImmutableMap.of(
                "noticeOfProceedings", NoticeOfProceedings.builder()
                    .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder().build())
                    .build()
            ))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseDetails);
        CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);
        JudgeAndLegalAdvisor judgeAndLegalAdvisor = caseData.getNoticeOfProceedings().getJudgeAndLegalAdvisor();

        assertThat(judgeAndLegalAdvisor.getAllocatedJudgeLabel()).isNull();
    }

    private List<Element<HearingBooking>> createHearingBookings() {
        return ElementUtils.wrapElements(
            createHearingBooking(timeNow().plusDays(5), timeNow().plusHours(6)),
            createHearingBooking(timeNow().plusDays(2), timeNow().plusMinutes(45)),
            createHearingBooking(timeNow(), timeNow().plusHours(2)));
    }
}
