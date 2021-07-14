package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration.Cafcass;
import uk.gov.hmcts.reform.fpl.enums.IssuedOrderType;
import uk.gov.hmcts.reform.fpl.events.cmo.CaseManagementOrderIssuedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.notify.LocalAuthorityInboxRecipientsRequest;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.IssuedCMOTemplate;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.RepresentativesInbox;
import uk.gov.hmcts.reform.fpl.service.email.content.CaseManagementOrderEmailContentProvider;

import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_ORDER_ISSUED_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.CAFCASS_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_CODE;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_EMAIL_ADDRESS;

@ExtendWith(MockitoExtension.class)
class CaseManagementOrderIssuedEventHandlerTest {

    private static final String SEND_DOCUMENT_EVENT = "internal-change-SEND_DOCUMENT";
    private static final long CASE_ID = 12345L;
    private static final IssuedCMOTemplate DIGITAL_REP_CMO_TEMPLATE_DATA = mock(IssuedCMOTemplate.class);
    private static final IssuedCMOTemplate EMAIL_REP_CMO_TEMPLATE_DATA = mock(IssuedCMOTemplate.class);
    private static final CaseData CASE_DATA = mock(CaseData.class);
    private static final HearingOrder CMO = mock(HearingOrder.class);
    private static final DocumentReference ORDER = mock(DocumentReference.class);
    private static final CaseManagementOrderIssuedEvent EVENT = new CaseManagementOrderIssuedEvent(CASE_DATA, CMO);

    @Mock
    private InboxLookupService inboxLookupService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private CaseManagementOrderEmailContentProvider cmoContentProvider;
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

    @BeforeEach
    void init() {
        when(CASE_DATA.getId()).thenReturn(CASE_ID);
        when(CMO.getOrder()).thenReturn(ORDER);
    }

    @Test
    void shouldNotifyPartiesOfCMOIssued() {
        when(CASE_DATA.getCaseLocalAuthority()).thenReturn(LOCAL_AUTHORITY_CODE);
        when(inboxLookupService.getRecipients(
            LocalAuthorityInboxRecipientsRequest.builder().caseData(CASE_DATA).build()
        )).thenReturn(Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS));
        when(cafcassLookupConfiguration.getCafcass(LOCAL_AUTHORITY_CODE))
            .thenReturn(new Cafcass(LOCAL_AUTHORITY_CODE, CAFCASS_EMAIL_ADDRESS));
        when(representativesInbox.getEmailsByPreference(CASE_DATA, EMAIL))
            .thenReturn(Set.of("barney@rubble.com"));
        when(representativesInbox.getEmailsByPreference(CASE_DATA, DIGITAL_SERVICE))
            .thenReturn(Set.of("fred@flinstone.com"));
        when(cmoContentProvider.buildCMOIssuedNotificationParameters(CASE_DATA, CMO, DIGITAL_SERVICE))
            .thenReturn(DIGITAL_REP_CMO_TEMPLATE_DATA);
        when(cmoContentProvider.buildCMOIssuedNotificationParameters(CASE_DATA, CMO, EMAIL))
            .thenReturn(EMAIL_REP_CMO_TEMPLATE_DATA);

        underTest.notifyParties(EVENT);

        verify(notificationService).sendEmail(
            CMO_ORDER_ISSUED_NOTIFICATION_TEMPLATE, Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS),
            DIGITAL_REP_CMO_TEMPLATE_DATA, String.valueOf(CASE_ID)
        );

        verify(notificationService).sendEmail(
            CMO_ORDER_ISSUED_NOTIFICATION_TEMPLATE, "FamilyPublicLaw+cafcass@gmail.com",
            EMAIL_REP_CMO_TEMPLATE_DATA, CASE_ID
        );

        verify(notificationService).sendEmail(
            CMO_ORDER_ISSUED_NOTIFICATION_TEMPLATE, "fred@flinstone.com",
            DIGITAL_REP_CMO_TEMPLATE_DATA, CASE_ID
        );

        verify(notificationService).sendEmail(
            CMO_ORDER_ISSUED_NOTIFICATION_TEMPLATE, "barney@rubble.com",
            EMAIL_REP_CMO_TEMPLATE_DATA, CASE_ID
        );

        verify(issuedOrderAdminNotificationHandler).notifyAdmin(CASE_DATA, CMO.getOrder(), IssuedOrderType.CMO);
    }

    @Test
    void shouldNotifyPostRepresentatives() {
        underTest.sendDocumentToPostRepresentatives(EVENT);

        verify(coreCaseDataService).triggerEvent(
            JURISDICTION, CASE_TYPE, CASE_ID, SEND_DOCUMENT_EVENT, Map.of("documentToBeSent", ORDER)
        );
    }
}
