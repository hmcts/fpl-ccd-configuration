package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.service.notify.NotificationClient;

import java.time.LocalDateTime;
import java.util.Map;

import static java.lang.Long.parseLong;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ORDER_GENERATED_NOTIFICATION_TEMPLATE_FOR_LA_AND_DIGITAL_REPRESENTATIVES;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_ADMIN;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_REPRESENTATIVES;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBookings;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createOrders;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createRespondents;
import static uk.gov.hmcts.reform.fpl.utils.OrderIssuedNotificationTestHelper.buildRepresentatives;
import static uk.gov.hmcts.reform.fpl.utils.OrderIssuedNotificationTestHelper.getExpectedCaseUrlParameters;
import static uk.gov.hmcts.reform.fpl.utils.OrderIssuedNotificationTestHelper.getExpectedParametersForRepresentatives;
import static uk.gov.hmcts.reform.fpl.utils.matchers.JsonMatcher.eqJson;

@ActiveProfiles("integration-test")
@WebMvcTest(GeneratedOrderController.class)
@OverrideAutoConfiguration(enabled = true)
class GeneratedOrderControllerSubmittedTest extends AbstractControllerTest {
    private static final String LOCAL_AUTHORITY_CODE = "example";
    private static final String LOCAL_AUTHORITY_EMAIL_ADDRESS = "local-authority@local-authority.com";
    private static final String DIGITAL_SERVED_REPRESENTATIVE_ADDRESS = "paul@example.com";
    private static final String EMAIL_SERVED_REPRESENTATIVE_ADDRESS = "bill@example.com";
    private static final String ADMIN_EMAIL_ADDRESS = "admin@family-court.com";
    private static final String CTSC_EMAIL_ADDRESS = "FamilyPublicLaw+ctsc@gmail.com";
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

    @MockBean
    private DocumentDownloadService documentDownloadService;

    GeneratedOrderControllerSubmittedTest() {
        super("create-order");
    }

    @BeforeEach
    void init() {
        given(documentDownloadService.downloadDocument(anyString())).willReturn(PDF);
    }

    @AfterEach
    void resetInvocations() {
        reset(notificationClient);
    }

    @Test
    void shouldNotifyRelevantPartiesWhenOrderIssued() throws Exception {
        Map<String, Object> caseData = getCommonCaseData()
            .put("representatives", buildRepresentatives())
            .build();

        CaseDetails caseDetails = buildCaseDetails(caseData);

        postSubmittedEvent(caseDetails);

        verify(notificationClient).sendEmail(
            eq(ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_REPRESENTATIVES),
            eq(EMAIL_SERVED_REPRESENTATIVE_ADDRESS),
            eqJson(getExpectedParametersForRepresentatives(BLANK_ORDER.getLabel(), true)),
            eq(CASE_ID));

        verify(notificationClient).sendEmail(
            eq(ORDER_GENERATED_NOTIFICATION_TEMPLATE_FOR_LA_AND_DIGITAL_REPRESENTATIVES),
            eq(DIGITAL_SERVED_REPRESENTATIVE_ADDRESS),
            eqJson(getExpectedCaseUrlParameters(BLANK_ORDER.getLabel(), true)),
            eq(CASE_ID));

        verify(notificationClient).sendEmail(
            eq(ORDER_GENERATED_NOTIFICATION_TEMPLATE_FOR_LA_AND_DIGITAL_REPRESENTATIVES),
            eq(LOCAL_AUTHORITY_EMAIL_ADDRESS),
            eqJson(getExpectedCaseUrlParameters(BLANK_ORDER.getLabel(), true)),
            eq(CASE_ID));

        verify(notificationClient).sendEmail(
            eq(ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_ADMIN),
            eq(ADMIN_EMAIL_ADDRESS),
            eqJson(getExpectedCaseUrlParameters(BLANK_ORDER.getLabel(), true)),
            eq(CASE_ID));

        verifyZeroInteractions(notificationClient);
        verifySendDocumentEventTriggered();
    }

    @Test
    void shouldNotifyCtscAdminWhenOrderIssuedAndCtscEnabled() throws Exception {
        Map<String, Object> caseData = getCommonCaseData().put("sendToCtsc", "Yes").build();
        CaseDetails caseDetails = buildCaseDetails(caseData);

        postSubmittedEvent(caseDetails);

        verify(notificationClient).sendEmail(
            eq(ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_ADMIN),
            eq(CTSC_EMAIL_ADDRESS),
            eqJson(getExpectedCaseUrlParameters(BLANK_ORDER.getLabel(), true)),
            eq(CASE_ID));

        verify(notificationClient, never()).sendEmail(
            eq(ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_ADMIN),
            eq(ADMIN_EMAIL_ADDRESS),
            any(),
            any());

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

    private void verifySendDocumentEventTriggered() {
        verify(coreCaseDataService).triggerEvent(JURISDICTION,
            CASE_TYPE,
            parseLong(CASE_ID),
            SEND_DOCUMENT_EVENT,
            Map.of("documentToBeSent", lastOrderDocumentReference));
    }
}
