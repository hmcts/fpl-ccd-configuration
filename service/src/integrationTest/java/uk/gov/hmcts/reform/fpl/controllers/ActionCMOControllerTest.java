package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.assertj.core.api.AssertionsForClassTypes;
import org.assertj.core.api.AssertionsForInterfaceTypes;
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
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrderAction;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.service.CaseManageOrderActionService;
import uk.gov.hmcts.reform.fpl.service.DateFormatterService;
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
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createCmoDirections;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createDraftCaseManagementOrder;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBookings;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createOthers;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createRespondents;

@ActiveProfiles("integration-test")
@WebMvcTest(ActionCMOController.class)
@OverrideAutoConfiguration(enabled = true)
@SuppressWarnings("unchecked")
public class ActionCMOControllerTest {
    private static final String AUTH_TOKEN = "Bearer token";
    private static final String USER_ID = "1";
    private static final LocalDateTime TODAYS_DATE = LocalDateTime.now();
    private final List<Element<HearingBooking>> hearingDetails = createHearingBookings(TODAYS_DATE);
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofLocalizedDate(
        FormatStyle.MEDIUM).localizedBy(Locale.UK);

    @Autowired
    private DraftCMOService draftCMOService;

    @Autowired
    private CaseManageOrderActionService caseManageOrderActionService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DateFormatterService dateFormatterService;

    @Test
    void aboutToStartShouldReturnDraftCaseManagementOrderForAction() throws Exception {
        CaseManagementOrder caseManagementOrder = createDraftCaseManagementOrder();

        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .data(ImmutableMap.of(
                    "hearingDetails", hearingDetails,
                    "caseManagementOrder", caseManagementOrder))
                .build())
            .build();

        MvcResult response = makeRequest(request, "about-to-start");
        AboutToStartOrSubmitCallbackResponse callbackResponse = objectMapper.readValue(
            response.getResponse().getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);

        CaseData convertedCasData = objectMapper.convertValue(callbackResponse.getData(), CaseData.class);

        assertThat(convertedCasData).isNotNull();

        List<String> expected = Arrays.asList(
            TODAYS_DATE.plusDays(5).format(dateTimeFormatter),
            TODAYS_DATE.plusDays(2).format(dateTimeFormatter),
            TODAYS_DATE.format(dateTimeFormatter));

        AssertionsForInterfaceTypes.assertThat(getHearingDates(callbackResponse)).isEqualTo(expected);
        // TODO: 04/12/2019 add more assert and include tests for other endpoints
    }

    @Test
    void aboutToSubmitShouldAppendHearingStartDateWhenCmoHasBeenActioned() throws Exception {
        Map<String, Object> data = ImmutableMap.of(
            "hearingDetails", hearingDetails,
            "respondents1", createRespondents(),
            "others", createOthers(),
            "caseManagementOrder", buildActionedCmo(fromString("ecac3668-8fa6-4ba0-8894-2114601a3e31")));

        AboutToStartOrSubmitCallbackResponse callbackResponse = getResponse(data, "about-to-submit");

        String date = dateFormatterService.formatLocalDateTimeBaseUsingFormat(TODAYS_DATE, "d MMMM");
        String time = dateFormatterService.formatLocalDateTimeBaseUsingFormat(TODAYS_DATE, "h:mma");
        String expectedLabel = String.format("The next hearing date is on %s at %s", date, time);

        AssertionsForClassTypes.assertThat(callbackResponse.getData().get("nextHearingDateLabel")).isEqualTo(expectedLabel);
    }

    private List<String> getHearingDates(AboutToStartOrSubmitCallbackResponse callbackResponse) {
        Map<String, Object> cmoHearingResponse = objectMapper.convertValue(
            callbackResponse.getData().get("nextHearingDateList"), Map.class);

        List<Map<String, Object>> listItemMap = objectMapper.convertValue(cmoHearingResponse.get("list_items"), List.class);

        return listItemMap.stream()
            .map(element -> objectMapper.convertValue(element, DynamicListElement.class))
            .map(DynamicListElement::getLabel).collect(Collectors.toList());
    }

    private CaseManagementOrder buildActionedCmo(UUID hearingId) {
        return CaseManagementOrder.builder()
            .directions(createCmoDirections())
            .caseManagementOrderAction(CaseManagementOrderAction.builder()
                .id(hearingId)
                .build())
            .build();
    }

    private AboutToStartOrSubmitCallbackResponse getResponse(
        final Map<String, Object> data, final String endpoint) throws Exception {
        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .data(data)
                .build())
            .build();

        MvcResult response = makeRequest(request, endpoint);

        return objectMapper.readValue(response.getResponse()
            .getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);
    }

    private MvcResult makeRequest(final CallbackRequest request, final String endpoint) throws Exception {
        return mockMvc
            .perform(post("/callback/action-cmo/" + endpoint)
                .header("authorization", AUTH_TOKEN)
                .header("user-id", USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn();
    }
}
