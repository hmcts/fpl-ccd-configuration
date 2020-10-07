package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.events.NoticeOfPlacementOrderUploadedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.LocalAuthorityEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.OrderIssuedEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.representative.RepresentativeNotificationService;

import java.util.Map;
import java.util.Set;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.NOTICE_OF_PLACEMENT_ORDER_UPLOADED_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_REPRESENTATIVES;
import static uk.gov.hmcts.reform.fpl.enums.IssuedOrderType.NOTICE_OF_PLACEMENT_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.DOCUMENT_CONTENTS;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.caseData;

@ExtendWith(SpringExtension.class)
class NoticeOfPlacementOrderUploadedEventHandlerTest {

    @Mock
    private InboxLookupService inboxLookupService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private OrderIssuedEmailContentProvider orderIssuedEmailContentProvider;

    @Mock
    private RepresentativeNotificationService representativeNotificationService;

    @Mock
    private LocalAuthorityEmailContentProvider localAuthorityEmailContentProvider;

    @Mock
    private IssuedOrderAdminNotificationHandler issuedOrderAdminNotificationHandler;

    @InjectMocks
    private NoticeOfPlacementOrderUploadedEventHandler noticeOfPlacementOrderUploadedEventHandler;

    @Test
    void shouldSendEmailForPlacementOrderUploaded() {

        final Map<String, Object> localAuthorityParameters = Map.of("key1", "value1");
        final Map<String, Object> representativesParameters = Map.of("key2", "value2");

        final CaseData caseData = caseData();
        final NoticeOfPlacementOrderUploadedEvent event = new NoticeOfPlacementOrderUploadedEvent(
            caseData, DOCUMENT_CONTENTS);

        given(inboxLookupService.getRecipients(caseData))
            .willReturn(Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS));

        given(localAuthorityEmailContentProvider.buildNoticeOfPlacementOrderUploadedNotification(caseData))
            .willReturn(localAuthorityParameters);

        given(orderIssuedEmailContentProvider.buildParametersWithoutCaseUrl(
            caseData, DOCUMENT_CONTENTS, NOTICE_OF_PLACEMENT_ORDER))
            .willReturn(representativesParameters);

        noticeOfPlacementOrderUploadedEventHandler.sendEmailForNoticeOfPlacementOrderUploaded(event);

        verify(notificationService).sendEmail(
            NOTICE_OF_PLACEMENT_ORDER_UPLOADED_TEMPLATE,
            Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS),
            localAuthorityParameters,
            caseData.getId().toString());

        verify(issuedOrderAdminNotificationHandler).sendToAdmin(
            caseData,
            event.getDocumentContents(),
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
