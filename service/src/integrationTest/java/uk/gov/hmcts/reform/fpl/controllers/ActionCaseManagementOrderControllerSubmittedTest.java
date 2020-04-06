package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.enums.ActionType;
import uk.gov.hmcts.reform.fpl.enums.CMOStatus;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.OrderAction;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.time.LocalDateTime;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_ORDER_ISSUED_CASE_LINK_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_ORDER_ISSUED_DOCUMENT_LINK_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_ADMIN;
import static uk.gov.hmcts.reform.fpl.enums.ActionType.JUDGE_REQUESTED_CHANGE;
import static uk.gov.hmcts.reform.fpl.enums.ActionType.SEND_TO_ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.CASE_MANAGEMENT_ORDER_JUDICIARY;
import static uk.gov.hmcts.reform.fpl.enums.NextHearingType.ISSUES_RESOLUTION_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.utils.AssertionHelper.assertEquals;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createCmoDirections;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBookings;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createRecitals;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createRespondents;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createSchedule;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.OrderIssuedNotificationTestHelper.buildRepresentativesServedByPost;
import static uk.gov.hmcts.reform.fpl.utils.OrderIssuedNotificationTestHelper.getExpectedParametersForAdminWhenNoRepresentativesServedByPost;
import static uk.gov.hmcts.reform.fpl.utils.OrderIssuedNotificationTestHelper.getExpectedParametersForAdminWhenRepresentativesServedByPost;

@ActiveProfiles("integration-test")
@WebMvcTest(ActionCaseManagementOrderController.class)
@OverrideAutoConfiguration(enabled = true)
class ActionCaseManagementOrderControllerSubmittedTest extends AbstractControllerTest {
    private static final String FAMILY_MAN_CASE_NUMBER = "SACCCCCCCC5676576567";
    private static final String LOCAL_AUTHORITY_NAME = "Example Local Authority";
    private static final String LOCAL_AUTHORITY_CODE = "example";
    private static final String LOCAL_AUTHORITY_EMAIL_ADDRESS = "local-authority@local-authority.com";
    private static final String CASE_ID = "12345";
    private static final String REPRESENTATIVES = "representatives";
    private static final String CAFCASS_EMAIL_ADDRESS = "cafcass@cafcass.com";
    private static final String CMO_EVENT_KEY = "internal-change:CMO_PROGRESSION";
    private static final String SEND_DOCUMENT_KEY = "internal-change:SEND_DOCUMENT";
    private static final UUID ID = randomUUID();
    private static final byte[] PDF = {1, 2, 3, 4, 5};
    private final LocalDateTime dateIn3Months = LocalDateTime.now().plusMonths(3);
    private final DocumentReference cmoDocument = DocumentReference.buildFromDocument(document());

    @MockBean
    private DocumentDownloadService documentDownloadService;

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @MockBean
    private NotificationClient notificationClient;

    @Captor
    private ArgumentCaptor<Map<String, Object>> dataCaptor;

    private Document document;

    ActionCaseManagementOrderControllerSubmittedTest() {
        super("action-cmo");
    }

    @BeforeEach
    void setup() {
        document = document();
        given(documentDownloadService.downloadDocument(anyString())).willReturn(PDF);
    }

    @Test
    void submittedShouldTriggerCMOProgressionEventAndSendCaseLinkNotificationsWhenIssuedOrderApproved()
        throws Exception {
        List<Element<Representative>> representativesServedByDigitalService =
            buildRepresentativesServedByDigitalService();

        CaseDetails caseDetails =
            populateRepresentativesByServedPreferenceData(representativesServedByDigitalService);

        postSubmittedEvent(caseDetails);

        verifyCMOTriggerEventsAndNotificationSentToLocalAuthorityOnApprovedCMO();
        verifySentDocumentEventTriggered();

        verify(notificationClient).sendEmail(
            CMO_ORDER_ISSUED_CASE_LINK_NOTIFICATION_TEMPLATE, "abc@example.com",
            getExpectedCMOIssuedCaseLinkNotificationParameters("Jon Snow"), CASE_ID);

        verify(notificationClient).sendEmail(
            CMO_ORDER_ISSUED_CASE_LINK_NOTIFICATION_TEMPLATE, "xyz@example.com",
            getExpectedCMOIssuedCaseLinkNotificationParameters("Hodo"), CASE_ID);

        verify(notificationClient).sendEmail(
            eq(CMO_ORDER_ISSUED_DOCUMENT_LINK_NOTIFICATION_TEMPLATE), eq(CAFCASS_EMAIL_ADDRESS),
            anyMap(), eq(CASE_ID));

        verifyNotificationSentToAdminWhenCMOIssuedWithNoServingNeeded();

        verifyZeroInteractions(notificationClient);
    }

    @Test
    void submittedShouldTriggerCMOProgressionEventAndSendDocumentLinkNotificationsWhenIssuedOrderApproved()
        throws Exception {
        List<Element<Representative>> representativesServedByEmail = buildRepresentativesServedByEmail();

        CaseDetails caseDetails = populateRepresentativesByServedPreferenceData(representativesServedByEmail);

        postSubmittedEvent(caseDetails);

        verifyCMOTriggerEventsAndNotificationSentToLocalAuthorityOnApprovedCMO();
        verifySentDocumentEventTriggered();

        verify(notificationClient).sendEmail(
            eq(CMO_ORDER_ISSUED_DOCUMENT_LINK_NOTIFICATION_TEMPLATE), eq(CAFCASS_EMAIL_ADDRESS),
            anyMap(), eq(CASE_ID));

        verify(notificationClient).sendEmail(
            eq(CMO_ORDER_ISSUED_DOCUMENT_LINK_NOTIFICATION_TEMPLATE), eq("jamie@example.com"),
            anyMap(), eq(CASE_ID));

        verify(notificationClient).sendEmail(
            eq(CMO_ORDER_ISSUED_DOCUMENT_LINK_NOTIFICATION_TEMPLATE), eq("ragnar@example.com"),
            anyMap(), eq(CASE_ID));

        verifyNotificationSentToAdminWhenCMOIssuedWithNoServingNeeded();

        verifyZeroInteractions(notificationClient);
    }

    @Test
    void submittedShouldNotifyHmctsAdminWhenRepresentativesServedByPost() throws Exception {
        List<Element<Representative>> representativeServedByPost = buildRepresentativesServedByPost();

        CaseDetails caseDetails = populateRepresentativesByServedPreferenceData(representativeServedByPost);

        postSubmittedEvent(caseDetails);

        verifyCMOTriggerEventsAndNotificationSentToLocalAuthorityOnApprovedCMO();
        verifySentDocumentEventTriggered();

        verify(notificationClient).sendEmail(
            eq(CMO_ORDER_ISSUED_DOCUMENT_LINK_NOTIFICATION_TEMPLATE),
            eq(CAFCASS_EMAIL_ADDRESS),
            anyMap(),
            eq(CASE_ID));

        verify(notificationClient).sendEmail(
            eq(ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_ADMIN),
            eq("admin@family-court.com"),
            dataCaptor.capture(),
            eq(CASE_ID));

        assertEquals(dataCaptor.getValue(), getExpectedParametersForAdminWhenRepresentativesServedByPost(true));

        verifyZeroInteractions(notificationClient);
    }

    @Test
    void submittedShouldNotifyCtscAdminWhenRepresentativesServedByPostAndCtscIsEnabled() throws Exception {
        List<Element<Representative>> representativeServedByPost = buildRepresentativesServedByPost();

        CaseDetails caseDetails = populateRepresentativesByServedPreferenceData(representativeServedByPost);

        caseDetails.setData(ImmutableMap.<String, Object>builder()
            .putAll(caseDetails.getData())
            .put("sendToCtsc", "Yes")
            .build());

        postSubmittedEvent(caseDetails);

        verify(notificationClient, never()).sendEmail(
            eq(ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_ADMIN),
            eq("admin@family-court.com"),
            dataCaptor.capture(),
            eq(CASE_ID));

        verify(notificationClient).sendEmail(
            eq(ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_ADMIN),
            eq("FamilyPublicLaw+ctsc@gmail.com"),
            dataCaptor.capture(),
            eq(CASE_ID));
    }

    @Test
    void submittedShouldNotSendNotificationsWhenIssuedOrderNotApproved() {
        CaseManagementOrder caseManagementOrder = getCaseManagementOrder();

        Map<String, Object> data = Map.of(
            "familyManCaseNumber", FAMILY_MAN_CASE_NUMBER,
            "respondents1", createRespondents(),
            "caseLocalAuthority", LOCAL_AUTHORITY_CODE,
            CASE_MANAGEMENT_ORDER_JUDICIARY.getKey(), caseManagementOrder.toBuilder()
                .action(getOrderAction(JUDGE_REQUESTED_CHANGE))
                .build());

        CaseDetails caseDetails = buildCaseDetails(data);

        postSubmittedEvent(caseDetails);

        verifyZeroInteractions(notificationClient);
        verify(coreCaseDataService).triggerEvent(any(), any(), any(), any());
        verify(coreCaseDataService).triggerEvent(any(), any(), any(), any(), any());
    }

    private CaseDetails buildCaseDetails(Map<String, Object> data) {
        return CaseDetails.builder()
            .id(12345L)
            .jurisdiction(JURISDICTION)
            .caseTypeId(CASE_TYPE)
            .data(data)
            .build();
    }

    private OrderAction getOrderAction(ActionType type) {
        return OrderAction.builder()
            .type(type)
            .nextHearingType(ISSUES_RESOLUTION_HEARING)
            .build();
    }

    private CaseManagementOrder getCaseManagementOrder() {
        return CaseManagementOrder.builder()
            .id(ID)
            .status(CMOStatus.SEND_TO_JUDGE)
            .schedule(createSchedule(true))
            .recitals(createRecitals())
            .directions(createCmoDirections())
            .orderDoc(cmoDocument)
            .build();
    }

    private Map<String, Object> getExpectedCMOIssuedCaseLinkNotificationParameters(String recipientName) {
        final String subjectLine = String.format("Jones, SACCCCCCCC5676576567, hearing %s",
            formatLocalDateToString(dateIn3Months.toLocalDate(), FormatStyle.MEDIUM));

        return ImmutableMap.<String, Object>builder()
            .put("localAuthorityNameOrRepresentativeFullName", recipientName)
            .put("subjectLineWithHearingDate", subjectLine)
            .put("reference", CASE_ID)
            .put("caseUrl", String.format("http://fake-url/case/%s/%s/12345", JURISDICTION, CASE_TYPE))
            .build();
    }

    private Map<String, Object> buildSubmittedRequestData(final List<Element<Representative>>
                                                              representatives) {
        return Map.of(
            "familyManCaseNumber", FAMILY_MAN_CASE_NUMBER,
            "respondents1", createRespondents(),
            "caseLocalAuthority", LOCAL_AUTHORITY_CODE,
            "hearingDetails", createHearingBookings(dateIn3Months, dateIn3Months.plusHours(4)),
            REPRESENTATIVES, representatives,
            CASE_MANAGEMENT_ORDER_JUDICIARY.getKey(), CaseManagementOrder.builder()
                .status(SEND_TO_JUDGE)
                .orderDoc(cmoDocument)
                .action(OrderAction.builder()
                    .type(SEND_TO_ALL_PARTIES)
                    .build())
                .build());
    }

    private List<Element<Representative>> buildRepresentativesServedByDigitalService() {
        return wrapElements(Representative.builder()
            .email("abc@example.com")
            .fullName("Jon Snow")
            .servingPreferences(DIGITAL_SERVICE)
            .build(), Representative.builder()
            .build(), Representative.builder()
            .email("xyz@example.com")
            .fullName("Hodo")
            .servingPreferences(DIGITAL_SERVICE)
            .build());
    }

    private CaseDetails populateRepresentativesByServedPreferenceData(
        List<Element<Representative>> representativesServedByPreference) {
        Map<String, Object> data = buildSubmittedRequestData(representativesServedByPreference);

        return buildCaseDetails(data);
    }

    private void verifyCMOTriggerEventsAndNotificationSentToLocalAuthorityOnApprovedCMO()
        throws NotificationClientException {
        verify(coreCaseDataService)
            .triggerEvent(JURISDICTION, CASE_TYPE, 12345L, CMO_EVENT_KEY);

        verify(notificationClient).sendEmail(
            CMO_ORDER_ISSUED_CASE_LINK_NOTIFICATION_TEMPLATE, LOCAL_AUTHORITY_EMAIL_ADDRESS,
            getExpectedCMOIssuedCaseLinkNotificationParameters(LOCAL_AUTHORITY_NAME), CASE_ID);
    }

    private void verifyNotificationSentToAdminWhenCMOIssuedWithNoServingNeeded() throws NotificationClientException {
        verify(notificationClient).sendEmail(
            ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_ADMIN,
            "admin@family-court.com",
            getExpectedParametersForAdminWhenNoRepresentativesServedByPost(true),
            CASE_ID);
    }

    private void verifySentDocumentEventTriggered() {
        verify(coreCaseDataService).triggerEvent(JURISDICTION,
            CASE_TYPE,
            12345L,
            SEND_DOCUMENT_KEY,
            Map.of("documentToBeSent", cmoDocument));
    }

    private List<Element<Representative>> buildRepresentativesServedByEmail() {
        return wrapElements(Representative.builder()
            .email("jamie@example.com")
            .fullName("Jamie Lannister")
            .servingPreferences(EMAIL)
            .build(), Representative.builder()
            .email("ragnar@example.com")
            .fullName("Ragnar")
            .servingPreferences(EMAIL)
            .build());
    }
}
