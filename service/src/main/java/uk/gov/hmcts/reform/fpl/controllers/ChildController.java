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
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.ConfidentialDetailsService;

import static uk.gov.hmcts.reform.fpl.enums.ConfidentialPartyType.CHILD;
import static uk.gov.hmcts.reform.fpl.model.Child.expandCollection;

@Api
@RestController
@RequestMapping("/callback/enter-children")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ChildController extends CallbackController {
    private final ConfidentialDetailsService confidentialDetailsService;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        caseDetails.getData().put("children1", confidentialDetailsService
            .prepareCollection(caseData.getAllChildren(), caseData.getConfidentialChildren(), expandCollection()));

        return respond(caseDetails);
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        confidentialDetailsService.addConfidentialDetailsToCase(caseDetails, caseData.getAllChildren(), CHILD);

        return respond(caseDetails);
    }
}
