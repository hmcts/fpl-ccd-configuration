package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.events.AdditionalApplicationsPbaPaymentNotTakenEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.service.CourtService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.AdditionalApplicationsUploadedEmailContentProvider;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.INTERLOCUTORY_UPLOAD_PBA_PAYMENT_NOT_TAKEN_TEMPLATE;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AdditionalApplicationsPbaPaymentNotTakenEventHandler {

    private final CourtService courtService;
    private final NotificationService notificationService;
    private final AdditionalApplicationsUploadedEmailContentProvider additionalApplicationsUploadedEmailContentProvider;

    @EventListener
    public void notifyAdmin(final AdditionalApplicationsPbaPaymentNotTakenEvent event) {
        final CaseData caseData = event.getCaseData();

        final String recipient = courtService.getCourtEmail(caseData);
        final NotifyData notifyData =
            additionalApplicationsUploadedEmailContentProvider.getPbaPaymentNotTakenNotifyData(
                caseData);

        notificationService
            .sendEmail(INTERLOCUTORY_UPLOAD_PBA_PAYMENT_NOT_TAKEN_TEMPLATE, recipient, notifyData, caseData.getId());
    }
}
