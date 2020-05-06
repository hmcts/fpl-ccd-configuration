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
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.service.HearingBookingService.HEARING_DETAILS_KEY;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@ActiveProfiles("integration-test")
@WebMvcTest(HearingBookingDetailsController.class)
@OverrideAutoConfiguration(enabled = true)
class HearingBookingDetailsControllerAboutToStartTest extends AbstractControllerTest {
    private static final String ERROR_MESSAGE = "You need to enter a judge before you can add hearing details";

    HearingBookingDetailsControllerAboutToStartTest() {
        super("add-hearing-bookings");
    }

    @Test
    void shouldReturnValidationErrorsWhenAJudgeIsNotAllocatedToTheCase() {
        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(caseDetails(ImmutableMap.of()));
        assertThat(response.getErrors()).containsExactly(ERROR_MESSAGE);
    }

    @Test
    void shouldReturnPopulatedHearingWhenNoOtherHearingsExistAndCaseHasAllocatedJudge() {
        Map<String, Object> data = Map.of("allocatedJudge", buildAllocatedJudge());
        CaseDetails caseDetails = caseDetails(data);

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(caseDetails);

        CaseData caseData = mapper.convertValue(response.getData(), CaseData.class);

        assertThat(unwrapElements(caseData.getHearingDetails())).containsExactly(HearingBooking.builder().build());
        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void shouldOnlyReturnHearingsWithFutureStartDateWhenHearingsInThePastExist() {
        Element<HearingBooking> hearingDetail = bookingWithStartDate(now().plusDays(5));
        Element<HearingBooking> hearingDetailPast = bookingWithStartDate(now().minusDays(5));

        List<Element<HearingBooking>> hearingDetails = newArrayList(hearingDetail, hearingDetailPast);

        Map<String, Object> data = Map.of(
            HEARING_DETAILS_KEY, hearingDetails,
            "allocatedJudge", buildAllocatedJudge()
        );

        CaseDetails caseDetails = caseDetails(data);

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(caseDetails);

        CaseData caseData = mapper.convertValue(response.getData(), CaseData.class);

        assertThat(caseData.getHearingDetails()).containsExactly(hearingDetail);
    }

    private CaseDetails caseDetails(Map<String, Object> data) {
        return CaseDetails.builder()
            .data(data)
            .build();
    }

    private Judge buildAllocatedJudge() {
        return Judge.builder()
            .judgeTitle(HIS_HONOUR_JUDGE)
            .judgeLastName("Davidson")
            .build();
    }

    private Element<HearingBooking> bookingWithStartDate(LocalDateTime date) {
        return element(HearingBooking.builder().startDate(date).build());
    }
}
