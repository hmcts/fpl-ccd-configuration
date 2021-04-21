package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.events.cmo.CaseManagementOrderIssuedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.notify.LocalAuthorityInboxRecipientsRequest;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.IssuedCMOTemplate;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.RepresentativesInbox;
import uk.gov.hmcts.reform.fpl.service.email.content.CaseManagementOrderEmailContentProvider;

import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_ORDER_ISSUED_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.IssuedOrderType.CMO;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.CAFCASS_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_CODE;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.caseData;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.DOCUMENT_CONTENT;

@ExtendWith(SpringExtension.class)
class CaseManagementOrderIssuedEventHandlerTest {

    @Mock
    private InboxLookupService inboxLookupService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private CaseManagementOrderEmailContentProvider caseManagementOrderEmailContentProvider;

    @Mock
    private DocumentDownloadService documentDownloadService;

    @Mock
    private IssuedOrderAdminNotificationHandler issuedOrderAdminNotificationHandler;

    @Mock
    private CafcassLookupConfiguration cafcassLookupConfiguration;

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @Mock
    private RepresentativesInbox representativesInbox;

    @InjectMocks
    private CaseManagementOrderIssuedEventHandler underTest;

    private final IssuedCMOTemplate digitalRepCMOTemplateData
        = IssuedCMOTemplate.builder().familyManCaseNumber("1").build();

    private final IssuedCMOTemplate emailRepCMOTemplateData
        = IssuedCMOTemplate.builder().familyManCaseNumber("2").build();

    private final CaseData caseData = caseData();
    private final HearingOrder cmo = buildCmo();

    private final CaseManagementOrderIssuedEvent event = new CaseManagementOrderIssuedEvent(caseData, cmo);
    private static final String SEND_DOCUMENT_EVENT = "internal-change-SEND_DOCUMENT";

    @BeforeEach
    void init() {
        given(documentDownloadService.downloadDocument(anyString())).willReturn(DOCUMENT_CONTENT);
    }

    @Test
    void shouldNotifyPartiesOfCMOIssued() {
        CaseData caseData = caseData();
        HearingOrder cmo = buildCmo();

        given(inboxLookupService.getRecipients(
            LocalAuthorityInboxRecipientsRequest.builder().caseData(caseData).build()))
            .willReturn(Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS));

        CafcassLookupConfiguration.Cafcass cafcass =
            new CafcassLookupConfiguration.Cafcass(LOCAL_AUTHORITY_CODE, CAFCASS_EMAIL_ADDRESS);

        when(cafcassLookupConfiguration.getCafcass(LOCAL_AUTHORITY_CODE)).thenReturn(cafcass);
        when(representativesInbox.getEmailsByPreference(caseData, EMAIL)).thenReturn(Set.of("fred@flinstone.com",
            "barney@rubble.com"));
        when(representativesInbox.getEmailsByPreference(caseData, DIGITAL_SERVICE)).thenReturn(Set.of(
            "fred@flinstone.com"));

        given(caseManagementOrderEmailContentProvider.buildCMOIssuedNotificationParameters(caseData, cmo,
            DIGITAL_SERVICE))
            .willReturn(digitalRepCMOTemplateData);

        given(caseManagementOrderEmailContentProvider.buildCMOIssuedNotificationParameters(caseData, cmo,
            EMAIL))
            .willReturn(emailRepCMOTemplateData);

        underTest.notifyParties(event);

        verify(notificationService).sendEmail(
            CMO_ORDER_ISSUED_NOTIFICATION_TEMPLATE,
            Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS),
            digitalRepCMOTemplateData,
            caseData.getId().toString());

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

        verify(issuedOrderAdminNotificationHandler).notifyAdmin(
            caseData,
            cmo.getOrder(),
            CMO);
    }

    @Test
    void shouldNotifyPostRepresentatives() {
        HearingOrder cmo = buildCmo();

        underTest.sendDocumentToPostRepresentatives(event);

        verify(coreCaseDataService).triggerEvent(JURISDICTION,
            CASE_TYPE,
            12345L,
            SEND_DOCUMENT_EVENT,
            Map.of("documentToBeSent", cmo.getOrder()));
    }

    private HearingOrder buildCmo() {
        return HearingOrder.builder().order(DocumentReference.builder()
            .filename("CMO")
            .url("url")
            .binaryUrl("testUrl")
            .build()).build();
    }
}
