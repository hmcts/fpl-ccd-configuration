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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.UUID.fromString;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBooking;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createOthers;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createRespondents;

@ActiveProfiles("integration-test")
@WebMvcTest(DraftCMOController.class)
@OverrideAutoConfiguration(enabled = true)
@SuppressWarnings("unchecked")
class DraftCMOControllerTest {
    private static final String AUTH_TOKEN = "Bearer token";
    private static final String USER_ID = "1";
    private static final LocalDateTime TODAYS_DATE = LocalDateTime.now();
    private final List<Element<HearingBooking>> hearingDetails = createHearingBookings(TODAYS_DATE);
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofLocalizedDate(
        FormatStyle.MEDIUM).localizedBy(Locale.UK);
    @Autowired
    private DraftCMOService draftCMOService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper mapper;

    @Test
    void aboutToStartCallbackShouldPopulateHearingDatesListAndSetAssigneeDropdownKeys() throws Exception {
        Map<String, Object> data = ImmutableMap.of(
            "hearingDetails", hearingDetails,
            "respondents1", createRespondents(),
            "others", createOthers());

        List<String> expected = Arrays.asList(
            TODAYS_DATE.plusDays(5).format(dateTimeFormatter),
            TODAYS_DATE.plusDays(2).format(dateTimeFormatter),
            TODAYS_DATE.format(dateTimeFormatter));

        AboutToStartOrSubmitCallbackResponse callbackResponse = getResponse(data, "about-to-start");

        assertThat(getHearingDates(callbackResponse)).isEqualTo(expected);

        String parentsAndRespondentsKeyCmo =
            mapper.convertValue(callbackResponse.getData().get("parentsAndRespondentsDropdownKeyCMO"), String.class);
        String otherPartiesKeyCMO =
            mapper.convertValue(callbackResponse.getData().get("otherPartiesDropdownKeyCMO"), String.class);

        assertThat(parentsAndRespondentsKeyCmo).contains(
            "Respondent 1 - Timothy Jones",
            "Respondent 2 - Sarah Simpson");

        assertThat(otherPartiesKeyCMO).contains(
            "Person 1 - Kyle Stafford",
            "Other Person 2 - Sarah Simpson");
    }

    @Test
    void aboutToSubmitShouldPopulateHiddenHearingDateField() throws Exception {
        List<Element<HearingBooking>> hearingDetails = createHearingBookings(TODAYS_DATE);

        DynamicList dynamicHearingDates = draftCMOService.buildDynamicListFromHearingDetails(hearingDetails);

        dynamicHearingDates
            .setValue(
                DynamicListElement.builder()
                    .code(fromString("b15eb00f-e151-47f2-8e5f-374cc6fc2657"))
                    .label(TODAYS_DATE.plusDays(5).toString())
                    .build());

        Map<String, Object> data = ImmutableMap.of("cmoHearingDateList", dynamicHearingDates);

        AboutToStartOrSubmitCallbackResponse callbackResponse = getResponse(data, "about-to-submit");

        assertThat(callbackResponse.getData()).doesNotContainKey("cmoHearingDateList");

        CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

        assertThat(caseData.getCaseManagementOrder()).extracting("id", "hearingDate")
            .containsExactly(fromString("b15eb00f-e151-47f2-8e5f-374cc6fc2657"), TODAYS_DATE.plusDays(5).toString());
    }

    private List<String> getHearingDates(AboutToStartOrSubmitCallbackResponse callbackResponse) {
        Map<String, Object> cmoHearingResponse = mapper.convertValue(
            callbackResponse.getData().get("cmoHearingDateList"), Map.class);

        List<Map<String, Object>> listItemMap = mapper.convertValue(cmoHearingResponse.get("list_items"), List.class);

        return listItemMap.stream()
            .map(element -> mapper.convertValue(element, DynamicListElement.class))
            .map(DynamicListElement::getLabel).collect(Collectors.toList());
    }

    private AboutToStartOrSubmitCallbackResponse getResponse(
        final Map<String, Object> data, final String endpoint) throws Exception {
        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .data(data)
                .build())
            .build();

        MvcResult response = makeRequest(request, endpoint);

        return mapper.readValue(response.getResponse()
            .getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);
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

    private List<Element<HearingBooking>> createHearingBookings(LocalDateTime date) {
        return ImmutableList.of(
            Element.<HearingBooking>builder()
                .id(fromString("b15eb00f-e151-47f2-8e5f-374cc6fc2657"))
                .value(createHearingBooking(date.plusDays(5), date.plusDays(6)))
                .build(),
            Element.<HearingBooking>builder()
                .id(UUID.randomUUID())
                .value(createHearingBooking(date.plusDays(2), date.plusDays(3)))
                .build(),
            Element.<HearingBooking>builder()
                .id(UUID.randomUUID())
                .value(createHearingBooking(date, date.plusDays(1)))
                .build()
        );
    }
}
