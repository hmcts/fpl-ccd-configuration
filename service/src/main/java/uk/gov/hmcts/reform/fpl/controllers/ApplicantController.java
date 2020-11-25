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
import uk.gov.hmcts.reform.fpl.service.ApplicantService;
import uk.gov.hmcts.reform.fpl.service.OrganisationService;
import uk.gov.hmcts.reform.fpl.service.PbaNumberService;
import uk.gov.hmcts.reform.rd.model.Organisation;

@Api
@RestController
@RequestMapping("/callback/enter-applicant")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ApplicantController extends CallbackController {
    private static final String APPLICANTS_PROPERTY = "applicants";
    private final ApplicantService applicantService;
    private final PbaNumberService pbaNumberService;
    private final OrganisationService organisationService;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        Organisation organisation = organisationService.findOrganisation().orElse(Organisation.builder().build());

        caseDetails.getData()
            .put(APPLICANTS_PROPERTY, applicantService.expandApplicantCollection(caseData, organisation));

        return respond(caseDetails);
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        var data = caseDetails.getData();
        CaseData caseData = getCaseData(caseDetails);

        var updatedApplicants = pbaNumberService.update(caseData.getApplicants());
        data.put(APPLICANTS_PROPERTY, updatedApplicants);

        return respond(caseDetails, pbaNumberService.validate(updatedApplicants));
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        caseDetails.getData().put(APPLICANTS_PROPERTY, applicantService.addHiddenValues(caseData));

        return respond(caseDetails);
    }
}
