package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.WorkAllocationTaskType;
import uk.gov.hmcts.reform.fpl.events.AdditionalApplicationsPbaPaymentNotTakenEvent;
import uk.gov.hmcts.reform.fpl.events.FailedPBAPaymentEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.service.CourtService;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.AdditionalApplicationsUploadedEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.workallocation.WorkAllocationTaskService;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.INTERLOCUTORY_UPLOAD_PBA_PAYMENT_NOT_TAKEN_TEMPLATE;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AdditionalApplicationsPbaPaymentNotTakenEventHandler {

    private final CourtService courtService;
    private final NotificationService notificationService;
    private final FeatureToggleService featureToggleService;
    private final WorkAllocationTaskService workAllocationTaskService;
    private final AdditionalApplicationsUploadedEmailContentProvider additionalApplicationsUploadedEmailContentProvider;

    @EventListener
    public void notifyAdmin(final AdditionalApplicationsPbaPaymentNotTakenEvent event) {
        if (featureToggleService.isWATaskEmailsEnabled()) {
            final CaseData caseData = event.getCaseData();

            final String recipient = courtService.getCourtEmail(caseData);
            final NotifyData notifyData =
                additionalApplicationsUploadedEmailContentProvider.getPbaPaymentNotTakenNotifyData(
                    caseData);

            notificationService
                .sendEmail(INTERLOCUTORY_UPLOAD_PBA_PAYMENT_NOT_TAKEN_TEMPLATE, recipient, notifyData, caseData.getId());
        } else {
            log.info("WA EMAIL SKIPPED - failed payment - {}", event.getCaseData().getId());
        }
    }

    @EventListener
    public void createWorkAllocationTask(final AdditionalApplicationsPbaPaymentNotTakenEvent event) {
        CaseData caseData = event.getCaseData();
        workAllocationTaskService.createWorkAllocationTask(caseData, WorkAllocationTaskType.FAILED_PAYMENT);
    }
}
