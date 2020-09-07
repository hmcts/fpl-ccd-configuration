package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.events.AmendedReturnedCaseEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.returnedcase.ReturnedCaseTemplate;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.ReturnedCaseContentProvider;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.AMENDED_APPLICATION_RETURNED_ADMIN_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.AMENDED_APPLICATION_RETURNED_CAFCASS_TEMPLATE;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AmendedReturnedCaseEventHandler {
    private final NotificationService notificationService;
    private final ReturnedCaseContentProvider contentProvider;
    private final HmctsAdminNotificationHandler adminNotificationHandler;
    private final CafcassLookupConfiguration cafcassLookupConfiguration;

    @Async
    @EventListener
    public void notifyAdmin(AmendedReturnedCaseEvent event) {
        final CaseData caseData = event.getCaseData();
        final String email = adminNotificationHandler.getHmctsAdminEmail(caseData);

        ReturnedCaseTemplate parameters = contentProvider.parametersWithCaseUrl(caseData);

        notificationService.sendEmail(AMENDED_APPLICATION_RETURNED_ADMIN_TEMPLATE, email, parameters,
            caseData.getId().toString());
    }

    @Async
    @EventListener
    public void notifyCafcass(AmendedReturnedCaseEvent event) {
        final CaseData caseData = event.getCaseData();

        final String email = cafcassLookupConfiguration.getCafcass(caseData.getCaseLocalAuthority()).getEmail();

        ReturnedCaseTemplate parameters = contentProvider.parametersWithApplicationLink(caseData);

        notificationService.sendEmail(AMENDED_APPLICATION_RETURNED_CAFCASS_TEMPLATE, email, parameters,
            caseData.getId().toString());
    }
}
