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
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.OrderAction;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.fpl.enums.ActionType.JUDGE_REQUESTED_CHANGE;
import static uk.gov.hmcts.reform.fpl.enums.ActionType.SEND_TO_ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderErrorMessages.HEARING_NOT_COMPLETED;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.CASE_MANAGEMENT_ORDER_JUDICIARY;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.CASE_MANAGEMENT_ORDER_LOCAL_AUTHORITY;

@ActiveProfiles("integration-test")
@WebMvcTest(CaseManagementOrderProgressionController.class)
@OverrideAutoConfiguration(enabled = true)
class CaseManagementOrderProgressionControllerTest {
    private static final UUID uuid = randomUUID();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Test
    void aboutToSubmitReturnCaseManagementOrdersToLocalAuthorityWhenChangesAreRequested() throws Exception {
        CaseManagementOrder order = CaseManagementOrder.builder()
            .action(OrderAction.builder()
                .type(JUDGE_REQUESTED_CHANGE)
                .build())
            .build();

        Map<String, Object> data = ImmutableMap.of(CASE_MANAGEMENT_ORDER_JUDICIARY.getKey(), order);

        AboutToStartOrSubmitCallbackResponse response = makeRequest(buildCallbackRequest(data));

        assertThat(response.getData()).containsOnlyKeys(CASE_MANAGEMENT_ORDER_LOCAL_AUTHORITY.getKey());
    }


    @Test
    void aboutToSubmitShouldReturnErrorsWhenJudgeSendsToAllPartiesAndHearingIsNotComplete() throws Exception {
        CaseManagementOrder order = CaseManagementOrder.builder()
            .id(uuid)
            .action(OrderAction.builder()
                .type(SEND_TO_ALL_PARTIES)
                .build())
            .build();

        Map<String, Object> data = ImmutableMap.of(
            CASE_MANAGEMENT_ORDER_JUDICIARY.getKey(), order,
            "hearingDetails", ImmutableList.of(Element.<HearingBooking>builder()
                .id(uuid)
                .value(HearingBooking.builder()
                    .startDate(LocalDateTime.now().minusDays(1))
                    .build())
                .build()));

        AboutToStartOrSubmitCallbackResponse response = makeRequest(buildCallbackRequest(data));

        assertThat(response.getErrors()).containsOnly(HEARING_NOT_COMPLETED.getValue());
    }

    @Test
    void aboutToSubmitShouldPopulateListServedCaseManagementOrdersWhenSendsToAllParties() throws Exception {
        CaseManagementOrder order = CaseManagementOrder.builder()
            .id(uuid)
            .action(OrderAction.builder()
                .type(SEND_TO_ALL_PARTIES)
                .build())
            .build();

        Map<String, Object> data = ImmutableMap.of(
            CASE_MANAGEMENT_ORDER_JUDICIARY.getKey(), order,
            "hearingDetails", ImmutableList.of(Element.<HearingBooking>builder()
                .id(uuid)
                .value(HearingBooking.builder()
                    .startDate(LocalDateTime.now().plusDays(1))
                    .build())
                .build()));

        AboutToStartOrSubmitCallbackResponse response = makeRequest(buildCallbackRequest(data));

        assertThat(response.getData()).containsOnlyKeys("servedCaseManagementOrders", "hearingDetails");
    }

    private CallbackRequest buildCallbackRequest(Map<String, Object> data) {
        return CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .data(data)
                .build())
            .build();
    }

    private AboutToStartOrSubmitCallbackResponse makeRequest(CallbackRequest request)
        throws Exception {
        MvcResult result = mockMvc
            .perform(post("/callback/cmo-progression/" + "about-to-submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn();

        return mapper.readValue(result.getResponse()
            .getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);
    }
}
