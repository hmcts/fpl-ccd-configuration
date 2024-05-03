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
import uk.gov.hmcts.reform.fpl.events.AfterSubmissionCaseDataUpdated;
import uk.gov.hmcts.reform.fpl.events.CaseDataChanged;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.EmailAddress;
import uk.gov.hmcts.reform.fpl.service.ApplicantService;
import uk.gov.hmcts.reform.fpl.service.OrganisationService;
import uk.gov.hmcts.reform.fpl.service.PbaNumberService;
import uk.gov.hmcts.reform.fpl.service.ValidateEmailService;
import uk.gov.hmcts.reform.rd.model.Organisation;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/callback/enter-applicant")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Deprecated
public class ApplicantController extends CallbackController {
    private static final String APPLICANTS_PROPERTY = "applicants";
    private final ApplicantService applicantService;
    private final PbaNumberService pbaNumberService;
    private final OrganisationService organisationService;
    private final ValidateEmailService validateEmailService;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(
        @RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        Organisation organisation = getOrganisation(caseData).orElse(Organisation.builder().build());

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

        List<String> applicantEmails = getApplicantEmails(caseData.getApplicants());
        List<String> errors = validateEmailService.validate(applicantEmails, "Applicant");

        String solicitorEmail = caseData.getSolicitor().getEmail();
        validateEmailService.validate(solicitorEmail,
            "Solicitor: Enter an email address in the correct format,"
                + " for example name@example.com").ifPresent(errors::add);

        List<String> pbaErrors = pbaNumberService.validate(updatedApplicants);

        errors.addAll(pbaErrors);

        if (!errors.isEmpty()) {
            return respond(caseDetails, errors);
        }

        return respond(caseDetails);
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        caseDetails.getData().put(APPLICANTS_PROPERTY, applicantService.addHiddenValues(caseData));

        return respond(caseDetails);
    }

    @PostMapping("/submitted")
    public void handleSubmitted(@RequestBody CallbackRequest callbackRequest) {
        publishEvent(new CaseDataChanged(getCaseData(callbackRequest)));
        publishEvent(
            new AfterSubmissionCaseDataUpdated(getCaseData(callbackRequest), getCaseDataBefore(callbackRequest))
        );
    }

    private Optional<Organisation> getOrganisation(CaseData caseData) {
        if (caseData.isOutsourced()) {
            String organisationId = caseData.getLocalAuthorityPolicy().getOrganisation().getOrganisationID();
            return organisationService.findOrganisation(organisationId);
        }

        return organisationService.findOrganisation();
    }

    private List<String> getApplicantEmails(List<Element<Applicant>> applicants) {
        return applicants.stream()
            .map(Element::getValue)
            .map(Applicant::getParty)
            .map(ApplicantParty::getEmail)
            .map(EmailAddress::getEmail)
            .collect(Collectors.toList());
    }
}
