package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

        RespondentSolicitor solicitor = event.getNewRespondentSolicitor();
        String recipient = solicitor.getEmail();

        NotifyData notifyData =
            noticeOfChangeContentProvider.buildNoticeOfChangeRespondentSolicitorTemplate(caseData,
                solicitor);

        notificationService.sendEmail(NOTICE_OF_CHANGE_NEW_REPRESENTATIVE, recipient, notifyData, caseData.getId());
    }

    @Async
    @EventListener
    public void notifySolicitorAccessRevoked(final NoticeOfChangeEvent event) {
        CaseData caseData = event.getCaseData();

        RespondentSolicitor solicitor = event.getOldRespondentSolicitor();
        if (solicitor == null) {
            log.info("No previous solicitor for respondent");
        } else {
            String recipient = solicitor.getEmail();
            if (isBlank(recipient)) {
                log.info("No email address for previous respondent solicitor");
            } else {
                NotifyData notifyData =
                    noticeOfChangeContentProvider.buildNoticeOfChangeRespondentSolicitorTemplate(caseData,
                        solicitor);

                notificationService.sendEmail(
                    NOTICE_OF_CHANGE_FORMER_REPRESENTATIVE,
                    recipient,
                    notifyData,
                    caseData.getId());
            }
        }
    }
}
