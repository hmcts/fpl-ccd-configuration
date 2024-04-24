package uk.gov.hmcts.reform.fpl.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.LegalCounsellor;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.EventService;
import uk.gov.hmcts.reform.fpl.service.legalcounsel.ManageLegalCounselService;

import java.util.List;

@RestController
@RequestMapping("/callback/manage-legal-counsel")
@RequiredArgsConstructor
public class ManageLegalCounselController extends CallbackController {

    private final ManageLegalCounselService manageLegalCounselService;
    private final EventService eventPublisher;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        List<Element<LegalCounsellor>> legalCounsellors =
            manageLegalCounselService.retrieveLegalCounselForLoggedInSolicitor(getCaseData(caseDetails));
        caseDetails.getData().put("legalCounsellors", legalCounsellors);

        return respond(caseDetails);
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        List<String> errorMessages = manageLegalCounselService.validateEventData(getCaseData(caseDetails));

        return respond(caseDetails, errorMessages);
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        manageLegalCounselService.updateLegalCounsel(caseDetails);

        return respond(caseDetails);
    }

    @PostMapping("/submitted")
    public void handleSubmitted(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails previousCaseDetails = callbackRequest.getCaseDetailsBefore();
        CaseDetails currentCaseDetails = callbackRequest.getCaseDetails();
        CaseData previousCaseData = getCaseData(previousCaseDetails);
        CaseData currentCaseData = getCaseData(currentCaseDetails);

        manageLegalCounselService.runFinalEventActions(previousCaseData, currentCaseData)
            .forEach(eventPublisher::publishEvent);
    }

}
