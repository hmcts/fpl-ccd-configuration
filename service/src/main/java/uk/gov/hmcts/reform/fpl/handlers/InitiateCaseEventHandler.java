package uk.gov.hmcts.reform.fpl.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.fpl.events.InitiateCaseEvent;
import uk.gov.hmcts.reform.fpl.service.CaseRepository;
import uk.gov.hmcts.reform.fpl.service.UserService;

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
     *
     */
    @Async
    @EventListener
    public void handleCaseInitiation(InitiateCaseEvent event) {
        String userId = event.getUserId();
        String authorization = event.getAuthorization();
        CallbackRequest callbackRequest = event.getCallbackRequest();

        System.out.println("authorization = " + authorization);
        System.out.println("userId = " + userId);

        String caseLocalAuthority = userService.getUserDetails(authorization);

        System.out.println("LA = " + caseLocalAuthority);

        caseRepository.setCaseLocalAuthority(authorization, userId, callbackRequest,caseLocalAuthority);
    }
}
