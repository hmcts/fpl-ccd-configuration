package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.events.cmo.CaseManagementOrderRejectedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.RecipientsRequest;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.RejectedCMOTemplate;
import uk.gov.hmcts.reform.fpl.service.LocalAuthorityRecipientsService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.CaseManagementOrderEmailContentProvider;

import java.util.Collection;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_REJECTED_BY_JUDGE_TEMPLATE;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseManagementOrderRejectedEventHandler {
    private final NotificationService notificationService;
    private final LocalAuthorityRecipientsService localAuthorityRecipients;
    private final CaseManagementOrderEmailContentProvider contentProvider;

    @EventListener
    public void notifyLocalAuthority(final CaseManagementOrderRejectedEvent event) {

        final CaseData caseData = event.getCaseData();
        final RejectedCMOTemplate parameters = contentProvider.buildCMORejectedByJudgeNotificationParameters(
            caseData, event.getCmo());

        final RecipientsRequest recipientsRequest = RecipientsRequest.builder()
            .caseData(caseData)
            .secondaryLocalAuthorityExcluded(true)
            .build();

        final Collection<String> recipients = localAuthorityRecipients.getRecipients(recipientsRequest);

        notificationService.sendEmail(CMO_REJECTED_BY_JUDGE_TEMPLATE, recipients, parameters, caseData.getId());
    }
}
