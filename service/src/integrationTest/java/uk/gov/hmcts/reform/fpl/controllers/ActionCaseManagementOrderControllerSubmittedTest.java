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
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
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

import java.time.LocalDateTime;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static org.mockito.ArgumentMatchers.any;
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
import static uk.gov.hmcts.reform.fpl.enums.IssuedOrderType.CMO;
import static uk.gov.hmcts.reform.fpl.enums.NextHearingType.ISSUES_RESOLUTION_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createCmoDirections;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBookings;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createRecitals;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createRespondents;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createSchedule;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.OrderIssuedNotificationTestHelper.getExpectedCaseUrlParameters;
import static uk.gov.hmcts.reform.fpl.utils.matchers.JsonMatcher.eqJson;

@ActiveProfiles("integration-test")
@WebMvcTest(ActionCaseManagementOrderController.class)
@OverrideAutoConfiguration(enabled = true)
class ActionCaseManagementOrderControllerSubmittedTest extends AbstractControllerTest {
    private static final String FAMILY_MAN_CASE_NUMBER = "SACCCCCCCC5676576567";
    private static final String LOCAL_AUTHORITY_NAME = "Example Local Authority";
    private static final String LOCAL_AUTHORITY_CODE = "example";
    private static final String LOCAL_AUTHORITY_EMAIL_ADDRESS = "local-authority@local-authority.com";
    private static final String DIGITAL_SERVED_REPRESENTATIVE_ADDRESS = "abc@digitalrep.com";
    private static final String EMAIL_SERVED_REPRESENTATIVE_ADDRESS = "jamie@emailrep.com";
    private static final String CASE_ID = "12345";
    private static final String REPRESENTATIVES = "representatives";
    private static final String CAFCASS_EMAIL_ADDRESS = "cafcass@cafcass.com";
    private static final String CMO_EVENT_KEY = "internal-change:CMO_PROGRESSION";
    private static final String SEND_DOCUMENT_KEY = "internal-change:SEND_DOCUMENT";
    private static final String ADMIN_EMAIL_ADDRESS = "admin@family-court.com";
    private static final String CTSC_EMAIL_ADDRESS = "FamilyPublicLaw+ctsc@gmail.com";
    private static final UUID ID = randomUUID();
    private static final byte[] PDF = {1, 2, 3, 4, 5};
    private static final DocumentReference CMO_DOCUMENT = DocumentReference.buildFromDocument(document());
    private static final LocalDateTime DATE_IN_3_MONTHS = LocalDateTime.now().plusMonths(3);

    @MockBean
    private DocumentDownloadService documentDownloadService;

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @MockBean
    private NotificationClient notificationClient;

    ActionCaseManagementOrderControllerSubmittedTest() {
        super("action-cmo");
    }

    @BeforeEach
    void setUp() {
        given(documentDownloadService.downloadDocument(anyString())).willReturn(PDF);
    }

    @Test
    void shouldTriggerCMOProgressionEventAndNotifyRelevantPartiesWhenCMOIssued()
        throws Exception {

        CaseDetails caseDetails = populateRepresentativesByServedPreferenceData(buildRepresentatives());

        postSubmittedEvent(caseDetails);

        verify(coreCaseDataService).triggerEvent(JURISDICTION, CASE_TYPE, 12345L, CMO_EVENT_KEY);

        verify(coreCaseDataService).triggerEvent(JURISDICTION, CASE_TYPE, 12345L, SEND_DOCUMENT_KEY,
            Map.of("documentToBeSent", CMO_DOCUMENT));

        verify(notificationClient).sendEmail(
            CMO_ORDER_ISSUED_CASE_LINK_NOTIFICATION_TEMPLATE,
            LOCAL_AUTHORITY_EMAIL_ADDRESS,
            getExpectedCMOIssuedCaseUrlParameters(LOCAL_AUTHORITY_NAME),
            CASE_ID);

        verify(notificationClient).sendEmail(
            CMO_ORDER_ISSUED_CASE_LINK_NOTIFICATION_TEMPLATE,
            DIGITAL_SERVED_REPRESENTATIVE_ADDRESS,
            getExpectedCMOIssuedCaseUrlParameters("Jon Snow"),
            CASE_ID);

        verify(notificationClient).sendEmail(
            eq(CMO_ORDER_ISSUED_DOCUMENT_LINK_NOTIFICATION_TEMPLATE),
            eq(CAFCASS_EMAIL_ADDRESS),
            eqJson(getExpectedCMOIssuedDocumentLinkParameters("cafcass")),
            eq(CASE_ID));

        verify(notificationClient).sendEmail(
            eq(CMO_ORDER_ISSUED_DOCUMENT_LINK_NOTIFICATION_TEMPLATE),
            eq(EMAIL_SERVED_REPRESENTATIVE_ADDRESS),
            eqJson(getExpectedCMOIssuedDocumentLinkParameters("Jamie Lannister")),
            eq(CASE_ID));

        verify(notificationClient).sendEmail(
            eq(ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_ADMIN),
            eq(ADMIN_EMAIL_ADDRESS),
            eqJson(getExpectedCaseUrlParameters(CMO.getLabel(), true)),
            eq(CASE_ID));

        verifyZeroInteractions(notificationClient);
    }

    @Test
    void shouldNotifyCtscAdminWhenOrderIssuedAndCtscEnabled() throws Exception {
        CaseDetails caseDetails = populateRepresentativesByServedPreferenceData(emptyList());

        caseDetails.setData(ImmutableMap.<String, Object>builder()
            .putAll(caseDetails.getData())
            .put("sendToCtsc", "Yes")
            .build());

        postSubmittedEvent(caseDetails);

        verify(notificationClient, never()).sendEmail(
            eq(ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_ADMIN),
            eq(ADMIN_EMAIL_ADDRESS),
            any(),
            any());

        verify(notificationClient).sendEmail(
            eq(ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_ADMIN),
            eq(CTSC_EMAIL_ADDRESS),
            eqJson(getExpectedCaseUrlParameters(CMO.getLabel(), true)),
            eq(CASE_ID));
    }

    @Test
    void shouldNotSendNotificationsWhenIssuedOrderNotApproved() {
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
            .orderDoc(CMO_DOCUMENT)
            .build();
    }

    private Map<String, Object> getExpectedCMOIssuedCaseUrlParameters(String recipientName) {
        final String subjectLine = String.format("Jones, SACCCCCCCC5676576567, hearing %s",
            formatLocalDateToString(DATE_IN_3_MONTHS.toLocalDate(), FormatStyle.MEDIUM));

        return ImmutableMap.<String, Object>builder()
            .put("localAuthorityNameOrRepresentativeFullName", recipientName)
            .put("subjectLineWithHearingDate", subjectLine)
            .put("reference", CASE_ID)
            .put("caseUrl", String.format("http://fake-url/case/%s/%s/12345", JURISDICTION, CASE_TYPE))
            .build();
    }

    private Map<String, Object> getExpectedCMOIssuedDocumentLinkParameters(String recipientName) {
        final String subjectLine = String.format("Jones, SACCCCCCCC5676576567, hearing %s",
            formatLocalDateToString(DATE_IN_3_MONTHS.toLocalDate(), FormatStyle.MEDIUM));

        String fileContent = new String(Base64.encodeBase64(PDF), ISO_8859_1);
        JSONObject jsonFileObject = new JSONObject().put("file", fileContent);

        return ImmutableMap.<String, Object>builder()
            .put("cafcassOrRespondentName", recipientName)
            .put("subjectLineWithHearingDate", subjectLine)
            .put("reference", CASE_ID)
            .put("caseUrl", String.format("http://fake-url/case/%s/%s/12345", JURISDICTION, CASE_TYPE))
            .put("link_to_document", jsonFileObject)
            .build();
    }

    private Map<String, Object> buildSubmittedRequestData(final List<Element<Representative>>
                                                              representatives) {
        return Map.of(
            "familyManCaseNumber", FAMILY_MAN_CASE_NUMBER,
            "respondents1", createRespondents(),
            "caseLocalAuthority", LOCAL_AUTHORITY_CODE,
            "hearingDetails", createHearingBookings(DATE_IN_3_MONTHS, DATE_IN_3_MONTHS.plusHours(4)),
            REPRESENTATIVES, representatives,
            CASE_MANAGEMENT_ORDER_JUDICIARY.getKey(), CaseManagementOrder.builder()
                .status(SEND_TO_JUDGE)
                .orderDoc(CMO_DOCUMENT)
                .action(OrderAction.builder()
                    .type(SEND_TO_ALL_PARTIES)
                    .build())
                .build());
    }

    private CaseDetails populateRepresentativesByServedPreferenceData(
        List<Element<Representative>> representativesServedByPreference) {
        Map<String, Object> data = buildSubmittedRequestData(representativesServedByPreference);

        return buildCaseDetails(data);
    }

    private List<Element<Representative>> buildRepresentatives() {
        return wrapElements(Representative.builder()
                .email(EMAIL_SERVED_REPRESENTATIVE_ADDRESS)
                .fullName("Jamie Lannister")
                .servingPreferences(EMAIL)
                .build(),
            Representative.builder()
                .email(DIGITAL_SERVED_REPRESENTATIVE_ADDRESS)
                .fullName("Jon Snow")
                .servingPreferences(DIGITAL_SERVICE)
                .build());
    }
}

