package uk.gov.hmcts.reform.fpl.handlers;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.events.C2PbaPaymentNotTakenEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.C2UploadedEmailContentProvider;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.Map;
import java.util.Set;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.C2_UPLOAD_PBA_PAYMENT_NOT_TAKEN_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.UserRole.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.AUTH_TOKEN;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.CTSC_INBOX;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.caseData;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {C2PbaPaymentNotTakenEventHandler.class, LookupTestConfig.class,
    HmctsAdminNotificationHandler.class})
class C2PbaPaymentNotTakenEventHandlerTest {
    private final Map<String, Object> c2PaymentNotTakenParameters = ImmutableMap.<String, Object>builder()
        .put("caseUrl", "null/case/" + JURISDICTION + "/" + CASE_TYPE + "/12345")
        .build();

    @MockBean
    private IdamClient idamClient;

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
        CaseData caseData = caseData();

        given(c2UploadedEmailContentProvider.buildC2UploadPbaPaymentNotTakenNotification(caseData))
            .willReturn(c2PaymentNotTakenParameters);

        c2PbaPaymentNotTakenEventHandler.sendEmail(new C2PbaPaymentNotTakenEvent(caseData));

        verify(notificationService).sendEmail(
            C2_UPLOAD_PBA_PAYMENT_NOT_TAKEN_TEMPLATE, "admin@family-court.com", c2PaymentNotTakenParameters,
            caseData.getId().toString());
    }

    @Test
    void shouldNotifyCtscAdminWhenUploadedC2IsNotUsingPbaPaymentAndCtscIsEnabled() {
        CaseData caseData = CaseData.builder()
            .id(RandomUtils.nextLong())
            .sendToCtsc("Yes")
            .build();

        given(idamClient.getUserInfo(AUTH_TOKEN)).willReturn(
            UserInfo.builder().sub(CTSC_INBOX).roles(LOCAL_AUTHORITY.getRoles()).build());

        given(inboxLookupService.getRecipients(caseData))
            .willReturn(Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS));

        given(c2UploadedEmailContentProvider.buildC2UploadPbaPaymentNotTakenNotification(caseData))
            .willReturn(c2PaymentNotTakenParameters);

        c2PbaPaymentNotTakenEventHandler.sendEmail(new C2PbaPaymentNotTakenEvent(caseData));

        verify(notificationService).sendEmail(C2_UPLOAD_PBA_PAYMENT_NOT_TAKEN_TEMPLATE, CTSC_INBOX,
            c2PaymentNotTakenParameters, caseData.getId().toString());
    }
}
