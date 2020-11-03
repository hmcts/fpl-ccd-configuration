package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.events.NoticeOfProceedingsIssuedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.NoticeOfProceedings;
import uk.gov.hmcts.reform.fpl.model.notify.allocatedjudge.AllocatedJudgeTemplateForNoticeOfProceedings;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.NoticeOfProceedingsEmailContentProvider;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.NOTICE_OF_PROCEEDINGS_ISSUED_JUDGE_TEMPLATE;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NoticeOfProceedingsIssuedEventHandler {
    private final NotificationService notificationService;
    private final NoticeOfProceedingsEmailContentProvider noticeOfProceedingsEmailContentProvider;

    @EventListener
    public void notifyAllocatedJudgeOfIssuedNoticeOfProceedings(NoticeOfProceedingsIssuedEvent event) {
        CaseData caseData = event.getCaseData();

        if (hasJudgeEmail(caseData.getNoticeOfProceedings())) {
            AllocatedJudgeTemplateForNoticeOfProceedings parameters = noticeOfProceedingsEmailContentProvider
                .buildAllocatedJudgeNotification(caseData);

            String email = caseData.getNoticeOfProceedings().getJudgeAndLegalAdvisor().getJudgeEmailAddress();

            notificationService.sendEmail(NOTICE_OF_PROCEEDINGS_ISSUED_JUDGE_TEMPLATE, email, parameters,
                caseData.getId().toString());
        }
    }

    private boolean hasJudgeEmail(NoticeOfProceedings noticeOfProceedings) {
        return isNotEmpty(noticeOfProceedings.getJudgeAndLegalAdvisor())
            && isNotEmpty(noticeOfProceedings.getJudgeAndLegalAdvisor().getJudgeEmailAddress());
    }
}
