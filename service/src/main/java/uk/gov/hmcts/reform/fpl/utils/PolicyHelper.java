package uk.gov.hmcts.reform.fpl.utils;

import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.fpl.enums.CaseRole;
import uk.gov.hmcts.reform.fpl.model.PolicyDatas;

import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;

public class PolicyHelper {

    public static boolean isPolicyMatchingCaseRoles(OrganisationPolicy organisationPolicy,
                                                    Collection<CaseRole> caseRoles) {
        return organisationPolicy != null && caseRoles != null
               && caseRoles.contains(CaseRole.from(organisationPolicy.getOrgPolicyCaseAssignedRole()));
    }

    public static void processFieldByPolicyDatas(PolicyDatas policyDatas, String fieldBaseName, Set<CaseRole> caseRoles,
                                                 Consumer<String> processFunction) {
        if (policyDatas != null) {
            policyDatas.getPolicyNoByCaseRoles(caseRoles).stream().forEach(policyNo ->
                processFunction.accept(fieldBaseName + policyNo));
        }
    }


}
