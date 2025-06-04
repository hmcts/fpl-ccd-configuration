package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.aac.client.NocApi;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.model.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.fpl.enums.CaseRole;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.util.List;

import static uk.gov.hmcts.reform.aac.model.DecisionRequest.decisionRequest;
import static uk.gov.hmcts.reform.ccd.model.ChangeOrganisationApprovalStatus.APPROVED;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseAssignmentService {

    private final Time time;
    private final RequestData requestData;
    private final AuthTokenGenerator tokenGenerator;
    private final SystemUserService systemUserService;
    private final NocApi nocApi;

    public AboutToStartOrSubmitCallbackResponse applyDecisionAsSystemUser(CaseDetails caseDetails) {
        return applyDecision(caseDetails, systemUserService.getSysUserToken());
    }

    public AboutToStartOrSubmitCallbackResponse replaceAsSystemUser(CaseDetails caseDetails,
                                                                    CaseRole caseRole,
                                                                    Organisation toAdd,
                                                                    Organisation toRemove) {

        caseDetails.getData()
            .put("changeOrganisationRequestField", changeRequest(caseRole.formattedName(), toAdd, toRemove));

        return applyDecisionAsSystemUser(caseDetails);
    }

    public AboutToStartOrSubmitCallbackResponse applyDecision(CaseDetails caseDetails) {
        return applyDecision(caseDetails, requestData.authorisation());
    }

    private AboutToStartOrSubmitCallbackResponse applyDecision(CaseDetails caseDetails, String userToken) {
        return nocApi.applyDecision(
            userToken,
            tokenGenerator.generate(),
            decisionRequest(caseDetails));
    }

    private ChangeOrganisationRequest changeRequest(String role, Organisation add, Organisation remove) {

        final DynamicListElement roleItem = DynamicListElement.builder()
            .code(role)
            .label(role)
            .build();

        return ChangeOrganisationRequest.builder()
            .approvalStatus(APPROVED)
            .requestTimestamp(time.now())
            .caseRoleId(DynamicList.builder()
                .value(roleItem)
                .listItems(List.of(roleItem))
                .build())
            .organisationToRemove(remove)
            .organisationToAdd(add)
            .build();
    }

}
