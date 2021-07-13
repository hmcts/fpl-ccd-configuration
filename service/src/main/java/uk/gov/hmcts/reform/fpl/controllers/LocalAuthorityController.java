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
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.EmailAddress;
import uk.gov.hmcts.reform.fpl.model.event.LocalAuthorityEventData;
import uk.gov.hmcts.reform.fpl.service.ApplicantService;
import uk.gov.hmcts.reform.fpl.service.LAService;
import uk.gov.hmcts.reform.fpl.service.OrganisationService;
import uk.gov.hmcts.reform.fpl.service.PbaNumberService;
import uk.gov.hmcts.reform.fpl.service.ValidateEmailService;
import uk.gov.hmcts.reform.rd.model.Organisation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.addOrReplace;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Api
@RestController
@RequestMapping("/callback/update-local-authority")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LocalAuthorityController extends CallbackController {
    private static final String APPLICANTS_PROPERTY = "applicants";
    private final ApplicantService applicantService;
    private final LAService localAuthorityService;
    private final PbaNumberService pbaNumberService;
    private final OrganisationService organisationService;
    private final ValidateEmailService validateEmailService;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(
        @RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        LocalAuthority localAuthority = localAuthorityService.getLocalAuthority(caseData);

        caseDetails.getData().put("localAuthority", localAuthority);
        caseDetails.getData().put("localAuthorityColleagues", localAuthority.getColleagues());

        return respond(caseDetails);
    }

    @PostMapping("/organisation/mid-event")
    public AboutToStartOrSubmitCallbackResponse validateOrganisation(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        LocalAuthorityEventData eventData = caseData.getLocalAuthorityEventData();
        LocalAuthority localAuthority = pbaNumberService.update(eventData.getLocalAuthority());
        caseDetails.getData().put("localAuthority", localAuthority);

        List<String> errors = new ArrayList<>();
        errors.addAll(pbaNumberService.validate(localAuthority.getPbaNumber()));
        errors.addAll(validateEmailService.validateEmail(localAuthority.getEmail()));

        if (isNotEmpty(errors)) {
            return respond(caseDetails, errors);
        }

        return respond(caseDetails);
    }

    @PostMapping("/colleagues/mid-event")
    public AboutToStartOrSubmitCallbackResponse validateColleagues(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        LocalAuthorityEventData eventData = caseData.getLocalAuthorityEventData();
        List<String> errors = validateEmailService.validate(eventData.getColleaguesEmails(), "Colleague");

        if (isNotEmpty(errors)) {
            return respond(caseDetails, errors);
        }

        caseDetails.getData().put("localAuthorityMainContactShown", YesNo.from(eventData.getLocalAuthorityColleagues().size() > 1).getValue());
        caseDetails.getData().put("localAuthorityColleaguesList", eventData.buildLocalAuthorityColleaguesList());
        caseDetails.getData().put("localAuthorityColleagues", eventData.getLocalAuthorityColleagues());

        return respond(caseDetails);
    }

    @PostMapping("/main-contact/mid-event")
    public AboutToStartOrSubmitCallbackResponse mainContact(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        LocalAuthorityEventData eventData = caseData.getLocalAuthorityEventData();

        eventData.setMainContact(eventData.getLocalAuthorityColleaguesList().getValueCodeAsUUID());

        caseDetails.getData().put("localAuthorityColleagues", eventData.getLocalAuthorityColleagues());

        return respond(caseDetails);
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);


        LocalAuthority updatedLocalAuthority = caseData.getLocalAuthorityEventData().combined();

        if (updatedLocalAuthority.getColleagues().size() == 1) {
            updatedLocalAuthority.getColleagues().get(0).getValue().setMainContact(YesNo.YES.getValue());
        }

        Element<LocalAuthority> existingLocalAuthority = isEmpty(caseData.getLocalAuthorities()) ? element(null) : caseData.getLocalAuthorities().get(0);
        List<Element<LocalAuthority>> las = addOrReplace(caseData.getLocalAuthorities(), element(existingLocalAuthority.getId(), updatedLocalAuthority));

        caseDetails.getData().put("localAuthorities", las);
        caseDetails.getData().remove("localAuthority");
        caseDetails.getData().remove("localAuthorityColleagues");
        return respond(caseDetails);
    }

    @PostMapping("/submitted")
    public void handleSubmitted(@RequestBody CallbackRequest callbackRequest) {
//        publishEvent(new CaseDataChanged(getCaseData(callbackRequest)));
//        publishEvent(
//            new AfterSubmissionCaseDataUpdated(getCaseData(callbackRequest), getCaseDataBefore(callbackRequest))
//        );
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
