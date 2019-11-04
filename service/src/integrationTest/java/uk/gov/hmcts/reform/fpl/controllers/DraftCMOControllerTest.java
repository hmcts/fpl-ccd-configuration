package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Order;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.service.DateFormatterService;
import uk.gov.hmcts.reform.fpl.service.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.DraftCMOService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.*;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBooking;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;

@ActiveProfiles("integration-test")
@WebMvcTest(DraftCMOController.class)
@OverrideAutoConfiguration(enabled = true)
@SuppressWarnings("unchecked")
class DraftCMOControllerTest {

    @Autowired
    private DraftCMOService draftCMOService;

    @Autowired
    private DateFormatterService dateFormatterService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    private static final String AUTH_TOKEN = "Bearer token";
    private static final String USER_ID = "1";

    @Test
    void aboutToStartCallbackShouldFillTheHearingDatesList() throws Exception {
        String title = "example direction";

        List<Element<HearingBooking>> hearingBooking = createHearingBookings();

        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .data(ImmutableMap.of("hearingDetails", hearingBooking))
                .build())
            .build();

        List<String> expected = Arrays.asList(
            convertdateTopLocalFormat(LocalDate.now().plusDays(5)),
            convertdateTopLocalFormat(LocalDate.now().plusDays(2)),
            convertdateTopLocalFormat(LocalDate.now()));

        MvcResult response = makeRequest(request, "about-to-start");

        AboutToStartOrSubmitCallbackResponse callbackResponse = mapper.readValue(response.getResponse()
            .getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);

        Map<String, Object> cmoHearingResponse = mapper.convertValue(callbackResponse.getData().get("cmoHearingDateList"), Map.class);

        List<Map<String, Object>> listItemMap = mapper.convertValue(cmoHearingResponse.get("list_items"), List.class);

        List<String> returnedDates = listItemMap.stream()
            .map(element -> mapper.convertValue(element, DynamicListElement.class))
            .map(DynamicListElement::getCode).collect(Collectors.toList());
        assertThat(returnedDates).isEqualTo(expected);
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

    private String convertdateTopLocalFormat(LocalDate date)
    {
        return dateFormatterService.formatLocalDateToString(date, FormatStyle.MEDIUM);
    }

    private List<Element<HearingBooking>> createHearingBookings() {
        return ImmutableList.of(
            Element.<HearingBooking>builder()
                .id(UUID.randomUUID())
                .value(createHearingBooking(LocalDate.now().plusDays(5)))
                .build(),
            Element.<HearingBooking>builder()
                .id(UUID.randomUUID())
                .value(createHearingBooking(LocalDate.now().plusDays(2)))
                .build(),
            Element.<HearingBooking>builder()
                .id(UUID.randomUUID())
                .value(createHearingBooking(LocalDate.now()))
                .build()
        );
    }
}
