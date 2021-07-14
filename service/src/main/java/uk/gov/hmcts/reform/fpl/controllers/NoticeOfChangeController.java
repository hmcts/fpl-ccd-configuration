package uk.gov.hmcts.reform.fpl.controllers;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.aac.client.CaseAssignmentApi;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.model.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.fpl.events.NoticeOfChangeEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.NoticeOfChangeService;
import uk.gov.hmcts.reform.fpl.service.RespondentService;

import java.util.List;

import static uk.gov.hmcts.reform.aac.model.DecisionRequest.decisionRequest;

@Api
@RestController
@RequestMapping("/callback/noc-decision")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class NoticeOfChangeController extends CallbackController {

    private final RequestData requestData;
    private final AuthTokenGenerator tokenGenerator;
    private final CaseAssignmentApi caseAssignmentApi;
    private final NoticeOfChangeService noticeOfChangeService;
    private final RespondentService respondentService;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        caseDetails.getData().putAll(noticeOfChangeService.updateRepresentation(caseData));

        return caseAssignmentApi.applyDecision(requestData.authorisation(), tokenGenerator.generate(),
            decisionRequest(caseDetails));
    }

    @PostMapping("/submitted")
    public void handleSubmittedEvent(@RequestBody CallbackRequest callbackRequest) {

        CaseData oldCaseData = getCaseDataBefore(callbackRequest);
        CaseData newCaseData = getCaseData(callbackRequest);

        List<ChangeOrganisationRequest> changeRequests = respondentService
            .getRepresentationChanges(newCaseData.getRespondents1(), oldCaseData.getRespondents1());

        changeRequests.forEach(
            changeRequest -> {
                int solicitorIndex = changeRequest.getCaseRole().getIndex();
                publishEvent(new NoticeOfChangeEvent(
                    newCaseData,
                    oldCaseData.getRespondents1().get(solicitorIndex).getValue(),
                    newCaseData.getRespondents1().get(solicitorIndex).getValue())
                );
            });
    }
}
