package uk.gov.hmcts.reform.fpl.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.events.NotifyAllocatedJudgeEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.EventData;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.AllocatedJudgeContentProvider;

import java.util.Map;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ALLOCATED_JUDGE_TEMPLATE;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NotifyAllocatedJudgeEventHandler {
    private final NotificationService notificationService;
    private final AllocatedJudgeContentProvider allocatedJudgeContentProvider;
    private final ObjectMapper mapper;

    @EventListener
    public void notifyAllocatedJudge(NotifyAllocatedJudgeEvent event) {
        EventData eventData = new EventData(event);
        CaseData caseData = mapper.convertValue(eventData.getCaseDetails().getData(), CaseData.class);

        Map<String, Object> parameters = allocatedJudgeContentProvider
            .buildAllocatedJudgeNotificationParameters(eventData.getCaseDetails());

        String email = caseData.getAllocatedJudge().getJudgeEmailAddress();

        notificationService.sendEmail(ALLOCATED_JUDGE_TEMPLATE, email, parameters,
            eventData.getReference());
    }
}
