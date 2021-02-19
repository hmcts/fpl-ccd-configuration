package uk.gov.hmcts.reform.fpl.controllers;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.events.NotifyGatekeepersEvent;
import uk.gov.hmcts.reform.fpl.events.OutsourcedCaseSentToGatekeepingEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.StandardDirectionsService;

@Api
@RestController
@RequestMapping("/callback/send-to-gatekeeper")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SendToGatekeeperController extends CallbackController {
    private final StandardDirectionsService standardDirectionsService;

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmitEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        caseDetails.getData().putAll(standardDirectionsService.populateStandardDirections(caseData));

        return respond(caseDetails);
    }

    @PostMapping("/submitted")
    public void handleSubmittedEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseData caseData = getCaseData(callbackRequest);

        if (isCaseOutsourced(caseData)) {
            publishEvent(new OutsourcedCaseSentToGatekeepingEvent(caseData));
        }

        publishEvent(new NotifyGatekeepersEvent(caseData));
    }

    private boolean isCaseOutsourced(CaseData caseData) {
        return (caseData.getOutsourcingPolicy() != null);
    }
}
