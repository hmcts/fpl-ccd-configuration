package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.OrderAction;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.ActionType.JUDGE_REQUESTED_CHANGE;
import static uk.gov.hmcts.reform.fpl.enums.ActionType.SEND_TO_ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.CASE_MANAGEMENT_ORDER_JUDICIARY;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.CASE_MANAGEMENT_ORDER_LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.SERVED_CASE_MANAGEMENT_ORDERS;
import static uk.gov.hmcts.reform.fpl.enums.Event.ACTION_CASE_MANAGEMENT_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.HearingBookingKeys.HEARING_DETAILS;

@ActiveProfiles("integration-test")
@WebMvcTest(CaseManagementOrderProgressionController.class)
@OverrideAutoConfiguration(enabled = true)
class CaseManagementOrderProgressionControllerTest extends AbstractControllerTest {
    private static final UUID uuid = randomUUID();

    CaseManagementOrderProgressionControllerTest() {
        super("cmo-progression");
    }

    @Test
    void aboutToSubmitReturnCaseManagementOrdersToLocalAuthorityWhenChangesAreRequested() {
        CaseManagementOrder order = CaseManagementOrder.builder()
            .status(SEND_TO_JUDGE)
            .action(OrderAction.builder()
                .type(JUDGE_REQUESTED_CHANGE)
                .build())
            .build();

        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of(CASE_MANAGEMENT_ORDER_JUDICIARY.getKey(), order))
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(buildCallbackRequest(caseDetails));

        assertThat(response.getData()).containsOnlyKeys(CASE_MANAGEMENT_ORDER_LOCAL_AUTHORITY.getKey());
    }

    @Test
    void aboutToSubmitShouldPopulateListServedCaseManagementOrdersWhenSendsToAllParties() {
        CaseManagementOrder order = CaseManagementOrder.builder()
            .status(SEND_TO_JUDGE)
            .id(uuid)
            .action(OrderAction.builder()
                .type(SEND_TO_ALL_PARTIES)
                .build())
            .build();

        CaseDetails caseDetails = CaseDetails.builder()
            .data(caseDataMap(order, LocalDateTime.now().minusDays(1)))
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(buildCallbackRequest(caseDetails));

        assertThat(response.getData())
            .containsOnlyKeys(SERVED_CASE_MANAGEMENT_ORDERS.getKey(), HEARING_DETAILS.getKey());
    }

    private Map<String, Object> caseDataMap(CaseManagementOrder order, LocalDateTime localDateTime) {
        return ImmutableMap.of(
            CASE_MANAGEMENT_ORDER_JUDICIARY.getKey(), order,
            HEARING_DETAILS.getKey(), List.of(Element.<HearingBooking>builder()
                .id(uuid)
                .value(HearingBooking.builder()
                    .startDate(localDateTime)
                    .build())
                .build()));
    }

    private CallbackRequest buildCallbackRequest(CaseDetails caseDetails) {
        return CallbackRequest.builder()
            .eventId(ACTION_CASE_MANAGEMENT_ORDER.getId())
            .caseDetails(caseDetails)
            .build();
    }

}
