package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.events.NoticeOfChangeThirdPartyEvent;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.service.OrganisationService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.NoticeOfChangeContentProvider;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.NOTICE_OF_CHANGE_FORMER_REPRESENTATIVE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.NOTICE_OF_CHANGE_NEW_REPRESENTATIVE;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NoticeOfChangeThirdPartyEventHandler {

    private final NotificationService notificationService;
    private final NoticeOfChangeContentProvider noticeOfChangeContentProvider;

    @Async
    @EventListener
    public void notifyThirdPartySolicitorAccessGranted(final NoticeOfChangeThirdPartyEvent event) {
        NotifyData notifyData = noticeOfChangeContentProvider.buildNoticeOfChangeThirdPartySolicitorTemplate(event.getCaseData()
        );

        notificationService.sendEmail(NOTICE_OF_CHANGE_NEW_REPRESENTATIVE, event.getNewThirdPartyOrg().getEmail(),
            notifyData, event.getCaseData().getId());
    }

    @Async
    @EventListener
    public void notifyThirdPartySolicitorAccessRemoved(final NoticeOfChangeThirdPartyEvent event) {
        NotifyData notifyData = noticeOfChangeContentProvider.buildNoticeOfChangeThirdPartySolicitorTemplate(event.getCaseData()
        );

        notificationService.sendEmail(
            NOTICE_OF_CHANGE_FORMER_REPRESENTATIVE,
            event.getOldThirdPartyOrg().getEmail(),
            notifyData, event.getCaseData().getId()
        );
    }
}
