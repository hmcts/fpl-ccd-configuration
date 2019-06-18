package uk.gov.hmcts.reform.fpl.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.events.DeletedCaseEvent;
import uk.gov.hmcts.reform.fpl.events.InitiatedCaseEvent;
import uk.gov.hmcts.reform.fpl.service.CaseService;
import uk.gov.hmcts.reform.fpl.service.LocalAuthorityUserService;

@Component
public class CaseAccessHandler {

    private final LocalAuthorityUserService localAuthorityUserService;
    private final CaseService caseService;

    @Autowired
    public CaseAccessHandler(LocalAuthorityUserService localAuthorityUserService, CaseService caseService) {
        this.localAuthorityUserService = localAuthorityUserService;
        this.caseService = caseService;
    }

    @Async
    @EventListener
    public void grantCaseAccessToAllLocalAuthorityUsers(InitiatedCaseEvent event) {
        String userId = event.getUserId();
        String authorization = event.getAuthorization();
        String caseId = Long.toString(event.getCallbackRequest().getCaseDetails().getId());
        String caseLocalAuthority = (String) event.getCallbackRequest().getCaseDetails().getData()
            .get("caseLocalAuthority");

        localAuthorityUserService.grantUserAccess(authorization, userId, caseId, caseLocalAuthority);
    }

    @Async
    @EventListener
    public void deleteCase(DeletedCaseEvent event) {
        String userId = event.getUserId();
        String authorization = event.getAuthorization();
        String caseId = Long.toString(event.getCallbackRequest().getCaseDetails().getId());

        // get all the eventIds for a given case
        // for each event id delete it
        caseService.deleteCase(authorization, userId, caseId);
    }
}
