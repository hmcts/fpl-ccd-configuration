package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences;
import uk.gov.hmcts.reform.fpl.events.SendNoticeOfHearing;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Recipient;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.notify.LocalAuthorityInboxRecipientsRequest;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.SendDocumentService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.NoticeOfHearingEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.representative.RepresentativeNotificationService;

import java.util.Collection;
import java.util.List;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.NOTICE_OF_NEW_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SendNoticeOfHearingHandler {

    private static final List<RepresentativeServingPreferences> SERVING_PREFERENCES = List.of(EMAIL, DIGITAL_SERVICE);

    private final NoticeOfHearingEmailContentProvider contentProvider;
    private final NotificationService notificationService;
    private final RepresentativeNotificationService representativeNotificationService;
    private final InboxLookupService inboxLookupService;
    private final CafcassLookupConfiguration cafcassLookupConfiguration;
    private final SendDocumentService sendDocumentService;

    @Async
    @EventListener
    public void notifyLocalAuthority(final SendNoticeOfHearing event) {
        final CaseData caseData = event.getCaseData();
        Collection<String> emails = inboxLookupService.getRecipients(
            LocalAuthorityInboxRecipientsRequest.builder().caseData(caseData).build());

        NotifyData notifyData = contentProvider.buildNewNoticeOfHearingNotification(
            caseData, event.getSelectedHearing(), DIGITAL_SERVICE
        );

        notificationService.sendEmail(
            NOTICE_OF_NEW_HEARING, emails, notifyData, caseData.getId().toString()
        );
    }

    @Async
    @EventListener
    public void notifyCafcass(final SendNoticeOfHearing event) {
        final CaseData caseData = event.getCaseData();

        final String recipient = cafcassLookupConfiguration.getCafcass(caseData.getCaseLocalAuthority()).getEmail();

        NotifyData notifyData = contentProvider.buildNewNoticeOfHearingNotification(
            caseData, event.getSelectedHearing(), EMAIL
        );

        notificationService.sendEmail(NOTICE_OF_NEW_HEARING, recipient, notifyData, caseData.getId());
    }

    @Async
    @EventListener
    public void notifyRepresentatives(final SendNoticeOfHearing event) {
        final CaseData caseData = event.getCaseData();

        SERVING_PREFERENCES.forEach(servingPreference -> {
            NotifyData notifyData = contentProvider.buildNewNoticeOfHearingNotification(
                caseData, event.getSelectedHearing(), servingPreference
            );

            representativeNotificationService.sendToRepresentativesByServedPreference(
                servingPreference, NOTICE_OF_NEW_HEARING, notifyData, caseData
            );
        });
    }

    @Async
    @EventListener
    public void sendNoticeOfHearingByPost(final SendNoticeOfHearing event) {
        final CaseData caseData = event.getCaseData();
        final DocumentReference noticeOfHearing = event.getSelectedHearing().getNoticeOfHearing();

        final List<Recipient> recipients = sendDocumentService.getStandardRecipients(caseData);

        sendDocumentService.sendDocuments(caseData, List.of(noticeOfHearing), recipients);
    }
}
