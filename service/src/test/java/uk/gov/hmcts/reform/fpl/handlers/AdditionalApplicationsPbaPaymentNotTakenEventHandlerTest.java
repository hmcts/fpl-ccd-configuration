package uk.gov.hmcts.reform.fpl.handlers;

import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.config.HighCourtAdminEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.events.AdditionalApplicationsPbaPaymentNotTakenEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.BaseCaseNotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.RecipientsRequest;
import uk.gov.hmcts.reform.fpl.service.CourtService;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.LocalAuthorityRecipientsService;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.AdditionalApplicationsUploadedEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.workallocation.WorkAllocationTaskService;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.Set;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.INTERLOCUTORY_UPLOAD_PBA_PAYMENT_NOT_TAKEN_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.UserRole.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.AUTH_TOKEN;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.CTSC_INBOX;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.caseData;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {AdditionalApplicationsPbaPaymentNotTakenEventHandler.class, LookupTestConfig.class,
    CourtService.class, HighCourtAdminEmailLookupConfiguration.class})
class AdditionalApplicationsPbaPaymentNotTakenEventHandlerTest {
    private final BaseCaseNotifyData additionalApplicationsPaymentNotTakenParameters = BaseCaseNotifyData.builder()
        .caseUrl("http://fpl/case/12345")
        .build();

    @MockBean
    private IdamClient idamClient;

    @MockBean
    private LocalAuthorityRecipientsService localAuthorityRecipients;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private AdditionalApplicationsUploadedEmailContentProvider additionalApplicationsUploadedEmailContentProvider;

    @MockBean
    private FeatureToggleService featureToggleService;

    @MockBean
    private WorkAllocationTaskService workAllocationTaskService;

    @Autowired
    private AdditionalApplicationsPbaPaymentNotTakenEventHandler additionalApplicationsPbaPaymentNotTakenEventHandler;

    @Test
    void shouldNotifyAdminWhenUploadedAdditionalApplicationsIsNotUsingPbaPayment() {
        CaseData caseData = caseData();
        given(featureToggleService.isWATaskEmailsEnabled()).willReturn(true);

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

        given(featureToggleService.isWATaskEmailsEnabled()).willReturn(true);

        given(idamClient.getUserInfo(AUTH_TOKEN)).willReturn(
            UserInfo.builder().sub(CTSC_INBOX).roles(LOCAL_AUTHORITY.getRoleNames()).build());

        given(localAuthorityRecipients.getRecipients(
            RecipientsRequest.builder().caseData(caseData).build()))
            .willReturn(Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS));

        given(additionalApplicationsUploadedEmailContentProvider.getPbaPaymentNotTakenNotifyData(caseData))
            .willReturn(additionalApplicationsPaymentNotTakenParameters);

        additionalApplicationsPbaPaymentNotTakenEventHandler.notifyAdmin(
            new AdditionalApplicationsPbaPaymentNotTakenEvent(caseData));

        verify(notificationService).sendEmail(INTERLOCUTORY_UPLOAD_PBA_PAYMENT_NOT_TAKEN_TEMPLATE, CTSC_INBOX,
            additionalApplicationsPaymentNotTakenParameters, caseData.getId());
    }

    @Test
    void shouldNotNotifyCtscWhenPBAPaymentNotTakenAndToggledOff() {
        given(featureToggleService.isWATaskEmailsEnabled()).willReturn(false);

        CaseData caseData = CaseData.builder()
            .id(RandomUtils.nextLong())
            .sendToCtsc("Yes")
            .build();

        additionalApplicationsPbaPaymentNotTakenEventHandler.notifyAdmin(
            new AdditionalApplicationsPbaPaymentNotTakenEvent(caseData));

        verify(notificationService, never()).sendEmail(INTERLOCUTORY_UPLOAD_PBA_PAYMENT_NOT_TAKEN_TEMPLATE, CTSC_INBOX,
            additionalApplicationsPaymentNotTakenParameters, caseData.getId());
    }
}
