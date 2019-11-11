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
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.service.DraftCMOService;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBooking;

@ActiveProfiles("integration-test")
@WebMvcTest(DraftCMOController.class)
@OverrideAutoConfiguration(enabled = true)
@SuppressWarnings("unchecked")
class DraftCMOControllerTest {

    private static final String AUTH_TOKEN = "Bearer token";
    private static final String USER_ID = "1";
    @Autowired
    private DraftCMOService draftCMOService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper mapper;

    private final LocalDate date = LocalDate.now();
    private final List<Element<HearingBooking>> hearingDetails = createHearingBookings(date);

    @Test
    void aboutToStartCallbackShouldPopulateHearingDatesList() throws Exception {
        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .data(ImmutableMap.of("hearingDetails", hearingDetails))
                .build())
            .build();

        List<String> expected = Arrays.asList(
            draftCMOService.convertDate(date.plusDays(5)),
            draftCMOService.convertDate(date.plusDays(2)),
            draftCMOService.convertDate(date));

        actAndAssert(request, expected);
    }

    @Test
    void aboutToSubmitShouldPopulateHiddenHearingDateField() throws Exception {
        List<Element<HearingBooking>> hearingDetails = createHearingBookings(date);

        DynamicList dynamicHearingDates = draftCMOService.buildDynamicListFromHearingDetails(hearingDetails);

        dynamicHearingDates
            .setValue(
                DynamicListElement.builder()
                    .code(date.plusDays(5).toString())
                    .label(date.plusDays(5).toString())
                    .build());

        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .data(ImmutableMap.of("cmoHearingDateList", dynamicHearingDates))
                .build())
            .build();

        MvcResult response = makeRequest(request, "about-to-submit");

        AboutToStartOrSubmitCallbackResponse callbackResponse = mapper.readValue(response.getResponse()
            .getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);

        CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

        assertThat(caseData.getCaseManagementOrder().getHearingDate())
            .isEqualTo(date.plusDays(5).toString());

        assertThat(callbackResponse.getData().get("cmoHearingDateList"))
            .isNull();
    }

    private void actAndAssert(CallbackRequest request, List<String> expected) throws Exception {
        MvcResult response = makeRequest(request, "about-to-start");

        AboutToStartOrSubmitCallbackResponse callbackResponse = mapper.readValue(response.getResponse()
            .getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);

        assertThat(getHearingDates(callbackResponse)).isEqualTo(expected);
    }

    private List<String> getHearingDates(AboutToStartOrSubmitCallbackResponse callbackResponse) {
        Map<String, Object> cmoHearingResponse = mapper.convertValue(
            callbackResponse.getData().get("cmoHearingDateList"), Map.class);

        List<Map<String, Object>> listItemMap = mapper.convertValue(cmoHearingResponse.get("list_items"), List.class);

        return listItemMap.stream()
            .map(element -> mapper.convertValue(element, DynamicListElement.class))
            .map(DynamicListElement::getLabel).collect(Collectors.toList());
    }

    private MvcResult makeRequest(CallbackRequest request, String endpoint) throws Exception {
        return mockMvc
            .perform(post("/callback/draft-cmo/" + endpoint)
                .header("authorization", AUTH_TOKEN)
                .header("user-id", USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn();
    }

    private List<Element<HearingBooking>> createHearingBookings(LocalDate date) {
        return ImmutableList.of(
            Element.<HearingBooking>builder()
                .id(UUID.randomUUID())
                .value(createHearingBooking(date.plusDays(5)))
                .build(),
            Element.<HearingBooking>builder()
                .id(UUID.randomUUID())
                .value(createHearingBooking(date.plusDays(2)))
                .build(),
            Element.<HearingBooking>builder()
                .id(UUID.randomUUID())
                .value(createHearingBooking(date))
                .build()
        );
    }
}
