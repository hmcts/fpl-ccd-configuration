package uk.gov.hmcts.reform.fpl.handlers;

import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.events.CaseManagementOrderIssuedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.IssuedCMOTemplate;
import uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.RepresentativeService;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.CaseManagementOrderEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.OrderIssuedEmailContentProvider;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;
import uk.gov.hmcts.reform.fpl.utils.TestDataHelper;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_ORDER_ISSUED_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_ADMIN;
import static uk.gov.hmcts.reform.fpl.enums.IssuedOrderType.CMO;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.COURT_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.CTSC_INBOX;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.DOCUMENT_CONTENTS;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_CODE;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_NAME;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.expectedRepresentatives;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.getCMOIssuedCaseLinkNotificationParameters;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.caseData;
import static uk.gov.hmcts.reform.fpl.utils.OrderIssuedNotificationTestHelper.getExpectedParameters;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.DOCUMENT_CONTENT;
import static uk.gov.hmcts.reform.fpl.utils.matchers.JsonMatcher.eqJson;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {CaseManagementOrderIssuedEventHandler.class, LookupTestConfig.class,
    IssuedOrderAdminNotificationHandler.class, HmctsAdminNotificationHandler.class, RepresentativeService.class,
    FixedTimeConfiguration.class})
class CaseManagementOrderIssuedEventHandlerTest {

    @MockBean
    private InboxLookupService inboxLookupService;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private RepresentativeService representativeService;

    @MockBean
    private OrderIssuedEmailContentProvider orderIssuedEmailContentProvider;

    @MockBean
    private CaseManagementOrderEmailContentProvider caseManagementOrderEmailContentProvider;

    @MockBean
    private DocumentDownloadService documentDownloadService;

    @Autowired
    private CaseManagementOrderIssuedEventHandler caseManagementOrderIssuedEventHandler;

    private final IssuedCMOTemplate issuedCMOTemplate = new IssuedCMOTemplate();

    @BeforeEach
    void init() {
        given(documentDownloadService.downloadDocument(anyString())).willReturn(DOCUMENT_CONTENT);
    }

    @Test
    void shouldNotifyHmctsAdminAndLocalAuthorityOfCMOIssued() {
        CaseData caseData = caseData();
        CaseManagementOrder cmo = buildCmo();

        given(inboxLookupService.getNotificationRecipientEmail(caseData))
            .willReturn(LOCAL_AUTHORITY_EMAIL_ADDRESS);

        given(caseManagementOrderEmailContentProvider.getCMOIssuedNotifyData(caseData, cmo,
            DIGITAL_SERVICE))
            .willReturn(issuedCMOTemplate);

        given(orderIssuedEmailContentProvider.getNotifyDataWithCaseUrl(caseData, DOCUMENT_CONTENTS, CMO))
            .willReturn(getExpectedParameters(CMO.getLabel(), true));

        caseManagementOrderIssuedEventHandler.notifyParties(
            new CaseManagementOrderIssuedEvent(caseData, cmo));

        verify(notificationService).sendEmail(
            CMO_ORDER_ISSUED_NOTIFICATION_TEMPLATE,
            LOCAL_AUTHORITY_EMAIL_ADDRESS,
            issuedCMOTemplate,
            caseData.getId());

        verify(notificationService).sendEmail(
            eq(ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_ADMIN),
            eq(COURT_EMAIL_ADDRESS),
            eqJson(getExpectedParameters(CMO.getLabel(), true)),
            eq(caseData.getId()));
    }

    @Test
    void shouldNotifyCtscAdminOfCMOIssued() {
        CaseData caseData = CaseData.builder()
            .id(RandomUtils.nextLong())
            .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
            .sendToCtsc("Yes")
            .build();
        CaseManagementOrder cmo = buildCmo();

        given(caseManagementOrderEmailContentProvider.buildCMOIssuedCaseLinkNotificationParameters(caseData,
            LOCAL_AUTHORITY_NAME))
            .willReturn(getCMOIssuedCaseLinkNotificationParameters());

        given(orderIssuedEmailContentProvider.getNotifyDataWithCaseUrl(caseData, DOCUMENT_CONTENTS, CMO))
            .willReturn(getExpectedParameters(CMO.getLabel(), true));

        caseManagementOrderIssuedEventHandler.notifyParties(
            new CaseManagementOrderIssuedEvent(caseData, cmo));

        verify(notificationService).sendEmail(
            eq(ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_ADMIN),
            eq(CTSC_INBOX),
            eq(getExpectedParameters(CMO.getLabel(), true)),
            eq(caseData.getId()));
    }

    @Test
    void shouldNotifyRepresentativesOfCMOIssued() {
        CaseData caseData = caseData();
        CaseManagementOrder cmo = buildCmo();

        given(representativeService.getRepresentativesByServedPreference(caseData.getRepresentatives(),
            DIGITAL_SERVICE))
            .willReturn(expectedRepresentatives());

        given(caseManagementOrderEmailContentProvider.getCMOIssuedNotifyData(caseData, cmo,
            DIGITAL_SERVICE))
            .willReturn(issuedCMOTemplate);

        caseManagementOrderIssuedEventHandler.notifyParties(
            new CaseManagementOrderIssuedEvent(caseData, cmo));

        verify(notificationService).sendEmail(
            CMO_ORDER_ISSUED_NOTIFICATION_TEMPLATE,
            "abc@example.com",
            issuedCMOTemplate,
            caseData.getId());
    }

    private CaseManagementOrder buildCmo() {
        return CaseManagementOrder.builder().order(TestDataHelper.testDocumentReference()).build();
    }
}
