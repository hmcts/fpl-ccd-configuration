package uk.gov.hmcts.reform.fpl.controllers;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
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
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static org.apache.logging.log4j.util.Strings.isBlank;

@Api
@RestController
@RequestMapping("/callback/enter-applicant")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ApplicantController extends CallbackController {
    private static final String APPLICANTS_PROPERTY = "applicants";
    private final ApplicantService applicantService;
    private final PbaNumberService pbaNumberService;
    private final OrganisationService organisationService;
    private final ValidateEmailService validateEmailService;

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

        List<String> applicantEmails = caseData.getApplicants().stream()
            .map(Element::getValue)
            .map(Applicant::getParty)
            .map(ApplicantParty::getEmail)
            .map(EmailAddress::getEmail)
            .filter(StringUtils::isNotEmpty)
            .collect(Collectors.toList());

        List<String> errors = validateEmailService.validate(applicantEmails, "Applicant");

        String solicitorEmail = caseData.getSolicitor().getEmail();

        if(!isNull(solicitorEmail) && !isBlank(solicitorEmail)) {
            String error = validateEmailService.validate(solicitorEmail);
            if(!error.isBlank()) {
                errors.add(error);
            }
        }

        if (!errors.isEmpty()) {
            return respond(caseDetails, errors);
        }

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
