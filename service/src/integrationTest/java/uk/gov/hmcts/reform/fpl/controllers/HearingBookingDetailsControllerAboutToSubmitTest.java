package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.HearingBookingKeys.HEARING_DETAILS;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ActiveProfiles("integration-test")
@WebMvcTest(HearingBookingDetailsController.class)
@OverrideAutoConfiguration(enabled = true)
class HearingBookingDetailsControllerAboutToSubmitTest extends AbstractControllerTest {
    private static final LocalDateTime TODAY = LocalDateTime.now();

    @Autowired
    private ObjectMapper mapper;

    HearingBookingDetailsControllerAboutToSubmitTest() {
        super("add-hearing-bookings");
    }

    @Test
    void shouldReturnEmptyListWhenSubmittingEmptyEvent() {
        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(callbackRequest(
            Map.of(HEARING_DETAILS.getKey(), emptyList()), Map.of(HEARING_DETAILS.getKey(), emptyList())));

        CaseData caseData = mapper.convertValue(response.getData(), CaseData.class);

        assertThat(caseData.getHearingDetails()).isEmpty();
    }

    @Test
    void shouldReturnHearingsWhenNoHearingsExistInPast() {
        List<Element<HearingBooking>> hearingDetails = newArrayList(bookingWithStartDate(TODAY.plusDays(5)));

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(callbackRequest(
            Map.of(HEARING_DETAILS.getKey(), hearingDetails), Map.of(HEARING_DETAILS.getKey(), hearingDetails)));

        CaseData caseData = mapper.convertValue(response.getData(), CaseData.class);

        assertThat(caseData.getHearingDetails()).hasSize(1);
        assertThat(caseData.getHearingDetails()).isEqualTo(hearingDetails);
    }

    @Test
    void shouldReturnHearingsWhenNoHearingsExistInFuture() {
        List<Element<HearingBooking>> hearingDetails = newArrayList(bookingWithStartDate(TODAY.plusDays(-5)));

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(callbackRequest(
            Map.of(HEARING_DETAILS.getKey(), emptyList()), Map.of(HEARING_DETAILS.getKey(), hearingDetails)));

        CaseData caseData = mapper.convertValue(response.getData(), CaseData.class);

        assertThat(caseData.getHearingDetails()).hasSize(1);
        assertThat(caseData.getHearingDetails()).isEqualTo(hearingDetails);
    }

    @Test
    void shouldReturnHearingsWhenHearingsInPastAndFutureExist() {
        Element<HearingBooking> hearingDetail = bookingWithStartDate(TODAY.plusDays(5));
        Element<HearingBooking> hearingDetailPast = bookingWithStartDate(TODAY.minusDays(5));

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(callbackRequest(
            Map.of(HEARING_DETAILS.getKey(), newArrayList(hearingDetail)),
            Map.of(HEARING_DETAILS.getKey(), newArrayList(hearingDetail, hearingDetailPast))));

        CaseData caseData = mapper.convertValue(response.getData(), CaseData.class);

        assertThat(caseData.getHearingDetails()).hasSize(2);
        assertThat(caseData.getHearingDetails().get(0)).isEqualTo(hearingDetailPast);
        assertThat(caseData.getHearingDetails().get(1)).isEqualTo(hearingDetail);
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

    private Element<HearingBooking> bookingWithStartDate(LocalDateTime date) {
        return element(HearingBooking.builder().startDate(date).build());
    }
}
