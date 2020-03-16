package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.service.DateFormatterService;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.service.notify.NotificationClient;

import java.time.LocalDateTime;
import java.time.format.FormatStyle;
import java.util.Map;

import static java.lang.Long.parseLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ORDER_GENERATED_NOTIFICATION_TEMPLATE_FOR_LA;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_ADMIN;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_REPRESENTATIVES;
import static uk.gov.hmcts.reform.fpl.enums.IssuedOrderType.GENERATED_ORDER;
import static uk.gov.hmcts.reform.fpl.utils.AssertionHelper.assertEquals;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBookings;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createOrders;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createRespondents;
import static uk.gov.hmcts.reform.fpl.utils.OrderIssuedNotificationTestHelper.buildRepresentativesServedByEmail;
import static uk.gov.hmcts.reform.fpl.utils.OrderIssuedNotificationTestHelper.buildRepresentativesServedByPost;
import static uk.gov.hmcts.reform.fpl.utils.OrderIssuedNotificationTestHelper.getExpectedParametersForAdmin;
import static uk.gov.hmcts.reform.fpl.utils.OrderIssuedNotificationTestHelper.getExpectedParametersForAdminWhenNoRepresentativesServedByPost;
import static uk.gov.hmcts.reform.fpl.utils.OrderIssuedNotificationTestHelper.getExpectedParametersForRepresentatives;

@ActiveProfiles("integration-test")
@WebMvcTest(GeneratedOrderController.class)
@OverrideAutoConfiguration(enabled = true)
class GeneratedOrderControllerSubmittedTest extends AbstractControllerTest {
    private static final String LOCAL_AUTHORITY_CODE = "example";
    private static final String LOCAL_AUTHORITY_EMAIL_ADDRESS = "local-authority@local-authority.com";
    private static final String LOCAL_AUTHORITY_NAME = "Example Local Authority";
    private static final String FAMILY_MAN_CASE_NUMBER = "SACCCCCCCC5676576567";
    private static final String CASE_ID = "12345";
    private static final String SEND_DOCUMENT_EVENT = "internal-change:SEND_DOCUMENT";
    private static final byte[] PDF = {1, 2, 3, 4, 5};

    private final LocalDateTime dateIn3Months = LocalDateTime.now().plusMonths(3);
    private final DocumentReference lastOrderDocumentReference = DocumentReference.builder()
        .filename("C21 3.pdf")
        .url("http://fake-document-gateway/documents/79ec80ec-7be6-493b-b4e6-f002f05b7079")
        .binaryUrl("http://fake-document-gateway/documents/79ec80ec-7be6-493b-b4e6-f002f05b7079/binary")
        .build();

    @MockBean
    private NotificationClient notificationClient;

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @Autowired
    private DateFormatterService dateFormatterService;

    @MockBean
    private DocumentDownloadService documentDownloadService;

    @Captor
    private ArgumentCaptor<Map<String, Object>> dataCaptor;

    GeneratedOrderControllerSubmittedTest() {
        super("create-order");
    }

    @AfterEach
    void resetInvocations() {
        reset(notificationClient);
    }

    @Test
    void submittedShouldNotifyHmctsAdminAndLAWhenNoRepresentativesNeedServing() throws Exception {
        CaseDetails caseDetails = buildCaseDetails(getCommonCaseData().build());
        postSubmittedEvent(caseDetails);

        verify(notificationClient).sendEmail(
            ORDER_GENERATED_NOTIFICATION_TEMPLATE_FOR_LA,
            LOCAL_AUTHORITY_EMAIL_ADDRESS,
            expectedOrderLocalAuthorityParameters(),
            CASE_ID);

        verify(notificationClient).sendEmail(
            ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_ADMIN,
            "admin@family-court.com",
            getExpectedParametersForAdminWhenNoRepresentativesServedByPost(),
            CASE_ID);

        verifyZeroInteractions(notificationClient);
        verifySendDocumentEventTriggered();
    }

    @Test
    void submittedShouldNotifyCtscAdminAWhenNoRepresentativesNeedServingAndCtscIsEnabled() throws Exception {
        Map<String, Object> caseData = getCommonCaseData().put("sendToCtsc", "Yes").build();
        CaseDetails caseDetails = buildCaseDetails(caseData);

        postSubmittedEvent(caseDetails);

        verify(notificationClient).sendEmail(
            ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_ADMIN,
            "FamilyPublicLaw+ctsc@gmail.com",
            getExpectedParametersForAdminWhenNoRepresentativesServedByPost(),
            CASE_ID);

        verify(notificationClient, never()).sendEmail(
            ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_ADMIN,
            "admin@family-court.com",
            getExpectedParametersForAdminWhenNoRepresentativesServedByPost(),
            CASE_ID);

        verifySendDocumentEventTriggered();
    }

    @Test
    void submittedShouldNotifyHmctsAdminAndLAWhenRepresentativesNeedServingByPost() throws Exception {
        given(documentDownloadService.downloadDocument(anyString())).willReturn(PDF);

        Map<String, Object> caseData = getCommonCaseData()
            .put("representatives", buildRepresentativesServedByPost())
            .build();

        CaseDetails caseDetails = buildCaseDetails(caseData);

        postSubmittedEvent(caseDetails);

        verify(notificationClient).sendEmail(
            ORDER_GENERATED_NOTIFICATION_TEMPLATE_FOR_LA,
            LOCAL_AUTHORITY_EMAIL_ADDRESS,
            expectedOrderLocalAuthorityParameters(),
            CASE_ID);

        verify(notificationClient).sendEmail(
            eq(ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_ADMIN),
            eq("admin@family-court.com"),
            dataCaptor.capture(),
            eq(CASE_ID));

        assertEquals(dataCaptor.getValue(), getExpectedParametersForAdmin(GENERATED_ORDER));

        verifyZeroInteractions(notificationClient);
        verifySendDocumentEventTriggered();
    }

    @Test
    void submittedShouldNotifyCtscAdminAndLAWhenRepresentativesNeedServingByPostAndCtscIsEnabled() throws Exception {
        given(documentDownloadService.downloadDocument(anyString())).willReturn(PDF);

        Map<String, Object> caseData = getCommonCaseData()
            .put("representatives", buildRepresentativesServedByPost())
            .put("sendToCtsc", "Yes")
            .build();

        CaseDetails caseDetails = buildCaseDetails(caseData);

        postSubmittedEvent(caseDetails);

        verify(notificationClient).sendEmail(
            eq(ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_ADMIN),
            eq("FamilyPublicLaw+ctsc@gmail.com"),
            dataCaptor.capture(),
            eq(CASE_ID));

        verify(notificationClient, never()).sendEmail(
            eq(ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_ADMIN),
            eq("admin@family-court.com"),
            dataCaptor.capture(),
            eq(CASE_ID));

        verifySendDocumentEventTriggered();
    }

    @Test
    void submittedShouldNotifyRepresentativesServedByEmail() throws Exception {
        given(documentDownloadService.downloadDocument(anyString())).willReturn(PDF);

        Map<String, Object> caseData = getCommonCaseData()
            .put("representatives", buildRepresentativesServedByEmail())
            .build();

        CaseDetails caseDetails = buildCaseDetails(caseData);

        postSubmittedEvent(caseDetails);

        verify(notificationClient).sendEmail(
            eq(ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_REPRESENTATIVES),
            eq("bill@example.com"),
            dataCaptor.capture(),
            eq(CASE_ID));

        assertEquals(dataCaptor.getValue(), getExpectedParametersForRepresentatives(GENERATED_ORDER));

        verifySendDocumentEventTriggered();
    }

    private CaseDetails buildCaseDetails(Map<String, Object> caseData) {
        return CaseDetails.builder()
            .id(parseLong(CASE_ID))
            .jurisdiction(JURISDICTION)
            .caseTypeId(CASE_TYPE)
            .data(caseData)
            .build();
    }

    private ImmutableMap.Builder<String, Object> getCommonCaseData() {
        Map<String, Object> caseData = Map.of(
            "orderCollection", createOrders(lastOrderDocumentReference),
            "caseLocalAuthority", LOCAL_AUTHORITY_CODE,
            "familyManCaseNumber", FAMILY_MAN_CASE_NUMBER,
            "respondents1", createRespondents(),
            "hearingDetails", createHearingBookings(dateIn3Months, dateIn3Months.plusHours(4))
        );

        return ImmutableMap.<String, Object>builder().putAll(caseData);
    }

    private Map<String, Object> commonNotificationParameters() {
        final String documentUrl = "http://fake-document-gateway/documents/79ec80ec-7be6-493b-b4e6-f002f05b7079/binary";
        final String subjectLine = "Jones, " + FAMILY_MAN_CASE_NUMBER;

        return ImmutableMap.<String, Object>builder()
            .put("subjectLine", subjectLine)
            .put("linkToDocument", documentUrl)
            .put("hearingDetailsCallout", subjectLine + ", hearing " + dateFormatterService.formatLocalDateToString(
                dateIn3Months.toLocalDate(), FormatStyle.MEDIUM))
            .put("reference", CASE_ID)
            .put("caseUrl", "http://fake-url/case/" + JURISDICTION + "/" + CASE_TYPE + "/" + CASE_ID)
            .build();
    }

    private Map<String, Object> expectedOrderLocalAuthorityParameters() {
        return ImmutableMap.<String, Object>builder()
            .putAll(commonNotificationParameters())
            .put("localAuthorityOrCafcass", LOCAL_AUTHORITY_NAME)
            .build();
    }

    private void verifySendDocumentEventTriggered() {
        verify(coreCaseDataService).triggerEvent(JURISDICTION,
            CASE_TYPE,
            parseLong(CASE_ID),
            SEND_DOCUMENT_EVENT,
            Map.of("documentToBeSent", lastOrderDocumentReference));
    }
}
