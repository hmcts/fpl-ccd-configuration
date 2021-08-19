package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.aac.client.CaseAssignmentApi;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.request.RequestData;

import static uk.gov.hmcts.reform.aac.model.DecisionRequest.decisionRequest;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseAssignmentService {

    private final RequestData requestData;
    private final AuthTokenGenerator tokenGenerator;
    private final SystemUserService systemUserService;
    private final CaseAssignmentApi caseAssignmentApi;

    public AboutToStartOrSubmitCallbackResponse applyDecisionAsSystemUser(CaseDetails caseDetails) {
        return applyDecision(caseDetails, systemUserService.getSysUserToken());
    }

    public AboutToStartOrSubmitCallbackResponse applyDecision(CaseDetails caseDetails) {
        return applyDecision(caseDetails, requestData.authorisation());
    }

    private AboutToStartOrSubmitCallbackResponse applyDecision(CaseDetails caseDetails, String userToken) {
        return caseAssignmentApi.applyDecision(
            userToken,
            tokenGenerator.generate(),
            decisionRequest(caseDetails));
    }
}
