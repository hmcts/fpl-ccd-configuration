package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.ActionType;
import uk.gov.hmcts.reform.fpl.enums.CMOStatus;
import uk.gov.hmcts.reform.fpl.enums.Event;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.OrderAction;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_READY_FOR_JUDGE_REVIEW_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_READY_FOR_PARTY_REVIEW_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_REJECTED_BY_JUDGE_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.ActionType.JUDGE_REQUESTED_CHANGE;
import static uk.gov.hmcts.reform.fpl.enums.ActionType.SEND_TO_ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.PARTIES_REVIEW;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SELF_REVIEW;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.CASE_MANAGEMENT_ORDER_JUDICIARY;
import static uk.gov.hmcts.reform.fpl.enums.Event.ACTION_CASE_MANAGEMENT_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.Event.DRAFT_CASE_MANAGEMENT_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createDocumentReference;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBookings;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createRespondents;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_SHORT_MONTH;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.formatCaseUrl;
import static uk.gov.hmcts.reform.fpl.utils.matchers.JsonMatcher.eqJson;

@ActiveProfiles("integration-test")
@WebMvcTest(CaseManagementOrderProgressionController.class)
@OverrideAutoConfiguration(enabled = true)
class CaseManagementOrderProgressionControllerTest extends AbstractControllerTest {
    private static final UUID HEARING_ID = randomUUID();
    private static final String LOCAL_AUTHORITY_CODE = "example";
    private static final String LOCAL_AUTHORITY_EMAIL_ADDRESS = "local-authority@local-authority.com";
    private static final String FAMILY_MAN_CASE_NUMBER = "SACCCCCCCC5676576567";
    private static final String HMCTS_ADMIN_INBOX = "admin@family-court.com";
    private static final String CTSC_ADMIN_INBOX = "FamilyPublicLaw+ctsc@gmail.com";
    private static final byte[] PDF = {1, 2, 3, 4, 5};
    private static final Long CASE_ID = 12345L;

    private LocalDateTime futureDate;

    @MockBean
    private NotificationClient notificationClient;

    @MockBean
    private DocumentDownloadService documentDownloadService;

    CaseManagementOrderProgressionControllerTest() {
        super("cmo-progression");
    }

    @BeforeEach
    void setUp() {
        futureDate = now().plusDays(1);
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

        CaseDetails caseDetails = buildCaseDetails(order, ACTION_CASE_MANAGEMENT_ORDER, "No");

        CaseData caseDataBefore = mapper.convertValue(caseDetails.getData(), CaseData.class);
        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(
            buildCallbackRequest(caseDetails, ACTION_CASE_MANAGEMENT_ORDER));
        CaseData responseData = mapper.convertValue(response.getData(), CaseData.class);

        assertThat(responseData.getCaseManagementOrder().getStatus()).isEqualTo(SELF_REVIEW);
        assertResponseDataContainsExpectedFields(responseData, caseDataBefore);

        verify(notificationClient).sendEmail(
            CMO_REJECTED_BY_JUDGE_TEMPLATE, LOCAL_AUTHORITY_EMAIL_ADDRESS,
            expectedJudgeRejectedNotificationParameters(), CASE_ID.toString());
    }

    @Test
    void aboutToSubmitShouldNotNotifyLocalAuthorityWhenChangesAreNotRequested()
        throws NotificationClientException {

        CaseManagementOrder order = buildOrder(SEND_TO_JUDGE, ActionType.SELF_REVIEW);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345L)
            .data(Map.of(CASE_MANAGEMENT_ORDER_JUDICIARY.getKey(), order))
            .build();

        postAboutToSubmitEvent(buildCallbackRequest(caseDetails, ACTION_CASE_MANAGEMENT_ORDER));

        verify(notificationClient, never()).sendEmail(
            CMO_REJECTED_BY_JUDGE_TEMPLATE, LOCAL_AUTHORITY_EMAIL_ADDRESS,
            expectedJudgeRejectedNotificationParameters(), CASE_ID.toString());
    }

    @Test
    void aboutToSubmitShouldPopulateListServedCaseManagementOrdersWhenSendsToAllParties() {
        CaseManagementOrder order = buildOrder(SEND_TO_JUDGE, SEND_TO_ALL_PARTIES);

        CaseDetails caseDetails = buildCaseDetails(order, ACTION_CASE_MANAGEMENT_ORDER, "No");

        CaseData caseDataBefore = mapper.convertValue(caseDetails.getData(), CaseData.class);
        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(buildCallbackRequest(caseDetails,
            ACTION_CASE_MANAGEMENT_ORDER));
        CaseData responseData = mapper.convertValue(response.getData(), CaseData.class);

        assertThat(unwrapElements(responseData.getServedCaseManagementOrders())).containsOnly(order);

        assertResponseDataContainsExpectedFields(responseData, caseDataBefore);
    }

    @Test
    void aboutToSubmitShouldNotifyRepresentativesWhenStatusIsSendToParties() throws Exception {
        given(documentDownloadService.downloadDocument(anyString())).willReturn(PDF);

        CaseManagementOrder order = buildOrder(PARTIES_REVIEW, null);

        CaseDetails caseDetails = buildCaseDetails(order, DRAFT_CASE_MANAGEMENT_ORDER, "No");

        postAboutToSubmitEvent(buildCallbackRequest(caseDetails, DRAFT_CASE_MANAGEMENT_ORDER));

        verify(notificationClient).sendEmail(
            eq(CMO_READY_FOR_PARTY_REVIEW_NOTIFICATION_TEMPLATE),
            eq("robert@example.com"),
            eqJson(expectedReviewByRepresentativesNotificationParameters(DIGITAL_SERVICE)),
            eq(CASE_ID.toString()));

        verify(notificationClient).sendEmail(
            eq(CMO_READY_FOR_PARTY_REVIEW_NOTIFICATION_TEMPLATE),
            eq("charlie@example.com"),
            eqJson(expectedReviewByRepresentativesNotificationParameters(EMAIL)),
            eq(CASE_ID.toString()));
    }

    @Test
    void aboutToSubmitShouldNotifyHmctsAdminWhenStatusIsSendToJudgeAndCtscIsDisabled() throws Exception {
        CaseManagementOrder order = buildOrder(SEND_TO_JUDGE, SEND_TO_ALL_PARTIES);

        CaseDetails caseDetails = buildCaseDetails(order, DRAFT_CASE_MANAGEMENT_ORDER, "No");

        postAboutToSubmitEvent(buildCallbackRequest(caseDetails, DRAFT_CASE_MANAGEMENT_ORDER));

        verify(notificationClient).sendEmail(
            CMO_READY_FOR_JUDGE_REVIEW_NOTIFICATION_TEMPLATE, HMCTS_ADMIN_INBOX,
            expectedCMODraftCompleteNotificationParameters(), CASE_ID.toString());

        verify(notificationClient, never()).sendEmail(
            CMO_READY_FOR_JUDGE_REVIEW_NOTIFICATION_TEMPLATE, CTSC_ADMIN_INBOX,
            expectedCMODraftCompleteNotificationParameters(), CASE_ID.toString());
    }

    @Test
    void aboutToSubmitShouldNotifyCtscAdminWhenStatusIsSendToJudgeAndCtscIsEnabled() throws Exception {
        CaseManagementOrder order = buildOrder(SEND_TO_JUDGE, SEND_TO_ALL_PARTIES);

        CaseDetails caseDetails = buildCaseDetails(order, DRAFT_CASE_MANAGEMENT_ORDER, "Yes");

        postAboutToSubmitEvent(buildCallbackRequest(caseDetails, DRAFT_CASE_MANAGEMENT_ORDER));

        verify(notificationClient, never()).sendEmail(
            CMO_READY_FOR_JUDGE_REVIEW_NOTIFICATION_TEMPLATE, HMCTS_ADMIN_INBOX,
            expectedCMODraftCompleteNotificationParameters(), CASE_ID.toString());

        verify(notificationClient).sendEmail(
            CMO_READY_FOR_JUDGE_REVIEW_NOTIFICATION_TEMPLATE, CTSC_ADMIN_INBOX,
            expectedCMODraftCompleteNotificationParameters(), CASE_ID.toString());
    }

    @Test
    void aboutToSubmitShouldNotNotifyAdminsWhenStatusIsNotSendToJudge() throws Exception {
        CaseManagementOrder order = buildOrder(SELF_REVIEW, SEND_TO_ALL_PARTIES);

        CaseDetails caseDetails = buildCaseDetails(order, DRAFT_CASE_MANAGEMENT_ORDER, "No");

        postAboutToSubmitEvent(buildCallbackRequest(caseDetails, DRAFT_CASE_MANAGEMENT_ORDER));

        verify(notificationClient, never()).sendEmail(
            CMO_READY_FOR_JUDGE_REVIEW_NOTIFICATION_TEMPLATE, HMCTS_ADMIN_INBOX,
            expectedCMODraftCompleteNotificationParameters(), CASE_ID.toString());

        verify(notificationClient, never()).sendEmail(
            CMO_READY_FOR_JUDGE_REVIEW_NOTIFICATION_TEMPLATE, CTSC_ADMIN_INBOX,
            expectedCMODraftCompleteNotificationParameters(), CASE_ID.toString()
        );
    }

    private void assertResponseDataContainsExpectedFields(CaseData responseData, CaseData caseDataBefore) {
        assertThat(responseData.getHearingDetails()).isEqualTo(caseDataBefore.getHearingDetails());
        assertThat(responseData.getRespondents1()).isEqualTo(caseDataBefore.getRespondents1());
        assertThat(responseData.getCaseLocalAuthority()).isEqualTo(caseDataBefore.getCaseLocalAuthority());
        assertThat(responseData.getFamilyManCaseNumber()).isEqualTo(caseDataBefore.getFamilyManCaseNumber());
    }

    private Map<String, Object> expectedReviewByRepresentativesNotificationParameters(
        RepresentativeServingPreferences servingPreference) {
        String fileContent = new String(Base64.encodeBase64(PDF), ISO_8859_1);
        JSONObject jsonFileObject = new JSONObject().put("file", fileContent);

        final String hearingDate = formatLocalDateTimeBaseUsingFormat(futureDate, DATE_SHORT_MONTH);
        final String subjectLine = "Jones, SACCCCCCCC5676576567, hearing " + hearingDate;

        return ImmutableMap.<String, Object>builder()
            .put("subjectLineWithHearingDate", subjectLine)
            .put("respondentLastName", "Jones")
            .put("digitalPreference", servingPreference == DIGITAL_SERVICE ? "Yes" : "No")
            .put("caseUrl", servingPreference == DIGITAL_SERVICE ? formatCaseUrl("http://fake-url", CASE_ID) : "")
            .put("link_to_document", jsonFileObject)
            .build();
    }

    private Map<String, Object> expectedJudgeRejectedNotificationParameters() {
        return new HashMap<>(commonNotificationParameters()
            .put("requestedChanges", "Please make this change XYZ")
            .build());
    }

    private Map<String, Object> expectedCMODraftCompleteNotificationParameters() {
        return new HashMap<>(
            commonNotificationParameters()
                .put("respondentLastName", "Jones")
                .put("judgeTitle", "Her Honour Judge")
                .put("judgeName", "Moley")
                .build());
    }

    private ImmutableMap.Builder<String, Object> commonNotificationParameters() {
        String hearingDate = formatLocalDateTimeBaseUsingFormat(futureDate, DATE_SHORT_MONTH);
        String subjectLine = String.format("Jones, %s, hearing %s", FAMILY_MAN_CASE_NUMBER, hearingDate);

        return ImmutableMap.<String, Object>builder()
            .put("subjectLineWithHearingDate", subjectLine)
            .put("reference", CASE_ID.toString())
            .put("caseUrl", formatCaseUrl("http://fake-url", CASE_ID));
    }

    private CaseManagementOrder buildOrder(CMOStatus status, ActionType actionType) {
        return CaseManagementOrder.builder()
            .status(status)
            .id(HEARING_ID)
            .orderDoc(createDocumentReference(randomUUID().toString()))
            .action(OrderAction.builder()
                .type(actionType)
                .build())
            .directions(emptyList())
            .build();
    }

    private CaseDetails buildCaseDetails(CaseManagementOrder order, Event cmoEvent, String enableCtsc) {
        return CaseDetails.builder()
            .id(12345L)
            .data(Map.of(CASE_MANAGEMENT_ORDER_JUDICIARY.getKey(), order,
                "cmoEventId", cmoEvent.getId(),
                "hearingDetails", createHearingBookings(futureDate, futureDate.plusHours(4)),
                "respondents1", createRespondents(),
                "caseLocalAuthority", LOCAL_AUTHORITY_CODE,
                "sendToCtsc", enableCtsc,
                "familyManCaseNumber", FAMILY_MAN_CASE_NUMBER,
                "allocatedJudge", buildAllocatedJudge(),
                "representatives", createRepresentatives())).build();
    }

    private Judge buildAllocatedJudge() {
        return Judge.builder()
            .judgeLastName("Moley")
            .judgeTitle(JudgeOrMagistrateTitle.HER_HONOUR_JUDGE)
            .judgeEmailAddress("moley@example.com")
            .build();
    }

    private CallbackRequest buildCallbackRequest(CaseDetails caseDetails, Event event) {
        return CallbackRequest.builder()
            .eventId(event.getId())
            .caseDetails(caseDetails)
            .build();
    }

    private List<Element<Representative>> createRepresentatives() {
        return wrapElements(Representative.builder()
            .email("robert@example.com")
            .fullName("Robert Robin")
            .servingPreferences(DIGITAL_SERVICE)
            .build(), Representative.builder()
            .email("charlie@example.com")
            .fullName("Charlie Brown")
            .servingPreferences(EMAIL)
            .build());
    }
}
