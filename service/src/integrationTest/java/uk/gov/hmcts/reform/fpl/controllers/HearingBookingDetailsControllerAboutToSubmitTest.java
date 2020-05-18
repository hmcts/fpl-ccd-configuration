package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HER_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.service.HearingBookingService.HEARING_DETAILS_KEY;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.ALLOCATED_JUDGE_KEY;

@ActiveProfiles("integration-test")
@WebMvcTest(HearingBookingDetailsController.class)
@OverrideAutoConfiguration(enabled = true)
class HearingBookingDetailsControllerAboutToSubmitTest extends AbstractControllerTest {

    HearingBookingDetailsControllerAboutToSubmitTest() {
        super("add-hearing-bookings");
    }

    @Test
    void shouldReturnEmptyHearingListWhenNoHearingInCase() {
        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(callbackRequest(
            Map.of(HEARING_DETAILS_KEY, emptyList()), Map.of(HEARING_DETAILS_KEY, emptyList())));

        CaseData caseData = mapper.convertValue(response.getData(), CaseData.class);

        assertThat(caseData.getHearingDetails()).isEmpty();
    }

    @Test
    void shouldReturnHearingsWhenNoHearingsExistInPast() {
        List<Element<HearingBooking>> hearingDetails = newArrayList(createHearingBooking(now().plusDays(5)));

        Map<String, Object> data = Map.of(
            HEARING_DETAILS_KEY, hearingDetails,
            ALLOCATED_JUDGE_KEY, buildAllocatedJudge()
        );

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(callbackRequest(
            data, Map.of(HEARING_DETAILS_KEY, hearingDetails)));

        CaseData caseData = mapper.convertValue(response.getData(), CaseData.class);

        assertThat(caseData.getHearingDetails()).isEqualTo(hearingDetails);
    }

    @Test
    void shouldReturnHearingsWhenNoHearingsExistInFuture() {
        List<Element<HearingBooking>> hearingDetails = newArrayList(createHearingBooking(now().minusDays(5)));

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(callbackRequest(
            Map.of(HEARING_DETAILS_KEY, emptyList()), Map.of(HEARING_DETAILS_KEY, hearingDetails)));

        CaseData caseData = mapper.convertValue(response.getData(), CaseData.class);

        assertThat(caseData.getHearingDetails()).isEqualTo(hearingDetails);
    }

    @Test
    void shouldReturnHearingsWhenHearingsInPastAndFutureExist() {
        Element<HearingBooking> hearingDetail = createHearingBooking(now().plusDays(5));
        Element<HearingBooking> hearingDetailPast = createHearingBooking(now().minusDays(5));

        Map<String, Object> data = Map.of(
            HEARING_DETAILS_KEY, newArrayList(hearingDetail),
            ALLOCATED_JUDGE_KEY, buildAllocatedJudge()
        );

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(callbackRequest(
            data, Map.of(HEARING_DETAILS_KEY, newArrayList(hearingDetail, hearingDetailPast))));

        CaseData caseData = mapper.convertValue(response.getData(), CaseData.class);

        assertThat(caseData.getHearingDetails()).containsExactly(hearingDetailPast, hearingDetail);
    }

    @Test
    void shouldUpdateHearingBookingJudgeWhenHearingIsToUseAllocatedJudge() {
        HearingBooking hearingBooking = HearingBooking.builder()
            .startDate(now())
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .useAllocatedJudge(YES.getValue())
                .build())
            .build();

        List<Element<HearingBooking>> hearingBookings = wrapElements(hearingBooking);

        Map<String, Object> data = Map.of(
            HEARING_DETAILS_KEY, hearingBookings,
            ALLOCATED_JUDGE_KEY, buildAllocatedJudge()
        );

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(callbackRequest(
            data, Map.of(HEARING_DETAILS_KEY, emptyList())));

        CaseData caseData = mapper.convertValue(response.getData(), CaseData.class);

        JudgeAndLegalAdvisor judgeAndLegalAdvisor
            = caseData.getHearingDetails().get(0).getValue().getJudgeAndLegalAdvisor();

        assertThat(judgeAndLegalAdvisor.getJudgeTitle()).isEqualTo(HER_HONOUR_JUDGE);
        assertThat(judgeAndLegalAdvisor.getJudgeLastName()).isEqualTo("Watson");
    }

    private CallbackRequest callbackRequest(Map<String, Object> data, Map<String, Object> dataBefore) {
        return CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .data(data)
                .build())
            .caseDetailsBefore(CaseDetails.builder()
                .data(dataBefore)
                .build())
            .build();
    }

    private Element<HearingBooking> createHearingBooking(LocalDateTime date) {
        return element(HearingBooking.builder()
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .judgeTitle(HIS_HONOUR_JUDGE)
                .judgeLastName("Davidson")
                .build())
            .startDate(date).build());
    }

    private Judge buildAllocatedJudge() {
        return Judge.builder()
            .judgeTitle(HER_HONOUR_JUDGE)
            .judgeLastName("Watson")
            .build();
    }
}
