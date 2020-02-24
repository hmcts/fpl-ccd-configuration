package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.MapDifference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.IssuedOrderType;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.service.DateFormatterService;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.service.notify.NotificationClient;

import java.time.LocalDateTime;
import java.time.format.FormatStyle;
import java.util.Map;

import static java.lang.Long.parseLong;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ORDER_GENERATED_NOTIFICATION_TEMPLATE_FOR_LA;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_ADMIN;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBookings;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createOrders;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createRespondents;
import static uk.gov.hmcts.reform.fpl.utils.NotifyAdminOrderIssuedTestHelper.buildRepresentativesServedByPost;
import static uk.gov.hmcts.reform.fpl.utils.NotifyAdminOrderIssuedTestHelper.getExpectedParametersForAdminWhenNoRepresentativesServedByPost;
import static uk.gov.hmcts.reform.fpl.utils.NotifyAdminOrderIssuedTestHelper.verifyNotificationSentToAdminWhenOrderIssued;

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
    void submittedShouldNotifyAdminAndLAWhenNoRepresentativesNeedServing() throws Exception {
        postSubmittedEvent(buildCallbackRequest());

        verify(notificationClient).sendEmail(
            eq(ORDER_GENERATED_NOTIFICATION_TEMPLATE_FOR_LA),
            eq(LOCAL_AUTHORITY_EMAIL_ADDRESS),
            eq(expectedOrderLocalAuthorityParameters()),
            eq(CASE_ID));

        verify(notificationClient).sendEmail(
            eq(ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_ADMIN),
            eq("admin@family-court.com"),
            eq(getExpectedParametersForAdminWhenNoRepresentativesServedByPost()),
            eq(CASE_ID));

        verifyZeroInteractions(notificationClient);
        verifySendDocumentEventTriggered();
    }

    @Test
    void submittedShouldNotifyAdminAndLAWhenRepresentativesNeedServingByPost() throws Exception {
        given(documentDownloadService.downloadDocument(anyString())).willReturn(PDF);

        postSubmittedEvent(buildCallbackRequestWithRepresentatives());

        verify(notificationClient).sendEmail(
            eq(ORDER_GENERATED_NOTIFICATION_TEMPLATE_FOR_LA),
            eq(LOCAL_AUTHORITY_EMAIL_ADDRESS),
            eq(expectedOrderLocalAuthorityParameters()),
            eq(CASE_ID));

        verify(notificationClient).sendEmail(
            eq(ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_ADMIN),
            eq("admin@family-court.com"),
            dataCaptor.capture(),
            eq(CASE_ID));

        MapDifference<String, Object> difference = verifyNotificationSentToAdminWhenOrderIssued(dataCaptor,
            IssuedOrderType.GENERATED_ORDER);

        assertThat(difference.areEqual()).isTrue();

        verifyZeroInteractions(notificationClient);
        verifySendDocumentEventTriggered();
    }

    private CallbackRequest buildCallbackRequest() {
        return CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(parseLong(CASE_ID))
                .jurisdiction(JURISDICTION)
                .caseTypeId(CASE_TYPE)
                .data(ImmutableMap.of(
                    "orderCollection", createOrders(lastOrderDocumentReference),
                    "hearingDetails", createHearingBookings(dateIn3Months, dateIn3Months.plusHours(4)),
                    "respondents1", createRespondents(),
                    "caseLocalAuthority", LOCAL_AUTHORITY_CODE,
                    "familyManCaseNumber", FAMILY_MAN_CASE_NUMBER))
                .build())
            .build();
    }

    private CallbackRequest buildCallbackRequestWithRepresentatives() {
        return CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(parseLong(CASE_ID))
                .jurisdiction(JURISDICTION)
                .caseTypeId(CASE_TYPE)
                .data(Map.of(
                    "orderCollection", createOrders(lastOrderDocumentReference),
                    "hearingDetails", createHearingBookings(dateIn3Months, dateIn3Months.plusHours(4)),
                    "respondents1", createRespondents(),
                    "representatives", buildRepresentativesServedByPost(),
                    "caseLocalAuthority", LOCAL_AUTHORITY_CODE,
                    "familyManCaseNumber", FAMILY_MAN_CASE_NUMBER))
                .build())
            .build();
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
