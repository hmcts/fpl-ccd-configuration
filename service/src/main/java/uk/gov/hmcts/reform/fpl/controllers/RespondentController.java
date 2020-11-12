package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableList;
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
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.Party;
import uk.gov.hmcts.reform.fpl.service.ConfidentialDetailsService;
import uk.gov.hmcts.reform.fpl.service.RespondentService;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.util.List;
import java.util.Objects;

import static uk.gov.hmcts.reform.fpl.enums.ConfidentialPartyType.RESPONDENT;
import static uk.gov.hmcts.reform.fpl.model.Respondent.expandCollection;

@Api
@RestController
@RequestMapping("/callback/enter-respondents")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RespondentController extends CallbackController {
    private final ConfidentialDetailsService confidentialDetailsService;
    private final RespondentService respondentService;
    private final Time time;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        caseDetails.getData().put("respondents1", confidentialDetailsService.prepareCollection(
            caseData.getAllRespondents(), caseData.getConfidentialRespondents(), expandCollection()));

        return respond(caseDetails);
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();

        return respond(caseDetails, validate(caseDetails));
    }

    @PostMapping("persist-representatives/mid-event")
    public AboutToStartOrSubmitCallbackResponse persistRepresentatives(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        CaseData caseDataBefore = getCaseDataBefore(callbackrequest);

        caseDetails.getData().put(
            "respondents1", respondentService.persistRepresentativesRelationship(
                caseData.getAllRespondents(), caseDataBefore.getAllRespondents()
            )
        );

        return respond(caseDetails);
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        confidentialDetailsService.addConfidentialDetailsToCase(caseDetails, caseData.getAllRespondents(), RESPONDENT);

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
