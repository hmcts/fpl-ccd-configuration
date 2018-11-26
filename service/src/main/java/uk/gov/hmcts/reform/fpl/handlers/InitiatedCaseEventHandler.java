package uk.gov.hmcts.reform.fpl.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.events.InitiatedCaseEvent;
import uk.gov.hmcts.reform.fpl.service.LocalAuthorityUserService;

@Component
public class InitiatedCaseEventHandler {

    private final LocalAuthorityUserService localAuthorityUserService;

    @Autowired
    public InitiatedCaseEventHandler(LocalAuthorityUserService localAuthorityUserService) {
        this.localAuthorityUserService = localAuthorityUserService;
    }

    @Async
    @EventListener
    public void handleCaseInitiation(InitiatedCaseEvent event) {
        String userId = event.getUserId();
        String authorization = event.getAuthorization();
        String caseId = event.getCallbackRequest().getCaseDetails().getId().toString();
        String caseLocalAuthority = event.getCallbackRequest().getCaseDetails().getData()
            .get("caseLocalAuthority").toString();

        localAuthorityUserService.grantUserAccess(authorization, userId, caseId, caseLocalAuthority);
    }
}
