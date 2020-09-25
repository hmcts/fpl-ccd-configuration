package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.events.CaseManagementOrderRejectedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.RejectedCMOTemplate;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.CaseManagementOrderEmailContentProvider;

import java.util.Collection;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_REJECTED_BY_JUDGE_TEMPLATE;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseManagementOrderRejectedEventHandler {
    private final NotificationService notificationService;
    private final InboxLookupService inboxLookupService;
    private final CaseManagementOrderEmailContentProvider caseManagementOrderEmailContentProvider;

    @EventListener
    public void notifyLocalAuthorityOfRejectedCaseManagementOrder(final CaseManagementOrderRejectedEvent event) {
        CaseData caseData = event.getCaseData();
        RejectedCMOTemplate parameters =
            caseManagementOrderEmailContentProvider.buildCMORejectedByJudgeNotificationParameters(
                caseData, event.getCmo());

        Collection<String> emails = inboxLookupService.getRecipients(caseData);

        notificationService.sendEmail(CMO_REJECTED_BY_JUDGE_TEMPLATE, emails, parameters, caseData.getId().toString());
    }
}
