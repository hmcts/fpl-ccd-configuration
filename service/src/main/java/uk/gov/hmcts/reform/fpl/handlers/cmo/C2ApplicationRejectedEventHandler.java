package uk.gov.hmcts.reform.fpl.handlers.cmo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.events.cmo.C2ApplicationRejectedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.C2ApplicationRejectedTemplate;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.cmo.C2ApplicationRejectedContentProvider;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.C2_APPLICATION_REFUSED_TEMPLATE;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class C2ApplicationRejectedEventHandler {
    private final NotificationService notificationService;
    private final C2ApplicationRejectedContentProvider c2ApplicationRejectedContentProvider;

    @Async
    @EventListener
    public void sendNotificationsToC2Applicant(C2ApplicationRejectedEvent event) {

        String recipient = event.getSelectedAdditionalApplicationBundle().getAuthor();
        // TBC The notification should not be sent to HMCTS if the application is uploaded by internal staff,
        // as they should have WA enabled
        if (!"HMCTS".equals(recipient)) {
            CaseData caseData = event.getCaseData();

            C2ApplicationRejectedTemplate notifyData =
                c2ApplicationRejectedContentProvider.buildContent(caseData, event.getRefusalOrderTitle());

            notificationService.sendEmail(C2_APPLICATION_REFUSED_TEMPLATE, recipient, notifyData, caseData.getId());
        }

    }
}
