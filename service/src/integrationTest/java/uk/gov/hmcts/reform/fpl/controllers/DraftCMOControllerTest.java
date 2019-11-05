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
import uk.gov.hmcts.reform.fpl.service.DateFormatterService;
import uk.gov.hmcts.reform.fpl.service.DraftCMOService;

import java.time.LocalDate;
import java.time.format.FormatStyle;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createCaseManagementOrder;
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
    private DateFormatterService dateFormatterService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper mapper;

    private final LocalDate date = LocalDate.now();

    @Test
    void aboutToStartCallbackShouldFillTheHearingDatesList() throws Exception {

        List<Element<HearingBooking>> hearingBooking = createHearingBookings(date);

        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .data(ImmutableMap.of("hearingDetails", hearingBooking))
                .build())
            .build();

        List<String> expected = Arrays.asList(
            convertdateTopLocalFormat(date.plusDays(5)),
            convertdateTopLocalFormat(date.plusDays(2)),
            convertdateTopLocalFormat(date));

        MvcResult response = makeRequest(request, "about-to-start");

        AboutToStartOrSubmitCallbackResponse callbackResponse = mapper.readValue(response.getResponse()
            .getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);

        List<String> returnedDates = getReturnedDatesFromResponse(callbackResponse);
        assertThat(returnedDates).isEqualTo(expected);
    }

    @Test
    void aboutToStartCallbackShouldFillTheHearingDatesListWhenCmoNotNUll() throws Exception {

        List<Element<HearingBooking>> hearingBooking = createHearingBookings(date);

        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .data(ImmutableMap.of("hearingDetails", hearingBooking,
                    "caseManagementOrder", createCaseManagementOrder(
                        convertdateTopLocalFormat(date.plusDays(5)))
                    ))
                .build())
            .build();

        List<String> expected = Arrays.asList(
            convertdateTopLocalFormat(date.plusDays(5)),
            convertdateTopLocalFormat(date.plusDays(2)),
            convertdateTopLocalFormat(date));

        MvcResult response = makeRequest(request, "about-to-start");

        AboutToStartOrSubmitCallbackResponse callbackResponse = mapper.readValue(response.getResponse()
            .getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);

        List<String> returnedDates = getReturnedDatesFromResponse(callbackResponse);

        assertThat(returnedDates).isEqualTo(expected);
    }

    @Test
    void aboutToStartCallbackHearingDatesListDontHaveExistingCmoHearingDate() throws Exception {

        List<Element<HearingBooking>> hearingBooking = ImmutableList.of(
            Element.<HearingBooking>builder()
                .id(UUID.randomUUID())
                .value(createHearingBooking(date.plusDays(2)))
                .build(),
            Element.<HearingBooking>builder()
                .id(UUID.randomUUID())
                .value(createHearingBooking(date))
                .build());

        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .data(ImmutableMap.of("hearingDetails", hearingBooking,
                    "caseManagementOrder", createCaseManagementOrder(
                        convertdateTopLocalFormat(date.plusDays(5)))
                ))
                .build())
            .build();

        List<String> expected = Arrays.asList(
            convertdateTopLocalFormat(date.plusDays(2)),
            convertdateTopLocalFormat(date),
            convertdateTopLocalFormat(date.plusDays(5)));

        MvcResult response = makeRequest(request, "about-to-start");

        AboutToStartOrSubmitCallbackResponse callbackResponse = mapper.readValue(response.getResponse()
            .getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);

        List<String> returnedDates = getReturnedDatesFromResponse(callbackResponse);

        assertThat(returnedDates).isEqualTo(expected);
    }

    @Test
    void aboutToSubmitShouldPopulateHiddenHearingDateField() throws Exception {
        List<Element<HearingBooking>> hearingDetails = createHearingBookings(date);

        DynamicList hearingDatesDynamic = draftCMOService.makeHearingDateList(hearingDetails);

        hearingDatesDynamic
            .setValue(
                DynamicListElement.builder()
                    .code(date.plusDays(5).toString())
                    .label(date.plusDays(5).toString())
                    .build());

        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .data(ImmutableMap.of("cmoHearingDateList", hearingDatesDynamic))
                .build())
            .build();

        MvcResult response = makeRequest(request, "about-to-submit");

        AboutToStartOrSubmitCallbackResponse callbackResponse = mapper.readValue(response.getResponse()
            .getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);

        CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

        assertThat(caseData.getCaseManagementOrder().getHearingDate())
            .isEqualTo(date.plusDays(5).toString());
    }

    private List<String> getReturnedDatesFromResponse(AboutToStartOrSubmitCallbackResponse callbackResponse) {
        Map<String, Object> cmoHearingResponse = mapper.convertValue(
            callbackResponse.getData().get("cmoHearingDateList"), Map.class);

        List<Map<String, Object>> listItemMap = mapper.convertValue(cmoHearingResponse.get("list_items"), List.class);

        return listItemMap.stream()
            .map(element -> mapper.convertValue(element, DynamicListElement.class))
            .map(DynamicListElement::getCode).collect(Collectors.toList());
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

    private String convertdateTopLocalFormat(LocalDate date) {
        return dateFormatterService.formatLocalDateToString(date, FormatStyle.MEDIUM);
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
