package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableList;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.Party;
import uk.gov.hmcts.reform.fpl.service.ConfidentialDetailsService;
import uk.gov.hmcts.reform.fpl.service.RespondentService;
import uk.gov.hmcts.reform.fpl.service.ValidateEmailService;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.fpl.enums.ConfidentialPartyType.RESPONDENT;
import static uk.gov.hmcts.reform.fpl.model.Respondent.expandCollection;

@Slf4j
@Api
@RestController
@RequestMapping("/callback/enter-respondents")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RespondentController extends CallbackController {
    private static final String RESPONDENTS_KEY = "respondents1";
    private final ConfidentialDetailsService confidentialDetailsService;
    private final RespondentService respondentService;
    private final Time time;
    private final ValidateEmailService validateEmailService;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        caseDetails.getData().put(RESPONDENTS_KEY, confidentialDetailsService.prepareCollection(
            caseData.getAllRespondents(), caseData.getConfidentialRespondents(), expandCollection()));

        return respond(caseDetails);
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        List<Respondent> respondentsWithLegalRep = respondentService.getRespondentsWithLegalRepresentation(caseData
            .getRespondents1());
        List<String> emails = respondentService.getRespondentSolicitorEmails(respondentsWithLegalRep);

        List<String> emailErrors = validateEmailService.validate(emails, "Representative");
        List<String> futureDOBErrors = validate(caseDetails);
        List<String> combinedValidationErrors = Stream.concat(emailErrors.stream(), futureDOBErrors.stream())
            .collect(Collectors.toList());

        caseDetails.getData().put("respondents1", respondentService.removeHiddenFields(caseData.getRespondents1()));
        return respond(caseDetails, combinedValidationErrors);
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        CaseData caseDataBefore = getCaseDataBefore(callbackRequest);

        confidentialDetailsService.addConfidentialDetailsToCase(caseDetails, caseData.getAllRespondents(), RESPONDENT);

        caseData = getCaseData(caseDetails);

        // can either do before or after but have to update case details manually either way as if there is no
        // confidential info then caseDetails won't be updated in the confidential details method and as such just
        // passing the updated list to the method won't work
        caseDetails.getData().put(RESPONDENTS_KEY, respondentService.persistRepresentativesRelationship(
            caseData.getAllRespondents(), caseDataBefore.getAllRespondents()
        ));

        caseDetails.getData().put("respondents1", respondentService.removeHiddenFields(caseData.getRespondents1()));
        return respond(caseDetails);
    }

    private List<String> validate(CaseDetails caseDetails) {
        ImmutableList.Builder<String> errors = ImmutableList.builder();
        CaseData caseData = getCaseData(caseDetails);

        caseData.getAllRespondents().stream()
            .map(Element::getValue)
            .map(Respondent::getParty)
            .map(Party::getDateOfBirth)
            .filter(Objects::nonNull)
            .filter(dob -> dob.isAfter(time.now().toLocalDate()))
            .findAny()
            .ifPresent(date -> errors.add("Date of birth cannot be in the future"));

        return errors.build();
    }
}
