package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.events.NoticeOfChangeEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.NoticeOfChangeContentProvider;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.NOTICE_OF_CHANGE_FORMER_REPRESENTATIVE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.NOTICE_OF_CHANGE_NEW_REPRESENTATIVE;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NoticeOfChangeEventHandler {

    private final NotificationService notificationService;
    private final NoticeOfChangeContentProvider noticeOfChangeContentProvider;

    @Async
    @EventListener
    public void notifySolicitorAccessGranted(final NoticeOfChangeEvent event) {
        CaseData caseData = event.getCaseData();

        RespondentSolicitor solicitor = event.getNewRespondentSolicitor();
        String recipient = solicitor.getEmail();

        NotifyData notifyData =
            noticeOfChangeContentProvider.buildRespondentSolicitorAccessGrantedNotification(caseData,
            solicitor);

        notificationService.sendEmail(NOTICE_OF_CHANGE_NEW_REPRESENTATIVE, recipient, notifyData, caseData.getId());
    }

    @Async
    @EventListener
    public void notifySolicitorAccessRevoked(final NoticeOfChangeEvent event) {
        CaseData caseData = event.getCaseData();

        RespondentSolicitor solicitor = event.getOldRespondentSolicitor();
        String recipient = solicitor.getEmail();

        NotifyData notifyData =
            noticeOfChangeContentProvider.buildRespondentSolicitorAccessRevokedNotification(caseData,
            solicitor);

        notificationService.sendEmail(NOTICE_OF_CHANGE_FORMER_REPRESENTATIVE, recipient, notifyData, caseData.getId());
    }
}
