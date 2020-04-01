package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.events.CaseManagementOrderIssuedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.RepresentativeService;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.CaseManagementOrderEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.HmctsEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.OrderIssuedEmailContentProvider;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_ORDER_ISSUED_CASE_LINK_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_ADMIN;
import static uk.gov.hmcts.reform.fpl.enums.IssuedOrderType.CMO;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.COURT_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.CTSC_INBOX;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.DOCUMENT_CONTENTS;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_CODE;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_NAME;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.appendSendToCtscOnCallback;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.buildCallbackRequest;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.buildCaseDataWithRepresentatives;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.expectedRepresentatives;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.getCMOIssuedCaseLinkNotificationParameters;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.getExpectedCMOIssuedCaseLinkNotificationParametersForRepresentative;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;
import static uk.gov.hmcts.reform.fpl.utils.OrderIssuedNotificationTestHelper.getExpectedParametersForAdminWhenNoRepresentativesServedByPost;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {CaseManagementOrderIssuedEventHandler.class, JacksonAutoConfiguration.class,
    HmctsEmailContentProvider.class, LookupTestConfig.class, CaseManagementOrderCaseLinkNotificationHandler.class,
    CaseManagementOrderDocumentLinkNotificationHandler.class, IssuedOrderAdminNotificationHandler.class,
    HmctsAdminNotificationHandler.class})
public class CaseManagementOrderIssuedEventHandlerTest {
    @MockBean
    private RequestData requestData;

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

    @Autowired
    private CaseManagementOrderIssuedEventHandler caseManagementOrderIssuedEventHandler;

    @Test
    void shouldNotifyHmctsAdminAndLocalAuthorityOfCMOIssued() {
        CallbackRequest callbackRequest = callbackRequest();
        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        given(inboxLookupService.getNotificationRecipientEmail(caseDetails, LOCAL_AUTHORITY_CODE))
            .willReturn(LOCAL_AUTHORITY_EMAIL_ADDRESS);

        given(caseManagementOrderEmailContentProvider.buildCMOIssuedCaseLinkNotificationParameters(caseDetails,
            LOCAL_AUTHORITY_NAME))
            .willReturn(getCMOIssuedCaseLinkNotificationParameters());

        given(orderIssuedEmailContentProvider.buildNotificationParametersForHmctsAdmin(
            callbackRequest().getCaseDetails(), LOCAL_AUTHORITY_CODE, DOCUMENT_CONTENTS, CMO))
            .willReturn(getExpectedParametersForAdminWhenNoRepresentativesServedByPost(true));

        caseManagementOrderIssuedEventHandler.sendEmailsForIssuedCaseManagementOrder(
            new CaseManagementOrderIssuedEvent(callbackRequest, requestData, DOCUMENT_CONTENTS));

        verify(notificationService).sendEmail(
            CMO_ORDER_ISSUED_CASE_LINK_NOTIFICATION_TEMPLATE,
            LOCAL_AUTHORITY_EMAIL_ADDRESS,
            getCMOIssuedCaseLinkNotificationParameters(),
            "12345");

        verify(notificationService).sendEmail(
            ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_ADMIN,
            COURT_EMAIL_ADDRESS,
            getExpectedParametersForAdminWhenNoRepresentativesServedByPost(true),
            "12345");
    }

    @Test
    void shouldNotifyCtscAdminOfCMOIssued() throws Exception {
        CallbackRequest callbackRequest = appendSendToCtscOnCallback();
        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        given(caseManagementOrderEmailContentProvider.buildCMOIssuedCaseLinkNotificationParameters(caseDetails,
            LOCAL_AUTHORITY_NAME))
            .willReturn(getCMOIssuedCaseLinkNotificationParameters());

        given(orderIssuedEmailContentProvider.buildNotificationParametersForHmctsAdmin(
            callbackRequest.getCaseDetails(), LOCAL_AUTHORITY_CODE, DOCUMENT_CONTENTS, CMO))
            .willReturn(getExpectedParametersForAdminWhenNoRepresentativesServedByPost(true));

        caseManagementOrderIssuedEventHandler.sendEmailsForIssuedCaseManagementOrder(
            new CaseManagementOrderIssuedEvent(callbackRequest, requestData, DOCUMENT_CONTENTS));

        verify(notificationService).sendEmail(
            ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_ADMIN,
            CTSC_INBOX,
            getExpectedParametersForAdminWhenNoRepresentativesServedByPost(true),
            "12345");
    }

    @Test
    void shouldNotifyRepresentativesOfCMOIssued() throws Exception {
        CallbackRequest callbackRequest = buildCallbackRequest();
        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        CaseData caseData = buildCaseDataWithRepresentatives();

        given(representativeService.getRepresentativesByServedPreference(caseData.getRepresentatives(),
            DIGITAL_SERVICE))
            .willReturn(expectedRepresentatives());

        given(caseManagementOrderEmailContentProvider.buildCMOIssuedCaseLinkNotificationParameters(caseDetails,
            "Jon Snow"))
            .willReturn(getExpectedCMOIssuedCaseLinkNotificationParametersForRepresentative());

        caseManagementOrderIssuedEventHandler.sendEmailsForIssuedCaseManagementOrder(
            new CaseManagementOrderIssuedEvent(callbackRequest, requestData, DOCUMENT_CONTENTS));

        verify(notificationService).sendEmail(
            CMO_ORDER_ISSUED_CASE_LINK_NOTIFICATION_TEMPLATE,
            "abc@example.com",
            getExpectedCMOIssuedCaseLinkNotificationParametersForRepresentative(),
            "12345");
    }
}
