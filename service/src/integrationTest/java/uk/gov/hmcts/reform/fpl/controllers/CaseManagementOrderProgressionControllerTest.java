package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.OrderAction;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_REJECTED_BY_JUDGE_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.ActionType.JUDGE_REQUESTED_CHANGE;
import static uk.gov.hmcts.reform.fpl.enums.ActionType.SEND_TO_ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.CASE_MANAGEMENT_ORDER_JUDICIARY;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.CASE_MANAGEMENT_ORDER_LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.SERVED_CASE_MANAGEMENT_ORDERS;
import static uk.gov.hmcts.reform.fpl.enums.Event.ACTION_CASE_MANAGEMENT_ORDER;
import static uk.gov.hmcts.reform.fpl.service.HearingBookingService.HEARING_DETAILS_KEY;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBookings;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createRespondents;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.formatCaseURL;

@ActiveProfiles("integration-test")
@WebMvcTest(CaseManagementOrderProgressionController.class)
@OverrideAutoConfiguration(enabled = true)
class CaseManagementOrderProgressionControllerTest extends AbstractControllerTest {
    private static final UUID uuid = randomUUID();
    private static final String LOCAL_AUTHORITY_CODE = "example";
    private static final String LOCAL_AUTHORITY_EMAIL_ADDRESS = "local-authority@local-authority.com";
    private static final String FAMILY_MAN_CASE_NUMBER = "SACCCCCCCC5676576567";

    private static final Long caseId = 12345L;
    private final LocalDateTime testDate = LocalDateTime.of(2020, 2, 1, 12, 30);

    @MockBean
    private NotificationClient notificationClient;

    CaseManagementOrderProgressionControllerTest() {
        super("cmo-progression");
    }

    @Test
    void aboutToSubmitReturnsCaseManagementOrdersToLocalAuthorityWhenChangesAreRequested()
        throws NotificationClientException {

        CaseManagementOrder order = CaseManagementOrder.builder()
            .status(SEND_TO_JUDGE)
            .action(OrderAction.builder()
                .type(JUDGE_REQUESTED_CHANGE)
                .changeRequestedByJudge("Please make this change XYZ")
                .build())
            .build();

        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345L)
            .data(Map.of(CASE_MANAGEMENT_ORDER_JUDICIARY.getKey(), order,
                "hearingDetails", createHearingBookings(testDate, testDate.plusHours(4)),
                "respondents1", createRespondents(),
                "caseLocalAuthority", LOCAL_AUTHORITY_CODE,
                "familyManCaseNumber", FAMILY_MAN_CASE_NUMBER))
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(buildCallbackRequest(caseDetails));

        assertThat(response.getData()).containsOnlyKeys(CASE_MANAGEMENT_ORDER_LOCAL_AUTHORITY.getKey(),
            "hearingDetails", "respondents1", "caseLocalAuthority", "familyManCaseNumber");

        verify(notificationClient).sendEmail(
            eq(CMO_REJECTED_BY_JUDGE_TEMPLATE), eq(LOCAL_AUTHORITY_EMAIL_ADDRESS),
            eq(expectedNotificationParameters()), eq(caseId.toString()));
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
            .containsOnlyKeys(SERVED_CASE_MANAGEMENT_ORDERS.getKey(), HEARING_DETAILS_KEY);
    }

    private Map<String, Object> expectedNotificationParameters() {
        return ImmutableMap.<String, Object>builder()
            .putAll(commonNotificationParameters())
            .put("requestedChanges", "Please make this change XYZ")
            .build();
    }

    private Map<String, Object> commonNotificationParameters() {
        final String subjectLine = "Jones, SACCCCCCCC5676576567," + " hearing 1 Feb 2020";
        return ImmutableMap.<String, Object>builder()
            .put("subjectLineWithHearingDate", subjectLine)
            .put("reference", caseId.toString())
            .put("caseUrl", formatCaseURL("http://fake-url", caseId))
            .build();
    }

    private Map<String, Object> caseDataMap(CaseManagementOrder order, LocalDateTime localDateTime) {
        return ImmutableMap.of(
            CASE_MANAGEMENT_ORDER_JUDICIARY.getKey(), order,
            HEARING_DETAILS_KEY, List.of(Element.<HearingBooking>builder()
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
