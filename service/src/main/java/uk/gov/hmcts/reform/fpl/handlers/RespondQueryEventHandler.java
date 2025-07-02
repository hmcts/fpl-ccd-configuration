package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.events.RespondQueryEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.UserService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.RespondQueryContentProvider;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.QUERY_RESPONDED;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class RespondQueryEventHandler {
    private final NotificationService notificationService;
    private final RespondQueryContentProvider respondQueryContentProvider;
    private final UserService userService;

    @EventListener
    @Async
    public void notifyUser(final RespondQueryEvent event) {
        CaseData caseData = event.getCaseData();
        UserDetails userDetails = userService.getUserDetailsById(event.getUserId());
        String recipient = userDetails.getEmail();
        String queryDate = event.getQueryDate();

        log.info("TESING: about to send QM response notification email.");

        log.info("RECIPIENT is {}", recipient);
        log.info("QUERYDATE is {}", queryDate);

        notificationService.sendEmail(QUERY_RESPONDED, recipient,
            respondQueryContentProvider.getRespondQueryNotifyData(caseData, queryDate), caseData.getId());

        log.info("TESING: Successfully sent QM response notification email.");
    }
}
