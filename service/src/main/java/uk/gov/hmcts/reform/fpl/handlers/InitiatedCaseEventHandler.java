package uk.gov.hmcts.reform.fpl.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.events.InitiatedCaseEvent;
import uk.gov.hmcts.reform.fpl.service.CaseRepository;
import uk.gov.hmcts.reform.fpl.service.UserService;


/**
 * Handles the case initiation event.
 */
@Component
public class InitiatedCaseEventHandler {

    private final UserService userService;
    private final CaseRepository caseRepository;

    @Autowired
    public InitiatedCaseEventHandler(UserService userService,
                                     CaseRepository caseRepository) {
        this.userService = userService;
        this.caseRepository = caseRepository;
    }

    /**
     * Grabs the local authority from the domain of an email address and uploads this into CCD case data.
     *
     * @param event initiate case event.
     */
    @Async
    @EventListener
    public void handleCaseInitiation(InitiatedCaseEvent event) {
        String userId = event.getUserId();
        String authorization = event.getAuthorization();
        String caseId = event.getCallbackRequest().getCaseDetails().getId().toString();

        String caseLocalAuthority = userService.extractUserDomainName(authorization);

        caseRepository.setCaseLocalAuthority(authorization, userId, caseId, caseLocalAuthority);
    }
}
