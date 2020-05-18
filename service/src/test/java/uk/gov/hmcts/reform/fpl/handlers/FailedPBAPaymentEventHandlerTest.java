package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.events.FailedPBAPaymentEvent;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.FailedPBAPaymentContentProvider;

import java.util.Map;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_CTSC;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_LA;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationType.C110A_APPLICATION;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationType.C2_APPLICATION;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.AUTH_TOKEN;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.CTSC_INBOX;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_CODE;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {FailedPBAPaymentEventHandler.class, LookupTestConfig.class})
public class FailedPBAPaymentEventHandlerTest {
    @MockBean
    private RequestData requestData;

    @MockBean
    private InboxLookupService inboxLookupService;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private FailedPBAPaymentContentProvider failedPBAPaymentContentProvider;

    @Autowired
    private FailedPBAPaymentEventHandler failedPBAPaymentEventHandler;

    @BeforeEach
    void before() {
        CaseDetails caseDetails = callbackRequest().getCaseDetails();

        given(requestData.authorisation()).willReturn(AUTH_TOKEN);

        given(inboxLookupService.getNotificationRecipientEmail(caseDetails, LOCAL_AUTHORITY_CODE))
            .willReturn(LOCAL_AUTHORITY_EMAIL_ADDRESS);
    }

    @Test
    void shouldNotifyLAWhenApplicationPBAPaymentFails() {
        CallbackRequest callbackRequest = callbackRequest();
        final Map<String, Object> expectedParameters = Map.of("applicationType", "C110a");

        given(failedPBAPaymentContentProvider.buildLANotificationParameters(C110A_APPLICATION))
            .willReturn(expectedParameters);

        failedPBAPaymentEventHandler.sendFailedPBAPaymentEmailToLocalAuthority(
            new FailedPBAPaymentEvent(callbackRequest, requestData, C110A_APPLICATION));

        verify(notificationService).sendEmail(
            APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_LA,
            LOCAL_AUTHORITY_EMAIL_ADDRESS,
            expectedParameters,
            "12345");
    }

    @Test
    void shouldNotifyCtscWhenApplicationPBAPaymentFails() {
        CallbackRequest callbackRequest = callbackRequest();
        final Map<String, Object> expectedParameters = getCtscNotificationParametersForFailedPayment();

        given(failedPBAPaymentContentProvider.buildCtscNotificationParameters(callbackRequest
            .getCaseDetails(), C2_APPLICATION)).willReturn(expectedParameters);

        failedPBAPaymentEventHandler.sendFailedPBAPaymentEmailToCTSC(
            new FailedPBAPaymentEvent(callbackRequest, requestData, C2_APPLICATION));

        verify(notificationService).sendEmail(
            APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_CTSC,
            CTSC_INBOX,
            expectedParameters,
            "12345");
    }

    private Map<String, Object> getCtscNotificationParametersForFailedPayment() {
        return Map.of("applicationType", "C2",
            "caseUrl", "caseUrl");
    }
}
