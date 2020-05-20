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
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HER_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.service.HearingBookingService.HEARING_DETAILS_KEY;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
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
        CallbackRequest callbackRequest = callbackRequest(hearingMapOf(emptyList()), hearingMapOf(emptyList()));

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(callbackRequest);

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

        CallbackRequest callbackRequest = callbackRequest(data, hearingMapOf(hearingDetails));
        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(callbackRequest);

        CaseData caseData = mapper.convertValue(response.getData(), CaseData.class);

        assertThat(caseData.getHearingDetails()).isEqualTo(hearingDetails);
    }

    @Test
    void shouldReturnHearingsWhenNoHearingsExistInFuture() {
        List<Element<HearingBooking>> hearingDetails = newArrayList(createHearingBooking(now().minusDays(5)));

        CallbackRequest callbackRequest = callbackRequest(hearingMapOf(emptyList()), hearingMapOf(hearingDetails));
        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(callbackRequest);

        CaseData caseData = mapper.convertValue(response.getData(), CaseData.class);

        assertThat(caseData.getHearingDetails()).isEqualTo(hearingDetails);
    }

    @Test
    void shouldReturnHearingsWhenHearingsInPastAndFutureExist() {
        Element<HearingBooking> hearing = createHearingBooking(now().plusDays(5));
        Element<HearingBooking> pastHearing = createHearingBooking(now().minusDays(5));

        Map<String, Object> data = Map.of(
            HEARING_DETAILS_KEY, newArrayList(hearing),
            ALLOCATED_JUDGE_KEY, buildAllocatedJudge()
        );

        CallbackRequest callbackRequest = callbackRequest(data, hearingMapOf(List.of(hearing, pastHearing)));
        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(callbackRequest);

        CaseData caseData = mapper.convertValue(response.getData(), CaseData.class);

        assertThat(caseData.getHearingDetails()).containsExactly(pastHearing, hearing);
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

        CallbackRequest callbackRequest = callbackRequest(data, hearingMapOf(emptyList()));
        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(callbackRequest);
        CaseData caseData = mapper.convertValue(response.getData(), CaseData.class);
        HearingBooking returnedHearing = unwrapElements(caseData.getHearingDetails()).get(0);

        assertThat(returnedHearing.getJudgeAndLegalAdvisor().getJudgeTitle()).isEqualTo(HER_HONOUR_JUDGE);
        assertThat(returnedHearing.getJudgeAndLegalAdvisor().getJudgeLastName()).isEqualTo("Watson");
    }

    @Test
    void shouldOnlyAddCurrentHearingToListWhenSameIdForHearings() {
        UUID id = randomUUID();

        HearingBooking hearing = getHearing(now());
        HearingBooking hearingInPast = getHearing(now().minusMinutes(1));

        CallbackRequest callbackRequest = getCallbackRequest(hearing, hearingInPast, id);

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(callbackRequest);
        CaseData caseData = mapper.convertValue(response.getData(), CaseData.class);

        assertThat(caseData.getHearingDetails()).containsExactly(element(id, hearing));
    }

    private CallbackRequest getCallbackRequest(HearingBooking hearing, HearingBooking hearingInPast, UUID id) {
        return callbackRequest(
            hearingMapOf(List.of(element(id, hearing))),
            hearingMapOf(List.of(element(id, hearingInPast))));
    }

    private Map<String, Object> hearingMapOf(List<Element<HearingBooking>> hearings) {
        return Map.of(HEARING_DETAILS_KEY, hearings);
    }

    private HearingBooking getHearing(LocalDateTime now) {
        return HearingBooking.builder()
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .judgeTitle(HIS_HONOUR_JUDGE)
                .judgeLastName("Davidson")
                .build())
            .startDate(now)
            .build();
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
        return element(getHearing(date));
    }

    private Judge buildAllocatedJudge() {
        return Judge.builder()
            .judgeTitle(HER_HONOUR_JUDGE)
            .judgeLastName("Watson")
            .build();
    }
}
