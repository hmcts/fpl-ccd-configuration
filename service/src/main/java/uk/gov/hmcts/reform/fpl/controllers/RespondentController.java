package uk.gov.hmcts.reform.fpl.controllers;

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
import uk.gov.hmcts.reform.fpl.enums.SolicitorRole;
import uk.gov.hmcts.reform.fpl.events.AfterSubmissionCaseDataUpdated;
import uk.gov.hmcts.reform.fpl.events.RespondentsUpdated;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.WithSolicitor;
import uk.gov.hmcts.reform.fpl.service.ConfidentialDetailsService;
import uk.gov.hmcts.reform.fpl.service.NoticeOfChangeService;
import uk.gov.hmcts.reform.fpl.service.RespondentAfterSubmissionRepresentationService;
import uk.gov.hmcts.reform.fpl.service.RespondentService;
import uk.gov.hmcts.reform.fpl.service.respondent.RespondentValidator;

import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static uk.gov.hmcts.reform.fpl.enums.ConfidentialPartyType.RESPONDENT;
import static uk.gov.hmcts.reform.fpl.enums.State.OPEN;
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
    private final RespondentAfterSubmissionRepresentationService respondentAfterSubmissionRepresentationService;
    private final RespondentValidator respondentValidator;
    private final NoticeOfChangeService noticeOfChangeService;

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
        CaseData caseDataBefore = getCaseDataBefore(callbackRequest);

        caseDetails.getData().put(RESPONDENTS_KEY, respondentService.removeHiddenFields(caseData.getRespondents1()));

        List<String> errors = respondentValidator.validate(caseData, caseDataBefore);
        return respond(caseDetails, errors);
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
        List<Element<Respondent>> oldRespondents = caseDataBefore.getAllRespondents();
        List<Element<Respondent>> newRespondents = respondentService.persistRepresentativesRelationship(
            caseData.getAllRespondents(), oldRespondents);

        newRespondents = respondentService.removeHiddenFields(newRespondents);

//        /////TODO - write unit test
        //TODO - extract to service?
        for (int i = 0; i < oldRespondents.size(); i++) {
            final int index = i;
            Optional<RespondentSolicitor> oldRespondentSolicitor = Optional.ofNullable(oldRespondents)
                .map(respondents -> respondents.get(index))
                .map(Element::getValue)
                .map(WithSolicitor::getSolicitor);

            Optional<WithSolicitor> respondentInNewList = Optional.ofNullable(newRespondents)
                .map(respondents -> respondents.get(index))
                .map(Element::getValue);
            Optional<RespondentSolicitor> newRespondentSolicitor = respondentInNewList
                .map(WithSolicitor::getSolicitor);

            if (!oldRespondentSolicitor.equals(newRespondentSolicitor)) {
                respondentInNewList.ifPresent(respondent -> respondent.setLegalCounsellors(emptyList()));
            }
        }
        /////
        //Get removed solicitors
        //Can I use the existing function? - I think I can make something generic
        //Remove their legal counsel as well

        caseDetails.getData().put(RESPONDENTS_KEY, newRespondents);
        if (!OPEN.equals(caseData.getState())) {
            caseDetails.getData().putAll(respondentAfterSubmissionRepresentationService.updateRepresentation(
                caseData, caseDataBefore, SolicitorRole.Representing.RESPONDENT, true
            ));
        }

        return respond(caseDetails);
    }

    @PostMapping("/submitted")
    public void handleSubmitted(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        CaseData caseDataBefore = getCaseDataBefore(callbackRequest);

        if (!OPEN.equals(caseData.getState())) {
            noticeOfChangeService.updateRepresentativesAccess(caseData, caseDataBefore,
                SolicitorRole.Representing.RESPONDENT);
            publishEvent(new RespondentsUpdated(caseData, caseDataBefore));
            publishEvent(new AfterSubmissionCaseDataUpdated(caseData, caseDataBefore));
        }
    }

}
