package uk.gov.hmcts.reform.fpl.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.events.ReturnedCaseEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.service.ReturnApplicationService;

@RestController
@RequestMapping("/callback/return-application")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ReturnApplicationController extends CallbackController {
    public static final String RETURN_APPLICATION = "returnApplication";

    private final ReturnApplicationService returnApplicationService;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        caseDetails.getData().remove(RETURN_APPLICATION);

        return respond(caseDetails);
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        DocumentReference submittedForm = caseData.getC110A().getSubmittedForm();
        if (submittedForm != null) {
            returnApplicationService.appendReturnedToFileName(submittedForm);
        }

        caseDetails.getData().put(RETURN_APPLICATION, returnApplicationService.updateReturnApplication(
            caseData.getReturnApplication(), submittedForm, caseData.getDateSubmitted()
        ));

        caseDetails.getData().remove("submittedForm");

        return respond(caseDetails);
    }

    @PostMapping("/submitted")
    public void handleSubmittedEvent(@RequestBody CallbackRequest callbackRequest) {
        publishEvent(new ReturnedCaseEvent(getCaseData(callbackRequest)));
    }
}
