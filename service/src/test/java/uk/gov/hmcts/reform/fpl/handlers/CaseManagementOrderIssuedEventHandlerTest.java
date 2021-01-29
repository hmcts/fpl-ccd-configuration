package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.events.cmo.CaseManagementOrderIssuedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.notify.LocalAuthorityInboxRecipientsRequest;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.IssuedCMOTemplate;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.CaseManagementOrderEmailContentProvider;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.util.Set;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_ORDER_ISSUED_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.IssuedOrderType.CMO;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.caseData;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.DOCUMENT_CONTENT;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {CaseManagementOrderIssuedEventHandler.class, LookupTestConfig.class,
    IssuedOrderAdminNotificationHandler.class, HmctsAdminNotificationHandler.class,
    FixedTimeConfiguration.class})
class CaseManagementOrderIssuedEventHandlerTest {

    @MockBean
    private InboxLookupService inboxLookupService;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private CaseManagementOrderEmailContentProvider caseManagementOrderEmailContentProvider;

    @MockBean
    private DocumentDownloadService documentDownloadService;

    @MockBean
    private IssuedOrderAdminNotificationHandler issuedOrderAdminNotificationHandler;

    @Autowired
    private CaseManagementOrderIssuedEventHandler caseManagementOrderIssuedEventHandler;

    private final IssuedCMOTemplate digitalRepCMOTemplateData
        = IssuedCMOTemplate.builder().familyManCaseNumber("1").build();

    private final IssuedCMOTemplate emailRepCMOTemplateData
        = IssuedCMOTemplate.builder().familyManCaseNumber("2").build();

    private final CaseData caseData = caseData();
    private final HearingOrder cmo = buildCmo();

    private final CaseManagementOrderIssuedEvent event = new CaseManagementOrderIssuedEvent(caseData, cmo);

    @BeforeEach
    void init() {
        given(documentDownloadService.downloadDocument(anyString())).willReturn(DOCUMENT_CONTENT);
    }

    @Test
    void shouldNotifyHmctsAdminAndLocalAuthorityOfCMOIssued() {
        CaseData caseData = caseData();
        HearingOrder cmo = buildCmo();

        given(inboxLookupService.getRecipients(
            LocalAuthorityInboxRecipientsRequest.builder().caseData(caseData).build()))
            .willReturn(Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS));

        given(caseManagementOrderEmailContentProvider.buildCMOIssuedNotificationParameters(caseData, cmo,
            DIGITAL_SERVICE))
            .willReturn(digitalRepCMOTemplateData);

        caseManagementOrderIssuedEventHandler.notifyParties(event);

        verify(notificationService).sendEmail(
            CMO_ORDER_ISSUED_NOTIFICATION_TEMPLATE,
            Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS),
            digitalRepCMOTemplateData,
            caseData.getId().toString());

        verify(issuedOrderAdminNotificationHandler).notifyAdmin(
            caseData,
            cmo.getOrder(),
            CMO);
    }

    @Test
    void shouldNotifyRepresentativesOfCMOIssued() {
        CaseData caseData = caseData();
        HearingOrder cmo = buildCmo();

        given(caseManagementOrderEmailContentProvider.buildCMOIssuedNotificationParameters(caseData, cmo,
            DIGITAL_SERVICE))
            .willReturn(digitalRepCMOTemplateData);

        given(caseManagementOrderEmailContentProvider.buildCMOIssuedNotificationParameters(caseData, cmo,
            EMAIL))
            .willReturn(emailRepCMOTemplateData);

        caseManagementOrderIssuedEventHandler.notifyParties(event);

        verify(notificationService).sendEmail(
            CMO_ORDER_ISSUED_NOTIFICATION_TEMPLATE,
            "FamilyPublicLaw+cafcass@gmail.com",
            emailRepCMOTemplateData,
            caseData.getId());

        verify(notificationService).sendEmail(
            CMO_ORDER_ISSUED_NOTIFICATION_TEMPLATE,
            "fred@flinstone.com",
            digitalRepCMOTemplateData,
            caseData.getId());

        verify(notificationService).sendEmail(
            CMO_ORDER_ISSUED_NOTIFICATION_TEMPLATE,
            "barney@rubble.com",
            emailRepCMOTemplateData,
            caseData.getId());
    }

    private HearingOrder buildCmo() {
        return HearingOrder.builder().order(DocumentReference.builder()
            .filename("CMO")
            .url("url")
            .binaryUrl("testUrl")
            .build()).build();
    }
}
