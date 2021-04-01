package uk.gov.hmcts.reform.fpl.handlers;

import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.events.AdditionalApplicationsPbaPaymentNotTakenEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.BaseCaseNotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.LocalAuthorityInboxRecipientsRequest;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.AdditionalApplicationsUploadedEmailContentProvider;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.Set;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.INTERLOCUTORY_UPLOAD_PBA_PAYMENT_NOT_TAKEN_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.UserRole.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.AUTH_TOKEN;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.CTSC_INBOX;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.caseData;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {AdditionalApplicationsPbaPaymentNotTakenEventHandler.class, LookupTestConfig.class,
    HmctsAdminNotificationHandler.class})
class AdditionalApplicationsPbaPaymentNotTakenEventHandlerTest {
    private final BaseCaseNotifyData additionalApplicationsPaymentNotTakenParameters = BaseCaseNotifyData.builder()
        .caseUrl("http://fpl/case/12345")
        .build();

    @MockBean
    private IdamClient idamClient;

    @MockBean
    private InboxLookupService inboxLookupService;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private AdditionalApplicationsUploadedEmailContentProvider additionalApplicationsUploadedEmailContentProvider;

    @Autowired
    private AdditionalApplicationsPbaPaymentNotTakenEventHandler additionalApplicationsPbaPaymentNotTakenEventHandler;

    @Test
    void shouldNotifyAdminWhenUploadedAdditionalApplicationsIsNotUsingPbaPayment() {
        CaseData caseData = caseData();

        given(additionalApplicationsUploadedEmailContentProvider.getPbaPaymentNotTakenNotifyData(caseData))
            .willReturn(additionalApplicationsPaymentNotTakenParameters);

        additionalApplicationsPbaPaymentNotTakenEventHandler.notifyAdmin(
            new AdditionalApplicationsPbaPaymentNotTakenEvent(caseData));

        verify(notificationService).sendEmail(
            INTERLOCUTORY_UPLOAD_PBA_PAYMENT_NOT_TAKEN_TEMPLATE, "admin@family-court.com",
            additionalApplicationsPaymentNotTakenParameters,
            caseData.getId());
    }

    @Test
    void shouldNotifyCtscAdminWhenUploadedAdditionalApplicationsIsNotUsingPbaPaymentAndCtscIsEnabled() {
        CaseData caseData = CaseData.builder()
            .id(RandomUtils.nextLong())
            .sendToCtsc("Yes")
            .build();

        given(idamClient.getUserInfo(AUTH_TOKEN)).willReturn(
            UserInfo.builder().sub(CTSC_INBOX).roles(LOCAL_AUTHORITY.getRoleNames()).build());

        given(inboxLookupService.getRecipients(
            LocalAuthorityInboxRecipientsRequest.builder().caseData(caseData).build()))
            .willReturn(Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS));

        given(additionalApplicationsUploadedEmailContentProvider.getPbaPaymentNotTakenNotifyData(caseData))
            .willReturn(additionalApplicationsPaymentNotTakenParameters);

        additionalApplicationsPbaPaymentNotTakenEventHandler.notifyAdmin(
            new AdditionalApplicationsPbaPaymentNotTakenEvent(caseData));

        verify(notificationService).sendEmail(INTERLOCUTORY_UPLOAD_PBA_PAYMENT_NOT_TAKEN_TEMPLATE, CTSC_INBOX,
            additionalApplicationsPaymentNotTakenParameters, caseData.getId());
    }
}
