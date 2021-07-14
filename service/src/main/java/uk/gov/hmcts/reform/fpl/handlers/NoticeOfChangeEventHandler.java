package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.events.NoticeOfChangeEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
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

        Respondent newRespondent = event.getNewRespondent();
        String recipient = newRespondent.getSolicitor().getEmail();

        NotifyData notifyData =
            noticeOfChangeContentProvider.buildNoticeOfChangeRespondentSolicitorTemplate(caseData, newRespondent);

        notificationService.sendEmail(NOTICE_OF_CHANGE_NEW_REPRESENTATIVE, recipient, notifyData, caseData.getId());
    }

    @Async
    @EventListener
    public void notifySolicitorAccessRevoked(final NoticeOfChangeEvent event) {
        CaseData caseData = event.getCaseData();

        Respondent oldRespondent = event.getOldRespondent();
        if (isNull(oldRespondent.getSolicitor())) {
            log.info("No previous oldRespondent for respondent");
        } else {
            String recipient = oldRespondent.getSolicitor().getEmail();
            if (isBlank(recipient)) {
                log.info("No email address for previous respondent oldRespondent");
            } else {
                NotifyData notifyData =
                    noticeOfChangeContentProvider.buildNoticeOfChangeRespondentSolicitorTemplate(
                        caseData, oldRespondent);

                notificationService.sendEmail(
                    NOTICE_OF_CHANGE_FORMER_REPRESENTATIVE,
                    recipient,
                    notifyData,
                    caseData.getId());
            }
        }
    }
}
