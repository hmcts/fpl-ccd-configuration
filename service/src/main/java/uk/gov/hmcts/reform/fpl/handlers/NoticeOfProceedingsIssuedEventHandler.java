package uk.gov.hmcts.reform.fpl.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.events.NoticeOfProceedingsIssuedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.EventData;
import uk.gov.hmcts.reform.fpl.model.notify.allocatedjudge.AllocatedJudgeTemplateForNoticeOfProceedings;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.NoticeOfProceedingsEmailContentProvider;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.NOTICE_OF_PROCEEDINGS_ISSUED_JUDGE_TEMPLATE;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NoticeOfProceedingsIssuedEventHandler {
    private final NotificationService notificationService;
    private final NoticeOfProceedingsEmailContentProvider noticeOfProceedingsEmailContentProvider;
    private final ObjectMapper mapper;

    @EventListener
    public void notifyAllocatedJudgeOfIssuedStandardDirectionsOrder(NoticeOfProceedingsIssuedEvent event) {
        EventData eventData = new EventData(event);
        CaseData caseData = mapper.convertValue(eventData.getCaseDetails().getData(), CaseData.class);

        AllocatedJudgeTemplateForNoticeOfProceedings parameters = noticeOfProceedingsEmailContentProvider
                .buildAllocatedJudgeNotification(eventData.getCaseDetails());

        String email = caseData.getNoticeOfProceedings().getJudgeAndLegalAdvisor().getJudgeEmailAddress();

        notificationService.sendEmail(NOTICE_OF_PROCEEDINGS_ISSUED_JUDGE_TEMPLATE, email, parameters,
                eventData.getReference());
    }
}
