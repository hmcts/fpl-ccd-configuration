package uk.gov.hmcts.reform.fpl.handlers;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.events.C2PbaPaymentNotTakenEvent;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.C2UploadedEmailContentProvider;
import uk.gov.hmcts.reform.idam.client.IdamApi;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.io.IOException;
import java.util.Map;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.C2_UPLOAD_PBA_PAYMENT_NOT_TAKEN_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.UserRole.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.AUTH_TOKEN;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.CTSC_INBOX;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_CODE;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.appendSendToCtscOnCallback;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {C2PbaPaymentNotTakenEventHandler.class, LookupTestConfig.class,
    HmctsAdminNotificationHandler.class})
public class C2PbaPaymentNotTakenEventHandlerTest {
    final Map<String, Object> c2PaymentNotTakenParameters = ImmutableMap.<String, Object>builder()
        .put("caseUrl", "null/case/" + JURISDICTION + "/" + CASE_TYPE + "/12345")
        .build();

    @MockBean
    private IdamApi idamApi;

    @MockBean
    private RequestData requestData;

    @MockBean
    private InboxLookupService inboxLookupService;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private C2UploadedEmailContentProvider c2UploadedEmailContentProvider;

    @Autowired
    private C2PbaPaymentNotTakenEventHandler c2PbaPaymentNotTakenEventHandler;

    @Test
    void shouldNotifyAdminWhenUploadedC2IsNotUsingPbaPayment() {
        CaseDetails caseDetails = callbackRequest().getCaseDetails();

        given(c2UploadedEmailContentProvider.buildC2UploadPbaPaymentNotTakenNotification(caseDetails))
            .willReturn(c2PaymentNotTakenParameters);

        c2PbaPaymentNotTakenEventHandler.sendEmailForC2UploadPbaPaymentNotTaken(
            new C2PbaPaymentNotTakenEvent(callbackRequest(), requestData));

        verify(notificationService).sendEmail(
            C2_UPLOAD_PBA_PAYMENT_NOT_TAKEN_TEMPLATE, "admin@family-court.com", c2PaymentNotTakenParameters,
            "12345");
    }

    @Test
    void shouldNotifyCtscAdminWhenUploadedC2IsNotUsingPbaPaymentAndCtscIsEnabled() throws IOException {
        CallbackRequest callbackRequest = appendSendToCtscOnCallback();
        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        given(idamApi.retrieveUserInfo(AUTH_TOKEN)).willReturn(
            UserInfo.builder().sub(CTSC_INBOX).roles(LOCAL_AUTHORITY.getRoles()).build());

        given(inboxLookupService.getNotificationRecipientEmail(caseDetails, LOCAL_AUTHORITY_CODE))
            .willReturn(LOCAL_AUTHORITY_EMAIL_ADDRESS);

        given(c2UploadedEmailContentProvider.buildC2UploadPbaPaymentNotTakenNotification(caseDetails))
            .willReturn(c2PaymentNotTakenParameters);

        c2PbaPaymentNotTakenEventHandler.sendEmailForC2UploadPbaPaymentNotTaken(
            new C2PbaPaymentNotTakenEvent(callbackRequest, requestData));

        verify(notificationService).sendEmail(
            C2_UPLOAD_PBA_PAYMENT_NOT_TAKEN_TEMPLATE, CTSC_INBOX, c2PaymentNotTakenParameters, "12345");
    }
}
