package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.hmcts.reform.fpl.events.ReturnedCaseEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.service.ReturnApplicationService;

@Api
@RestController
@RequestMapping("/callback/return-application")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ReturnApplicationController extends CallbackController {
    public static final String RETURN_APPLICATION = "returnApplication";
    private final ObjectMapper mapper;
    private final ReturnApplicationService returnApplicationService;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        caseDetails.getData().put(RETURN_APPLICATION, null);

        return respond(caseDetails);
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        DocumentReference documentReference = mapper.convertValue(caseDetails.getData().get("submittedForm"),
            DocumentReference.class);

        String updatedFileName = returnApplicationService.appendReturnedToFileName(documentReference.getFilename());
        documentReference.setFilename(updatedFileName);

        caseDetails.getData().put(RETURN_APPLICATION, returnApplicationService.updateReturnApplication(
            caseData.getReturnApplication(), documentReference, caseData.getDateSubmitted()));

        caseDetails.getData().put("submittedForm", null);

        return respond(caseDetails);
    }

    @PostMapping("/submitted")
    public void handleSubmittedEvent(@RequestBody CallbackRequest callbackRequest) {
        publishEvent(new ReturnedCaseEvent(getCaseData(callbackRequest)));
    }
}
