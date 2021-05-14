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
import uk.gov.hmcts.reform.fpl.events.AfterSubmissionCaseDataUpdated;
import uk.gov.hmcts.reform.fpl.events.RespondentsUpdated;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.ConfidentialDetailsService;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.NoticeOfChangeService;
import uk.gov.hmcts.reform.fpl.service.RespondentAfterSubmissionRepresentationService;
import uk.gov.hmcts.reform.fpl.service.RespondentService;
import uk.gov.hmcts.reform.fpl.service.respondent.RespondentValidator;

import java.util.List;

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
    private final FeatureToggleService featureToggleService;
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
        List<Element<Respondent>> respondents = respondentService.persistRepresentativesRelationship(
            caseData.getAllRespondents(), caseDataBefore.getAllRespondents());

        caseDetails.getData().put(RESPONDENTS_KEY, respondentService.removeHiddenFields(respondents));
        if (!OPEN.equals(caseData.getState())) {
            caseDetails.getData().putAll(
                respondentAfterSubmissionRepresentationService.updateRepresentation(caseData, caseDataBefore));
        }
        return respond(caseDetails);
    }

    @PostMapping("/submitted")
    public void handleSubmitted(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        CaseData caseDataBefore = getCaseDataBefore(callbackRequest);

        if (!OPEN.equals(caseData.getState())) {
            if (featureToggleService.isNoticeOfChangeEnabled()) {
                noticeOfChangeService.updateRepresentativesAccess(caseData, caseDataBefore);
            }
            publishEvent(new RespondentsUpdated(caseData, caseDataBefore));
            publishEvent(new AfterSubmissionCaseDataUpdated(caseData, caseDataBefore));
        }
    }

}
