package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.events.AmendedReturnedCaseEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.service.CourtService;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.ReturnedCaseContentProvider;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.AMENDED_APPLICATION_RETURNED_ADMIN_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.AMENDED_APPLICATION_RETURNED_CAFCASS_TEMPLATE;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AmendedReturnedCaseEventHandler {

    private final CourtService courtService;
    private final NotificationService notificationService;
    private final ReturnedCaseContentProvider contentProvider;
    private final CafcassLookupConfiguration cafcassLookupConfiguration;
    private final FeatureToggleService featureToggleService;

    @Async
    @EventListener
    public void notifyAdmin(AmendedReturnedCaseEvent event) {
        if (featureToggleService.isWATaskEmailsEnabled()) {
            final CaseData caseData = event.getCaseData();

            final String recipient = courtService.getCourtEmail(caseData);
            final NotifyData notifyData = contentProvider.parametersWithCaseUrl(caseData);

            notificationService
                .sendEmail(AMENDED_APPLICATION_RETURNED_ADMIN_TEMPLATE, recipient, notifyData, caseData.getId());
        } else {
            log.info("WA EMAIL SKIPPED - application resubmitted - {}", event.getCaseData().getId());
        }
    }

    @Async
    @EventListener
    public void notifyCafcass(AmendedReturnedCaseEvent event) {
        final CaseData caseData = event.getCaseData();

        final String recipient = cafcassLookupConfiguration.getCafcass(caseData.getCaseLaOrRelatingLa()).getEmail();
        final NotifyData notifyData = contentProvider.parametersWithApplicationLink(caseData);

        notificationService
            .sendEmail(AMENDED_APPLICATION_RETURNED_CAFCASS_TEMPLATE, recipient, notifyData, caseData.getId());
    }
}
