package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.events.NoticeOfChangeEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.interfaces.WithSolicitor;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.NoticeOfChangeContentProvider;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.NOTICE_OF_CHANGE_FORMER_REPRESENTATIVE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.NOTICE_OF_CHANGE_NEW_REPRESENTATIVE;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NoticeOfChangeEventHandler {

    private final NotificationService notificationService;
    private final NoticeOfChangeContentProvider noticeOfChangeContentProvider;

    @Async
    @EventListener
    public void notifySolicitorAccessGranted(final NoticeOfChangeEvent event) {
        CaseData caseData = event.getCaseData();

        WithSolicitor newParty = event.getNewParty();
        String recipient = newParty.getSolicitor().getEmail();

        NotifyData notifyData = noticeOfChangeContentProvider.buildNoticeOfChangeRespondentSolicitorTemplate(
            caseData, newParty
        );

        notificationService.sendEmail(NOTICE_OF_CHANGE_NEW_REPRESENTATIVE, recipient, notifyData, caseData.getId());
    }

    @Async
    @EventListener
    public void notifySolicitorAccessRevoked(final NoticeOfChangeEvent event) {
        CaseData caseData = event.getCaseData();
        WithSolicitor oldParty = event.getOldParty();
        String partyType = oldParty.getClass().getSimpleName().toLowerCase();

        if (isNull(oldParty.getSolicitor())) {
            log.info("No previous solicitor for {}", partyType);
        } else {
            String recipient = oldParty.getSolicitor().getEmail();
            if (isBlank(recipient)) {
                log.info("No email address for previous {} solicitor", partyType);
            } else {
                NotifyData notifyData = noticeOfChangeContentProvider.buildNoticeOfChangeRespondentSolicitorTemplate(
                        caseData, oldParty
                );

                notificationService.sendEmail(
                    NOTICE_OF_CHANGE_FORMER_REPRESENTATIVE,
                    recipient,
                    notifyData,
                    caseData.getId()
                );
            }
        }
    }
}
