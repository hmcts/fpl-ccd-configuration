package uk.gov.hmcts.reform.fpl.controllers;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.HearingBookingKeys.HEARING_DETAILS;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ActiveProfiles("integration-test")
@WebMvcTest(HearingBookingDetailsController.class)
@OverrideAutoConfiguration(enabled = true)
class HearingBookingDetailsControllerMidEventTest extends AbstractControllerTest {
    private static final String ERROR_MESSAGE = "Enter a start date in the future";
    private static final HearingBooking EMPTY_HEARING_BOOKING = HearingBooking.builder().build();

    HearingBookingDetailsControllerMidEventTest() {
        super("add-hearing-bookings");
    }

    @Test
    void shouldReturnAnErrorWhenHearingDateIsSetToYesterday() throws Exception {
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);

        AboutToStartOrSubmitCallbackResponse callbackResponse = makeRequest(createHearing(
            yesterday, yesterday.plusDays(1)));

        assertThat(callbackResponse.getErrors()).contains(ERROR_MESSAGE);
    }

    @Test
    void shouldNotReturnAnErrorWhenHearingDateIsSetToTomorrow() throws Exception {
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);

        AboutToStartOrSubmitCallbackResponse callbackResponse = makeRequest(createHearing(
            tomorrow, tomorrow.plusDays(1)));

        assertThat(callbackResponse.getErrors()).doesNotContain(ERROR_MESSAGE);
    }

    @Test
    void shouldReturnAnErrorWhenHearingDateIsSetToToday() throws Exception {
        LocalDateTime today = LocalDateTime.now();

        AboutToStartOrSubmitCallbackResponse callbackResponse = makeRequest(createHearing(
            today, today.plusDays(1)));

        assertThat(callbackResponse.getErrors()).contains(ERROR_MESSAGE);
    }

    @Test
    void shouldReturnAnErrorWhenHearingDateIsSetInDistantPast() throws Exception {
        LocalDateTime distantPast = LocalDateTime.now().minusYears(10000);

        AboutToStartOrSubmitCallbackResponse callbackResponse = makeRequest(createHearing(
            distantPast, distantPast.plusDays(1)));

        assertThat(callbackResponse.getErrors()).contains(ERROR_MESSAGE);
    }

    @Test
    void shouldNotReturnAnErrorWhenHearingDateIsSetInDistantFuture() throws Exception {
        LocalDateTime distantFuture = LocalDateTime.now().plusYears(1000);

        AboutToStartOrSubmitCallbackResponse callbackResponse = makeRequest(createHearing(
            distantFuture, distantFuture.plusDays(1)));

        assertThat(callbackResponse.getErrors()).doesNotContain(ERROR_MESSAGE);
    }

    @Test
    void shouldReturnAnErrorWhenExistingBookingIsUpdatedToPastDate() {
        LocalDateTime date = LocalDateTime.now().plusDays(5);
        UUID hearingId = randomUUID();

        List<Element<HearingBooking>> newHearingBooking = listBookingWithStartDate(hearingId, date.minusYears(1));
        List<Element<HearingBooking>> oldHearingBooking = listBookingWithStartDate(hearingId, date);

        CallbackRequest request = callbackRequestWithEditedBooking(newHearingBooking, oldHearingBooking);

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(request);

        assertThat(callbackResponse.getErrors()).containsOnlyOnce(ERROR_MESSAGE);
    }

    private CallbackRequest callbackRequestWithEditedBooking(List<Element<HearingBooking>> newHearings,
                                                             List<Element<HearingBooking>> oldHearings) {
        return CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .data(Map.of(HEARING_DETAILS.getKey(), newHearings))
                .build())
            .caseDetailsBefore(CaseDetails.builder()
                .data(Map.of(HEARING_DETAILS.getKey(), oldHearings))
                .build())
            .build();
    }

    private HearingBooking createHearing(LocalDateTime startDate, LocalDateTime endDate) {
        return HearingBooking.builder()
            .startDate(startDate)
            .endDate(endDate)
            .build();
    }

    private AboutToStartOrSubmitCallbackResponse makeRequest(HearingBooking after) {
        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(12345L)
                .data(Map.of(HEARING_DETAILS.getKey(), wrapElements(after)))
                .build())
            .caseDetailsBefore(CaseDetails.builder()
                .id(12345L)
                .data(Map.of(HEARING_DETAILS.getKey(), wrapElements(EMPTY_HEARING_BOOKING)))
                .build())
            .build();

        return postMidEvent(request);
    }

    private List<Element<HearingBooking>> listBookingWithStartDate(UUID id, LocalDateTime date) {
        return Lists.newArrayList(element(id, createHearing(date, date.plusDays(1))));
    }
}
