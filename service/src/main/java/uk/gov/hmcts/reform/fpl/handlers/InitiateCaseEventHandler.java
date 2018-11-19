package uk.gov.hmcts.reform.fpl.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.events.InitiateCaseEvent;
import uk.gov.hmcts.reform.fpl.service.CaseRepository;
import uk.gov.hmcts.reform.fpl.service.UserService;


/**
 * Handles the case initiation event.
 */
@Component
public class InitiateCaseEventHandler {

    private final UserService userService;
    private final CaseRepository caseRepository;

    @Autowired
    public InitiateCaseEventHandler(UserService userService,
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
    public void handleCaseInitiation(InitiateCaseEvent event) {
        String userId = event.getUserId();
        String authorization = event.getAuthorization();
        CaseDetails caseDetails = event.getCallbackRequest().getCaseDetails();

        String caseLocalAuthority = userService.getUserDetails(authorization);

        caseRepository.setCaseLocalAuthority(authorization, userId, caseDetails, caseLocalAuthority);
    }
}
