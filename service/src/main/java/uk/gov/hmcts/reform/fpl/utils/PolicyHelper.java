package uk.gov.hmcts.reform.fpl.utils;

import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.fpl.enums.CaseRole;
import uk.gov.hmcts.reform.fpl.model.PolicyData;

import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;

public class PolicyHelper {

    private PolicyHelper() {
        // DO nothing
    }

    public static boolean isPolicyMatchingCaseRoles(OrganisationPolicy organisationPolicy,
                                                    Collection<CaseRole> caseRoles) {
        return organisationPolicy != null && caseRoles != null
            && caseRoles.contains(CaseRole.from(organisationPolicy.getOrgPolicyCaseAssignedRole()));
    }

    public static void processFieldByPolicyDatas(PolicyData policyData, String fieldBaseName, Set<CaseRole> caseRoles,
                                                 Consumer<String> processFunction) {
        if (policyData != null) {
            policyData.getPolicyNoByCaseRoles(caseRoles).stream().forEach(policyNo ->
                processFunction.accept(fieldBaseName + policyNo));
        }
    }
}
