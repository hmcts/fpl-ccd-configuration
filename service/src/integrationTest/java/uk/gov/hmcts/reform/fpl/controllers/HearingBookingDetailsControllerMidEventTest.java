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
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderKey.NEW_HEARING_LABEL;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderKey.NEW_HEARING_SELECTOR;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.FINAL;
import static uk.gov.hmcts.reform.fpl.service.HearingBookingService.HEARING_DETAILS_KEY;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
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
    void shouldReturnAnErrorWhenHearingDateIsSetToYesterday() {
        LocalDateTime yesterday = now().minusDays(1);

        AboutToStartOrSubmitCallbackResponse response = makeRequest(createHearing(yesterday, yesterday.plusDays(1)));

        assertThat(response.getErrors()).contains(ERROR_MESSAGE);
    }

    @Test
    void shouldNotReturnAnErrorWhenHearingDateIsSetToTomorrow() {
        LocalDateTime tomorrow = now().plusDays(1);

        AboutToStartOrSubmitCallbackResponse response = makeRequest(createHearing(tomorrow, tomorrow.plusDays(1)));

        assertThat(response.getErrors()).doesNotContain(ERROR_MESSAGE);
    }

    @Test
    void shouldReturnAnErrorWhenHearingDateIsSetToToday() {
        LocalDateTime today = LocalDateTime.now(); // this needs to use LocalDateTime due to @Future validator

        AboutToStartOrSubmitCallbackResponse response = makeRequest(createHearing(today, today.plusDays(1)));

        assertThat(response.getErrors()).contains(ERROR_MESSAGE);
    }

    @Test
    void shouldReturnAnErrorWhenHearingDateIsSetInDistantPast() {
        LocalDateTime pastDate = now().minusYears(10000);

        AboutToStartOrSubmitCallbackResponse response = makeRequest(createHearing(pastDate, pastDate.plusDays(1)));

        assertThat(response.getErrors()).contains(ERROR_MESSAGE);
    }

    @Test
    void shouldNotReturnAnErrorWhenHearingDateIsSetInDistantFuture() {
        LocalDateTime futureDate = now().plusYears(1000);

        AboutToStartOrSubmitCallbackResponse response = makeRequest(createHearing(futureDate, futureDate.plusDays(1)));

        assertThat(response.getErrors()).doesNotContain(ERROR_MESSAGE);
    }

    @Test
    void shouldReturnAnErrorWhenExistingBookingIsUpdatedToPastDate() {
        LocalDateTime date = now().plusDays(5);
        UUID hearingId = randomUUID();

        List<Element<HearingBooking>> newHearingBooking = listBookingWithStartDate(hearingId, date.minusYears(1));
        List<Element<HearingBooking>> oldHearingBooking = listBookingWithStartDate(hearingId, date);

        CallbackRequest request = callbackRequestWithEditedBooking(newHearingBooking, oldHearingBooking);

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(request);

        assertThat(callbackResponse.getErrors()).containsOnlyOnce(ERROR_MESSAGE);
    }

    @Test
    void shouldSetNewHearingsLabelAndSelectorWhenNewHearingsHaveBeenAdded() {
        LocalDateTime date = now();
        UUID id = UUID.randomUUID();

        List<Element<HearingBooking>> oldHearingList = List.of(
            createHearingBookingElement(id, createHearing(date, date.plusDays(1))));

        List<Element<HearingBooking>> newHearingList = List.of(
            createHearingBookingElement(id, createHearing(date, date.plusDays(1))),
            createHearingBookingElement(UUID.randomUUID(), createHearing(date, date.plusDays(1))),
            createHearingBookingElement(UUID.randomUUID(), createHearing(date.plusDays(1), date.plusDays(2))));

        CallbackRequest callbackRequest = callbackRequestWithEditedBooking(newHearingList, oldHearingList);
        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(callbackRequest);

        assertThat(callbackResponse.getData().get(NEW_HEARING_LABEL.getKey()))
            .isEqualTo(String.format("Hearing 2: Final hearing %s\nHearing 3: Final hearing %s\n",
                formatLocalDateTimeBaseUsingFormat(date, DATE),
                formatLocalDateTimeBaseUsingFormat(date.plusDays(1), DATE)));

        Map<String, Object> expectedSerializedSelector = Map.of(
            "optionCount", "123", "option0Hidden", "Yes");

        assertThat(callbackResponse.getData().get(NEW_HEARING_SELECTOR.getKey())).isEqualTo(expectedSerializedSelector);
    }

    @Test
    void shouldResetNewHearingsLabelAndSelectorWhenNoNewHearingsHaveBeenAdded() {
        LocalDateTime date = now().plusDays(5);
        UUID hearingId = randomUUID();

        List<Element<HearingBooking>> newHearingBooking = listBookingWithStartDate(hearingId, date.plusDays(3));
        List<Element<HearingBooking>> oldHearingBooking = listBookingWithStartDate(hearingId, date);

        CallbackRequest request = callbackRequestWithEditedBooking(newHearingBooking, oldHearingBooking);
        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(request);

        assertThat(callbackResponse.getData().get(NEW_HEARING_LABEL.getKey())).isEqualTo("");
    }

    private CallbackRequest callbackRequestWithEditedBooking(List<Element<HearingBooking>> newHearings,
                                                             List<Element<HearingBooking>> oldHearings) {
        return CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .data(Map.of(HEARING_DETAILS_KEY, newHearings))
                .build())
            .caseDetailsBefore(CaseDetails.builder()
                .data(Map.of(HEARING_DETAILS_KEY, oldHearings))
                .build())
            .build();
    }

    private HearingBooking createHearing(LocalDateTime startDate, LocalDateTime endDate) {
        return HearingBooking.builder()
            .startDate(startDate)
            .endDate(endDate)
            .venue("Test venue")
            .type(FINAL)
            .build();
    }

    private AboutToStartOrSubmitCallbackResponse makeRequest(HearingBooking after) {
        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(12345L)
                .data(Map.of(HEARING_DETAILS_KEY, wrapElements(after)))
                .build())
            .caseDetailsBefore(CaseDetails.builder()
                .id(12345L)
                .data(Map.of(HEARING_DETAILS_KEY, wrapElements(EMPTY_HEARING_BOOKING)))
                .build())
            .build();

        return postMidEvent(request);
    }

    private List<Element<HearingBooking>> listBookingWithStartDate(UUID id, LocalDateTime date) {
        return Lists.newArrayList(element(id, createHearing(date, date.plusDays(1))));
    }

    private Element<HearingBooking> createHearingBookingElement(UUID id, HearingBooking hearingBooking) {
        return element(id, hearingBooking);
    }
}
