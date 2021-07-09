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
import uk.gov.hmcts.reform.fpl.enums.SolicitorRole;
import uk.gov.hmcts.reform.fpl.events.NoticeOfChangeEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.WithSolicitor;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.NoticeOfChangeService;
import uk.gov.hmcts.reform.fpl.service.RespondentService;

import java.util.List;
import java.util.function.Function;

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
    @SuppressWarnings("unchecked")
    public void handleSubmittedEvent(@RequestBody CallbackRequest callbackRequest) {

        CaseData oldCaseData = getCaseDataBefore(callbackRequest);
        CaseData newCaseData = getCaseData(callbackRequest);

        // can put a stream
        List<ChangeOrganisationRequest> changeRequests = respondentService
            .getRepresentationChanges((List)newCaseData.getRespondents1(), (List) oldCaseData.getRespondents1(),
                SolicitorRole.Representing.RESPONDENT);

        changeRequests.addAll(respondentService
            .getRepresentationChanges((List)newCaseData.getAllChildren(), (List) oldCaseData.getAllChildren(),
                SolicitorRole.Representing.CHILD));

        changeRequests.forEach(
            changeRequest -> {
                SolicitorRole caseRole = changeRequest.getCaseRole();
                Function<CaseData, List<Element<WithSolicitor>>> target = caseRole.getRepresenting().getTarget();
                int solicitorIndex = caseRole.getIndex();
                publishEvent(new NoticeOfChangeEvent(
                    newCaseData,
                    target.apply(oldCaseData).get(solicitorIndex).getValue().getSolicitor(),
                    target.apply(newCaseData).get(solicitorIndex).getValue().getSolicitor())
                );
            });
    }
}
