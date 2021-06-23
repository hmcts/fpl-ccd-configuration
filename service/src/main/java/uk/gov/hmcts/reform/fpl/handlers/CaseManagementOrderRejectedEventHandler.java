package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.events.cmo.CaseManagementOrderRejectedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.LocalAuthorityInboxRecipientsRequest;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.RejectedCMOTemplate;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.CaseManagementOrderEmailContentProvider;

import java.util.Collection;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_REJECTED_BY_JUDGE_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_REJECTED_BY_JUDGE_TEMPLATE_CHILD_NAME;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseManagementOrderRejectedEventHandler {
    private final NotificationService notificationService;
    private final InboxLookupService inboxLookupService;
    private final CaseManagementOrderEmailContentProvider contentProvider;
    private final FeatureToggleService toggleService;

    @EventListener
    public void notifyLocalAuthority(final CaseManagementOrderRejectedEvent event) {
        CaseData caseData = event.getCaseData();
        RejectedCMOTemplate parameters = contentProvider.buildCMORejectedByJudgeNotificationParameters(
            caseData, event.getCmo()
        );

        Collection<String> emails = inboxLookupService.getRecipients(
            LocalAuthorityInboxRecipientsRequest.builder().caseData(caseData).build()
        );

        String template = toggleService.isEldestChildLastNameEnabled() ? CMO_REJECTED_BY_JUDGE_TEMPLATE_CHILD_NAME
                                                                       : CMO_REJECTED_BY_JUDGE_TEMPLATE;

        notificationService.sendEmail(template, emails, parameters, caseData.getId().toString());
    }
}
