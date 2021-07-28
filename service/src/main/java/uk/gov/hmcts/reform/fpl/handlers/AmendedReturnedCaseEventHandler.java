package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.events.AmendedReturnedCaseEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.service.CourtService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.ReturnedCaseContentProvider;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.AMENDED_APPLICATION_RETURNED_ADMIN_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.AMENDED_APPLICATION_RETURNED_CAFCASS_TEMPLATE;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AmendedReturnedCaseEventHandler {

    private final CourtService courtService;
    private final NotificationService notificationService;
    private final ReturnedCaseContentProvider contentProvider;
    private final CafcassLookupConfiguration cafcassLookupConfiguration;

    @Async
    @EventListener
    public void notifyAdmin(AmendedReturnedCaseEvent event) {
        final CaseData caseData = event.getCaseData();

        final String recipient = courtService.getCourtEmail(caseData);
        final NotifyData notifyData = contentProvider.parametersWithCaseUrl(caseData);

        notificationService
            .sendEmail(AMENDED_APPLICATION_RETURNED_ADMIN_TEMPLATE, recipient, notifyData, caseData.getId());
    }

    @Async
    @EventListener
    public void notifyCafcass(AmendedReturnedCaseEvent event) {
        final CaseData caseData = event.getCaseData();

        final String recipient = cafcassLookupConfiguration.getCafcass(caseData.getCaseLocalAuthority()).getEmail();
        final NotifyData notifyData = contentProvider.parametersWithApplicationLink(caseData);

        notificationService
            .sendEmail(AMENDED_APPLICATION_RETURNED_CAFCASS_TEMPLATE, recipient, notifyData, caseData.getId());
    }
}
