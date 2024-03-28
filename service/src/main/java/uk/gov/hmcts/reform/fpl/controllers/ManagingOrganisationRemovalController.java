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
import uk.gov.hmcts.reform.fpl.events.ManagingOrganisationRemoved;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.CaseAssignmentService;
import uk.gov.hmcts.reform.fpl.service.ManagingOrganisationService;
import uk.gov.hmcts.reform.rd.model.Organisation;

@RestController
@RequestMapping("/callback/remove-managing-organisation")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ManagingOrganisationRemovalController extends CallbackController {

    private final CaseAssignmentService caseAssignmentService;
    private final ManagingOrganisationService organisationService;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStartEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        Organisation managingOrganisation = organisationService.getManagingOrganisation(caseData);

        caseDetails.getData().put("managingOrganisationName", managingOrganisation.getName());

        return respond(caseDetails);
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        caseDetails.getData().remove("managingOrganisationName");
        caseDetails.getData().put("changeOrganisationRequestField", organisationService.getRemovalRequest(caseData));

        return caseAssignmentService.applyDecisionAsSystemUser(caseDetails);
    }

    @PostMapping("/submitted")
    public void handleSubmitted(@RequestBody CallbackRequest callbackRequest) {
        CaseData caseData = getCaseData(callbackRequest);
        CaseData caseDataBefore = getCaseDataBefore(callbackRequest);

        Organisation managingOrganisation = organisationService.getManagingOrganisation(caseDataBefore);

        publishEvent(new ManagingOrganisationRemoved(caseData, managingOrganisation));
    }

}
