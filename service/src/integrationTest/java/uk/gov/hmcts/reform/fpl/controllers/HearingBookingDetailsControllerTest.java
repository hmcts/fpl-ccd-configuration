package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
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
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.configuration.HearingVenue;
import uk.gov.hmcts.reform.fpl.service.HearingVenueLookupService;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
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
    private static final String DATE_ERROR_MESSAGE = "Enter a future date";
    private static final String VENUE_ERROR_MESSAGE = "Select a hearing venue";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private HearingVenueLookupService lookupService;

    private List<HearingVenue> hearingVenueList;

    @BeforeEach
    void setUp() throws IOException {
        hearingVenueList = lookupService.getHearingVenues();
    }

    @Test
    void shouldReturnAnErrorWhenHearingDateIsSetToYesterday() throws Exception {
        LocalDate yesterday = LocalDate.now().minusDays(1);

        AboutToStartOrSubmitCallbackResponse callbackResponse = makeRequest(createHearing(yesterday, null));

        assertThat(callbackResponse.getErrors()).contains(DATE_ERROR_MESSAGE);
    }

    @Test
    void shouldNotReturnAnErrorWhenHearingDateIsSetToTomorrow() throws Exception {
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        AboutToStartOrSubmitCallbackResponse callbackResponse = makeRequest(createHearing(tomorrow, null));

        assertThat(callbackResponse.getErrors()).doesNotContain(DATE_ERROR_MESSAGE);
    }

    @Test
    void shouldReturnAnErrorWhenHearingDateIsSetToToday() throws Exception {
        LocalDate today = LocalDate.now();

        AboutToStartOrSubmitCallbackResponse callbackResponse = makeRequest(createHearing(today, null));

        assertThat(callbackResponse.getErrors()).contains(DATE_ERROR_MESSAGE);
    }

    @Test
    void shouldReturnAnErrorWhenHearingDateIsSetInDistantPast() throws Exception {
        LocalDate distantPast = LocalDate.now().minusYears(10000);

        AboutToStartOrSubmitCallbackResponse callbackResponse = makeRequest(createHearing(distantPast, null));

        assertThat(callbackResponse.getErrors()).contains(DATE_ERROR_MESSAGE);
    }

    @Test
    void shouldNotReturnAnErrorWhenHearingDateIsSetInDistantFuture() throws Exception {
        LocalDate distantFuture = LocalDate.now().plusYears(1000);

        AboutToStartOrSubmitCallbackResponse callbackResponse = makeRequest(createHearing(distantFuture, null));

        assertThat(callbackResponse.getErrors()).doesNotContain(DATE_ERROR_MESSAGE);
    }

    @Test
    void shouldReturnAnErrorWhenHearingVenueIsDefault() throws Exception {
        DynamicList venueList = DynamicList.toDynamicList(hearingVenueList);

        AboutToStartOrSubmitCallbackResponse callbackResponse = makeRequest(createHearing(null, venueList));

        assertThat(callbackResponse.getErrors()).contains(VENUE_ERROR_MESSAGE);
    }

    @Test
    void shouldNotReturnAnErrorWhenHearingVenueIsSelected() throws Exception {
        DynamicList venueList = DynamicList.toDynamicList(hearingVenueList, 0);

        AboutToStartOrSubmitCallbackResponse callbackResponse = makeRequest(createHearing(null, venueList));

        assertThat(callbackResponse.getErrors()).doesNotContain(VENUE_ERROR_MESSAGE);
    }

    private HearingBooking createHearing(LocalDate hearingDate,
                                         DynamicList venueList) {
        return HearingBooking.builder()
            .date(hearingDate)
            .venueList(venueList)
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
