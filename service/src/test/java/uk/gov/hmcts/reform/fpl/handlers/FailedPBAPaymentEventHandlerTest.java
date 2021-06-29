package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.events.FailedPBAPaymentEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.payment.FailedPBANotificationData;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.FailedPBAPaymentContentProvider;

import java.util.Set;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_CTSC;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_LA;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationType.C110A_APPLICATION;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationType.C2_APPLICATION;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.AUTH_TOKEN;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.CTSC_INBOX;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.model.notify.LocalAuthorityInboxRecipientsRequest.builder;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.caseData;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {FailedPBAPaymentEventHandler.class, LookupTestConfig.class})
class FailedPBAPaymentEventHandlerTest {

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

    private CaseData caseData;

    @BeforeEach
    void before() {
        caseData = caseData();

        given(requestData.authorisation()).willReturn(AUTH_TOKEN);

        given(inboxLookupService.getRecipients(
            builder()
                .caseData(caseData)
                .excludeLegalRepresentatives(true)
                .build()))
            .willReturn(Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS));
    }

    @Test
    void shouldNotifyLAWhenApplicationPBAPaymentFails() {
        final FailedPBANotificationData expectedParameters = FailedPBANotificationData.builder()
            .applicationType(C110A_APPLICATION.getType())
            .build();

        given(failedPBAPaymentContentProvider.getLocalAuthorityNotifyData(C110A_APPLICATION, null))
            .willReturn(expectedParameters);

        failedPBAPaymentEventHandler.notifyApplicant(
            new FailedPBAPaymentEvent(caseData, C110A_APPLICATION, null));

        verify(notificationService).sendEmail(
            APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_LA,
            Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS),
            expectedParameters,
            caseData.getId().toString());
    }

    @Test
    void shouldNotifyCtscWhenApplicationPBAPaymentFails() {
        String applicant = "Swansea council, Applicant";
        final FailedPBANotificationData expectedParameters = FailedPBANotificationData.builder()
            .applicationType(C2_APPLICATION.getType())
            .caseUrl("caseUrl")
            .applicant(applicant)
            .build();

        given(failedPBAPaymentContentProvider.getCtscNotifyData(caseData, C2_APPLICATION, applicant))
            .willReturn(expectedParameters);

        failedPBAPaymentEventHandler.notifyCTSC(
            new FailedPBAPaymentEvent(caseData, C2_APPLICATION, applicant));

        verify(notificationService).sendEmail(
            APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_CTSC,
            CTSC_INBOX,
            expectedParameters,
            caseData.getId());
    }

}
