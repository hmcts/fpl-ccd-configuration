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
import uk.gov.hmcts.reform.fpl.events.PartyAddedToCaseEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.OthersService;
import uk.gov.hmcts.reform.fpl.service.RepresentativeService;
import uk.gov.hmcts.reform.fpl.service.RespondentService;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.enums.ConfidentialPartyType.OTHER;
import static uk.gov.hmcts.reform.fpl.enums.ConfidentialPartyType.RESPONDENT;

@RestController
@RequestMapping("/callback/manage-representatives")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RepresentativesController extends CallbackController {
    private final RepresentativeService representativeService;
    private final RespondentService respondentService;
    private final OthersService othersService;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        caseDetails.getData().put("representatives", representativeService.getDefaultRepresentatives(caseData));
        caseDetails.getData().put("respondents_label", getRespondentsLabel(caseData));
        caseDetails.getData().put("others_label", getOthersLabel(caseData));

        return respond(caseDetails);
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        final List<String> validationErrors = representativeService.validateRepresentatives(caseData);

        return respond(caseDetails, validationErrors);
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleSubmitted(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails updatedCaseDetails = callbackRequest.getCaseDetails();
        CaseData updatedCaseData = getCaseData(updatedCaseDetails);
        CaseData originalCaseData = getCaseDataBefore(callbackRequest);

        representativeService.updateRepresentatives(updatedCaseDetails.getId(), updatedCaseData, originalCaseData);

        updatedCaseDetails.getData().put("representatives", updatedCaseData.getRepresentatives());
        updatedCaseDetails.getData().put(OTHER.getCaseDataKey(), updatedCaseData.getOthersV2());
        updatedCaseDetails.getData().put(RESPONDENT.getCaseDataKey(), updatedCaseData.getRespondents1());

        return respond(updatedCaseDetails);
    }

    @PostMapping("/submitted")
    public void handleSubmittedEvent(@RequestBody CallbackRequest callbackRequest) {
        publishEvent(new PartyAddedToCaseEvent(getCaseData(callbackRequest), getCaseDataBefore(callbackRequest)));
        publishEvent(new AfterSubmissionCaseDataUpdated(getCaseData(callbackRequest),
            getCaseDataBefore(callbackRequest)));
    }

    private String getRespondentsLabel(CaseData caseData) {
        return respondentService.buildRespondentLabel(defaultIfNull(caseData.getRespondents1(), emptyList()));
    }

    private String getOthersLabel(CaseData caseData) {
        return othersService.buildOthersLabel(defaultIfNull(caseData.getOthersV2(), List.of()));
    }
}
