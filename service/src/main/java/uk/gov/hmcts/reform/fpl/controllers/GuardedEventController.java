package uk.gov.hmcts.reform.fpl.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.FplEvent;
import uk.gov.hmcts.reform.fpl.controllers.guards.EventGuardProvider;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.util.List;

public class GuardedEventController {

    @Autowired
    EventGuardProvider eventGuardProvider;

    protected FplEvent event;

    public GuardedEventController(FplEvent event) {
        this.event = event;
    }


    public AboutToStartOrSubmitCallbackResponse save(CaseDetails caseDetails) {
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    public AboutToStartOrSubmitCallbackResponse save(CaseDetails caseDetails, List<String> errors) {
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .errors(errors)
            .build();
    }

}
