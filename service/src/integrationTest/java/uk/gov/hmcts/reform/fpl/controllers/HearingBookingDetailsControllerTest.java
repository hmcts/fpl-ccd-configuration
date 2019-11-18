package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("integration-test")
@WebMvcTest(HearingBookingDetailsController.class)
@OverrideAutoConfiguration(enabled = true)
class HearingBookingDetailsControllerTest {

    private static final String AUTH_TOKEN = "Bearer token";
    private static final String USER_ID = "1";
    private static final String ERROR_MESSAGE = "Enter a start date in the future";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
        Map<String, Object> map = objectMapper.readValue(objectMapper.writeValueAsString(hearingDetail),
            new TypeReference<>() {
            });

        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(12345L)
                .data(ImmutableMap.<String, Object>builder()
                    .put("hearingDetails",
                        ImmutableList.of(
                            Element.builder()
                                .value(map)
                                .build()))
                    .build())
                .build())
            .build();

        MvcResult response = mockMvc
            .perform(post("/callback/add-hearing-bookings/mid-event")
                .header("authorization", AUTH_TOKEN)
                .header("user-id", USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn();

        return objectMapper.readValue(response.getResponse()
            .getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);
    }
}
