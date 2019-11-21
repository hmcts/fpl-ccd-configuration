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

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBooking;

@ActiveProfiles("integration-test")
@WebMvcTest(NoticeOfProceedingsController.class)
@OverrideAutoConfiguration(enabled = true)
class NoticeOfProceedingsControllerAboutToStartTest {

    private static final String AUTH_TOKEN = "Bearer token";
    private static final String USER_ID = "1";
    private static final LocalDateTime TODAYS_DATE = LocalDateTime.now();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DateFormatterService dateFormatterService;

    @Test
    void shouldReturnErrorsWhenFamilymanNumberIsNotProvided() throws Exception {
        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(12345L)
                .data(ImmutableMap.of(
                    "hearingDetails", createHearingBookings()
                ))
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = objectMapper.readValue(
            makeRequest(request).getResponse().getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);

        assertThat(callbackResponse.getErrors()).containsOnlyOnce("Enter Familyman case number");
    }

    @Test
    void shouldUpdateProceedingLabelToIncludeHearingBookingDetailsDate() throws Exception {
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
            .formatLocalDateTimeBaseUsingFormat(TODAYS_DATE, "d MMMM yyyy"));

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
                .value(createHearingBooking(TODAYS_DATE.plusDays(5), TODAYS_DATE.plusHours(6)))
                .build(),
            Element.<HearingBooking>builder()
                .id(UUID.randomUUID())
                .value(createHearingBooking(TODAYS_DATE.plusDays(2), TODAYS_DATE.plusMinutes(45)))
                .build(),
            Element.<HearingBooking>builder()
                .id(UUID.randomUUID())
                .value(createHearingBooking(TODAYS_DATE, TODAYS_DATE.plusHours(2)))
                .build()
        );
    }
}
