package uk.gov.hmcts.reform.fpl.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.events.GeneratedOrderEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.RepresentativeService;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.C2UploadedEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.GeneratedOrderEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.HmctsEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.OrderIssuedEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.representative.RepresentativeNotificationService;
import uk.gov.hmcts.reform.idam.client.IdamApi;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ORDER_GENERATED_NOTIFICATION_TEMPLATE_FOR_LA;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_ADMIN;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_REPRESENTATIVES;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.IssuedOrderType.GENERATED_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.enums.UserRole.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.AUTH_TOKEN;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.COURT_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.CTSC_INBOX;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.DOCUMENT_CONTENTS;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_CODE;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_NAME;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.USER_ID;
import static uk.gov.hmcts.reform.fpl.utils.AssertionHelper.assertEquals;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;
import static uk.gov.hmcts.reform.fpl.utils.OrderIssuedNotificationTestHelper.getExpectedParametersForAdminWhenNoRepresentativesServedByPost;
import static uk.gov.hmcts.reform.fpl.utils.OrderIssuedNotificationTestHelper.getExpectedParametersForRepresentatives;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {GeneratedOrderEventHandler.class, InboxLookupService.class, HmctsEmailContentProvider.class,
    JacksonAutoConfiguration.class, LookupTestConfig.class, RepresentativeNotificationHandler.class,
    IssuedOrderAdminNotificationHandler.class, RepresentativeNotificationService.class})
class GeneratedOrderEventHandlerTest {
    final String mostRecentUploadedDocumentUrl =
        "http://fake-document-gateway/documents/79ec80ec-7be6-493b-b4e6-f002f05b7079/binary";
    final String subjectLine = "Lastname, SACCCCCCCC5676576567";
    final Map<String, Object> c2Parameters = ImmutableMap.<String, Object>builder()
        .put("subjectLine", subjectLine)
        .put("hearingDetailsCallout", subjectLine)
        .put("reference", "12345")
        .put("caseUrl", "null/case/" + JURISDICTION + "/" + CASE_TYPE + "/12345")
        .build();

    final Map<String, Object> orderLocalAuthorityParameters = ImmutableMap.<String, Object>builder()
        .putAll(c2Parameters)
        .put("localAuthorityOrCafcass", LOCAL_AUTHORITY_NAME)
        .put("linkToDocument", mostRecentUploadedDocumentUrl)
        .build();

    @Captor
    private ArgumentCaptor<Map<String, Object>> dataCaptor;

    @MockBean
    private C2UploadedEmailContentProvider c2UploadedEmailContentProvider;

    @MockBean
    private GeneratedOrderEmailContentProvider orderEmailContentProvider;

    @MockBean
    private OrderIssuedEmailContentProvider orderIssuedEmailContentProvider;

    @MockBean
    private IdamApi idamApi;

    @MockBean
    private InboxLookupService inboxLookupService;

    @MockBean
    private RepresentativeService representativeService;

    @MockBean
    private NotificationService notificationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private GeneratedOrderEventHandler generatedOrderEventHandler;

    @BeforeEach
    void before() throws IOException {
        CaseDetails caseDetails = callbackRequest().getCaseDetails();
        CaseData caseData = objectMapper.convertValue(caseDetails.getData(), CaseData.class);

        given(inboxLookupService.getNotificationRecipientEmail(caseDetails, LOCAL_AUTHORITY_CODE))
            .willReturn(LOCAL_AUTHORITY_EMAIL_ADDRESS);

        given(c2UploadedEmailContentProvider.buildC2UploadNotification(caseDetails))
            .willReturn(c2Parameters);

        given(orderEmailContentProvider.buildOrderNotificationParametersForLocalAuthority(
            callbackRequest().getCaseDetails(), LOCAL_AUTHORITY_CODE, mostRecentUploadedDocumentUrl))
            .willReturn(orderLocalAuthorityParameters);

        given(orderIssuedEmailContentProvider.buildNotificationParametersForHmctsAdmin(
            callbackRequest().getCaseDetails(), LOCAL_AUTHORITY_CODE, DOCUMENT_CONTENTS, GENERATED_ORDER))
            .willReturn(getExpectedParametersForAdminWhenNoRepresentativesServedByPost(true));

        given(orderIssuedEmailContentProvider.buildNotificationParametersForRepresentatives(
            callbackRequest().getCaseDetails(), LOCAL_AUTHORITY_CODE, DOCUMENT_CONTENTS, GENERATED_ORDER))
            .willReturn(getExpectedParametersForRepresentatives(BLANK_ORDER.getLabel(), true));

        given(representativeService.getRepresentativesByServedPreference(caseData.getRepresentatives(), EMAIL))
            .willReturn(getExpectedEmailRepresentativesForAddingPartiesToCase());
    }

    @Test
    void shouldNotifyPartiesOnOrderSubmission() throws IOException {
        generatedOrderEventHandler.sendEmailsForOrder(new GeneratedOrderEvent(callbackRequest(),
            AUTH_TOKEN, USER_ID, mostRecentUploadedDocumentUrl, DOCUMENT_CONTENTS));

        verify(notificationService).sendEmail(
            ORDER_GENERATED_NOTIFICATION_TEMPLATE_FOR_LA,
            LOCAL_AUTHORITY_EMAIL_ADDRESS,
            orderLocalAuthorityParameters,
            "12345");

        verify(notificationService).sendEmail(
            ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_ADMIN,
            COURT_EMAIL_ADDRESS,
            getExpectedParametersForAdminWhenNoRepresentativesServedByPost(true),
            "12345");

        verify(notificationService).sendEmail(
            eq(ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_REPRESENTATIVES),
            eq("barney@rubble.com"),
            dataCaptor.capture(),
            eq("12345"));

        assertEquals(dataCaptor.getValue(), getExpectedParametersForRepresentatives(BLANK_ORDER.getLabel(), true));
    }

    @Test
    void shouldNotifyCtsAdminOnOrderSubmission() throws IOException {
        CallbackRequest callbackRequest = appendSendToCtscOnCallback();

        given(orderIssuedEmailContentProvider.buildNotificationParametersForHmctsAdmin(
            callbackRequest.getCaseDetails(), LOCAL_AUTHORITY_CODE, DOCUMENT_CONTENTS, GENERATED_ORDER))
            .willReturn(getExpectedParametersForAdminWhenNoRepresentativesServedByPost(true));

        given(idamApi.retrieveUserInfo(AUTH_TOKEN)).willReturn(
            UserInfo.builder().sub(CTSC_INBOX).roles(LOCAL_AUTHORITY.getRoles()).build());

        generatedOrderEventHandler.sendEmailsForOrder(new GeneratedOrderEvent(callbackRequest,
            AUTH_TOKEN, USER_ID, mostRecentUploadedDocumentUrl, DOCUMENT_CONTENTS));

        verify(notificationService).sendEmail(
            ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_ADMIN,
            CTSC_INBOX,
            getExpectedParametersForAdminWhenNoRepresentativesServedByPost(true),
            "12345");
    }

    private List<Representative> getExpectedEmailRepresentativesForAddingPartiesToCase() {
        return ImmutableList.of(
            Representative.builder()
                .email("barney@rubble.com")
                .fullName("Barney Rubble")
                .servingPreferences(EMAIL)
                .build());
    }

    private CallbackRequest appendSendToCtscOnCallback() throws IOException {
        CallbackRequest callbackRequest = callbackRequest();
        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        Map<String, Object> updatedCaseData = ImmutableMap.<String, Object>builder()
            .putAll(caseDetails.getData())
            .put("sendToCtsc", "Yes")
            .build();

        caseDetails.setData(updatedCaseData);
        callbackRequest.setCaseDetails(caseDetails);

        return callbackRequest;
    }
}
