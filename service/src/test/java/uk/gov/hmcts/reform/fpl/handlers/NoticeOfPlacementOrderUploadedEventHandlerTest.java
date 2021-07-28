package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.events.NoticeOfPlacementOrderUploadedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.notify.BaseCaseNotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.LocalAuthorityInboxRecipientsRequest;
import uk.gov.hmcts.reform.fpl.model.notify.OrderIssuedNotifyData;
import uk.gov.hmcts.reform.fpl.service.CourtService;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.LocalAuthorityEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.OrderIssuedEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.representative.RepresentativeNotificationService;

import java.util.Set;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.NOTICE_OF_PLACEMENT_ORDER_UPLOADED_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_REPRESENTATIVES;
import static uk.gov.hmcts.reform.fpl.enums.IssuedOrderType.NOTICE_OF_PLACEMENT_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.caseData;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {NoticeOfPlacementOrderUploadedEventHandler.class, InboxLookupService.class,
    LookupTestConfig.class,
    IssuedOrderAdminNotificationHandler.class, RepresentativeNotificationService.class,
    CourtService.class})
class NoticeOfPlacementOrderUploadedEventHandlerTest {

    @MockBean
    private InboxLookupService inboxLookupService;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private OrderIssuedEmailContentProvider orderIssuedEmailContentProvider;

    @MockBean
    private RepresentativeNotificationService representativeNotificationService;

    @MockBean
    private LocalAuthorityEmailContentProvider localAuthorityEmailContentProvider;

    @MockBean
    private IssuedOrderAdminNotificationHandler issuedOrderAdminNotificationHandler;

    @Autowired
    private NoticeOfPlacementOrderUploadedEventHandler noticeOfPlacementOrderUploadedEventHandler;

    private final CaseData caseData = caseData();

    private final DocumentReference testDocument = DocumentReference.builder()
        .filename("NoticeOfPlacement")
        .url("url")
        .binaryUrl("testUrl")
        .build();

    private final NoticeOfPlacementOrderUploadedEvent event = new NoticeOfPlacementOrderUploadedEvent(
        caseData, testDocument);

    @Test
    void shouldSendEmailForPlacementOrderUploaded() {

        final BaseCaseNotifyData localAuthorityParameters = BaseCaseNotifyData.builder()
            .caseUrl("test1")
            .build();

        final OrderIssuedNotifyData representativesParameters = OrderIssuedNotifyData.builder()
            .caseUrl("test2")
            .build();

        given(inboxLookupService.getRecipients(
            LocalAuthorityInboxRecipientsRequest.builder().caseData(caseData).build()))
            .willReturn(Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS));

        given(localAuthorityEmailContentProvider.buildNoticeOfPlacementOrderUploadedNotification(caseData))
            .willReturn(localAuthorityParameters);

        given(orderIssuedEmailContentProvider.getNotifyDataWithoutCaseUrl(
            caseData, event.getOrderDocument(), NOTICE_OF_PLACEMENT_ORDER))
            .willReturn(representativesParameters);

        noticeOfPlacementOrderUploadedEventHandler.notifyParties(event);

        verify(notificationService).sendEmail(
            NOTICE_OF_PLACEMENT_ORDER_UPLOADED_TEMPLATE,
            Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS),
            localAuthorityParameters,
            caseData.getId().toString());

        verify(issuedOrderAdminNotificationHandler).notifyAdmin(
            caseData,
            testDocument,
            NOTICE_OF_PLACEMENT_ORDER);

        verify(representativeNotificationService).sendToRepresentativesByServedPreference(
            DIGITAL_SERVICE,
            NOTICE_OF_PLACEMENT_ORDER_UPLOADED_TEMPLATE,
            localAuthorityParameters,
            caseData);

        verify(representativeNotificationService).sendToRepresentativesByServedPreference(
            EMAIL,
            ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_REPRESENTATIVES,
            representativesParameters,
            caseData);
    }
}
