package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ActiveProfiles("integration-test")
@WebMvcTest(HearingBookingDetailsController.class)
@OverrideAutoConfiguration(enabled = true)
class HearingBookingDetailsControllerTest extends AbstractControllerTest {

    private static final String ERROR_MESSAGE = "Enter a start date in the future";

    HearingBookingDetailsControllerTest() {
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

        AboutToStartOrSubmitCallbackResponse callbackResponse = makeRequest(createHearing(today, today.plusDays(1)));

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

    private HearingBooking createHearing(LocalDateTime startDate, LocalDateTime endDate) {
        return HearingBooking.builder()
            .startDate(startDate)
            .endDate(endDate)
            .build();
    }

    private AboutToStartOrSubmitCallbackResponse makeRequest(HearingBooking hearingDetail) throws Exception {
        Map<String, Object> map = mapper.readValue(mapper.writeValueAsString(hearingDetail),
            new TypeReference<>() {
            });

        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345L)
            .data(Map.of("hearingDetails", wrapElements(map)))
            .build();

        return postMidEvent(caseDetails);
    }
}
