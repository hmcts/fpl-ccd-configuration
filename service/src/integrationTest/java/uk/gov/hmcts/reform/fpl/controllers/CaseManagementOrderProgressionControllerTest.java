package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.Event;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.OrderAction;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.DateFormatterService;
import uk.gov.service.notify.NotificationClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_READY_FOR_JUDGE_REVIEW_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.ActionType.JUDGE_REQUESTED_CHANGE;
import static uk.gov.hmcts.reform.fpl.enums.ActionType.SEND_TO_ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.CASE_MANAGEMENT_ORDER_JUDICIARY;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.CASE_MANAGEMENT_ORDER_LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.SERVED_CASE_MANAGEMENT_ORDERS;
import static uk.gov.hmcts.reform.fpl.enums.Event.ACTION_CASE_MANAGEMENT_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.Event.DRAFT_CASE_MANAGEMENT_ORDER;
import static uk.gov.hmcts.reform.fpl.service.HearingBookingService.HEARING_DETAILS_KEY;

@ActiveProfiles("integration-test")
@WebMvcTest(CaseManagementOrderProgressionController.class)
@OverrideAutoConfiguration(enabled = true)
class CaseManagementOrderProgressionControllerTest extends AbstractControllerTest {
    @MockBean
    private NotificationClient notificationClient;

    @Autowired
    private DateFormatterService dateFormatterService;

    private static final UUID uuid = randomUUID();
    private static final Long CASE_ID = 12345L;
    private static final String CASE_REFERENCE = "12345";
    private static final String LOCAL_AUTHORITY_CODE = "example";
    private static final String FAMILY_MAN_CASE_NUMBER = "SACCCCCCCC5676576567";
    private static final String RESPONDENT_SURNAME = "Watson";
    private static final LocalDateTime YESTERDAY = LocalDateTime.now().minusDays(1);

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

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(buildCallbackRequest(caseDetails,
            ACTION_CASE_MANAGEMENT_ORDER));

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
            .data(caseDataMap(order))
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(buildCallbackRequest(caseDetails,
            ACTION_CASE_MANAGEMENT_ORDER));

        assertThat(response.getData())
            .containsOnlyKeys(SERVED_CASE_MANAGEMENT_ORDERS.getKey(), HEARING_DETAILS_KEY);
    }

    @Test
    void aboutToSubmitShouldBuildNotificationTemplateWhenSentToJudge() throws Exception {
        CaseManagementOrder order = CaseManagementOrder.builder()
            .status(SEND_TO_JUDGE)
            .id(uuid)
            .action(OrderAction.builder()
                .type(SEND_TO_ALL_PARTIES)
                .build())
            .build();

        CaseDetails caseDetails = CaseDetails.builder()
            .data(ImmutableMap.<String, Object>builder()
                .putAll(caseDataMap(order))
                .putAll(getNotificationData())
                .build())
            .id(CASE_ID)
            .build();

        postAboutToSubmitEvent(buildCallbackRequest(caseDetails, DRAFT_CASE_MANAGEMENT_ORDER));

        verify(notificationClient, times(1)).sendEmail(
            eq(CMO_READY_FOR_JUDGE_REVIEW_NOTIFICATION_TEMPLATE), eq("admin@family-court.com"),
            eq(expectedTemplateParameters()), eq(CASE_REFERENCE));
    }

    private Map<String, Object> caseDataMap(CaseManagementOrder order) {
        return ImmutableMap.of(
            CASE_MANAGEMENT_ORDER_JUDICIARY.getKey(), order,
            HEARING_DETAILS_KEY, List.of(Element.<HearingBooking>builder()
                .id(uuid)
                .value(HearingBooking.builder()
                    .startDate(YESTERDAY)
                    .build())
                .build()));
    }

    private Map<String, Object> getNotificationData() {
        return ImmutableMap.of(
            "cmoEventId", "draftCMO",
            "caseLocalAuthority", LOCAL_AUTHORITY_CODE,
            "familyManCaseNumber", FAMILY_MAN_CASE_NUMBER,
            "respondents1", ImmutableList.of(
                    ImmutableMap.of(
                    "id", "",
                    "value", Respondent.builder()
                        .party(RespondentParty.builder()
                            .lastName(RESPONDENT_SURNAME)
                            .build())
                        .build()
                    ))
        );
    }

    private CallbackRequest buildCallbackRequest(CaseDetails caseDetails, Event event) {
        return CallbackRequest.builder()
            .eventId(event.getId())
            .caseDetails(caseDetails)
            .build();
    }

    private Map<String, Object> expectedTemplateParameters() {
        return ImmutableMap.<String, Object>builder()
            .put("subjectLineWithHearingDate", buildSubjectLine())
            .put("reference", CASE_REFERENCE)
            .put("respondentLastName", RESPONDENT_SURNAME)
            .put("caseUrl", "http://fake-url/case/" + JURISDICTION + "/" + CASE_TYPE + "/12345")
            .build();
    }

    private String buildSubjectLine() {
        String hearingDate = dateFormatterService
            .formatLocalDateTimeBaseUsingFormat(YESTERDAY, "d MMM yyyy");

        return String.format("%s, %s, hearing %s", RESPONDENT_SURNAME, FAMILY_MAN_CASE_NUMBER, hearingDate);
    }
}
