package uk.gov.hmcts.reform.fpl.controllers;

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
import uk.gov.hmcts.reform.fpl.service.DateFormatterService;

import java.time.LocalDate;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("integration-test")
@WebMvcTest(NoticeOfProceedingsController.class)
@OverrideAutoConfiguration(enabled = true)
class NoticeOfProceedingsControllerAboutToStartTest {

    private static final String AUTH_TOKEN = "Bearer token";
    private static final String USER_ID = "1";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DateFormatterService dateFormatterService;

    @Test
    void shouldReturnErrorsWhenFamilymanNumberOrHearingBookingDetailsIsNotProvided() throws Exception {
        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(12345L)
                .data(ImmutableMap.of())
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = objectMapper.readValue(
            makeRequest(request).getResponse().getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);

        assertThat(callbackResponse.getErrors()).containsOnlyOnce(
            "Enter Familyman case number",
            "Enter hearing details"
        );
    }

    @Test
    void shouldReturnNoErrorsWhenFamilymanNumberAndHearingIsProvided() throws Exception {
        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(12345L)
                .data(ImmutableMap.of(
                    "hearingDetails", createHearingBookings(),
                    "familyManCaseNumber", "123"
                ))
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = objectMapper.readValue(
            makeRequest(request).getResponse().getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);

        assertThat(callbackResponse.getErrors()).isEmpty();
    }

    @Test
    void shouldUpdateProceedingLabelToIncludeHearingDate() throws Exception {
        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(12345L)
                .data(ImmutableMap.of(
                    "hearingDetails", createHearingBookings(),
                    "familyManCaseNumber", "123"
                ))
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = objectMapper.readValue(
            makeRequest(request).getResponse().getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);

        String proceedingLabel = callbackResponse.getData().get("proceedingLabel").toString();

        String expectedContent = String.format("The case management hearing will be on the %s.", dateFormatterService
            .formatLocalDateToString(LocalDate.now().plusDays(1), FormatStyle.LONG));

        assertThat(proceedingLabel).isEqualTo(expectedContent);
    }

    private MvcResult makeRequest(CallbackRequest request) throws Exception {
        return mockMvc
            .perform(post("/callback/notice-of-proceedings/about-to-start")
                .header("authorization", AUTH_TOKEN)
                .header("user-id", USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn();
    }

    private List<Element<HearingBooking>> createHearingBookings() {
        return ImmutableList.of(
            Element.<HearingBooking>builder()
                .id(UUID.randomUUID())
                .value(HearingBooking.builder()
                    .date(LocalDate.now().plusDays(5))
                    .venue("Venue 1")
                    .preHearingAttendance("This is usually one hour before the hearing")
                    .time("09.15")
                    .build())
                .build(),
            Element.<HearingBooking>builder()
                .id(UUID.randomUUID())
                .value(HearingBooking.builder()
                    .date(LocalDate.now().plusDays(2))
                    .venue("Venue 2")
                    .preHearingAttendance("This is usually one hour before the hearing")
                    .time("09.15")
                    .build())
                .build(),
            Element.<HearingBooking>builder()
                .id(UUID.randomUUID())
                .value(HearingBooking.builder()
                    .date(LocalDate.now().plusDays(1))
                    .venue("Venue 3")
                    .preHearingAttendance("This is usually one hour before the hearing")
                    .time("09.15")
                    .build())
                .build()
        );
    }
}
