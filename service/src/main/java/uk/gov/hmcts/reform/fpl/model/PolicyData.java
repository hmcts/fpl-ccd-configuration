package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.fpl.enums.CaseRole;
import uk.gov.hmcts.reform.fpl.utils.PolicyHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public interface PolicyData {
    @JsonIgnore
    OrganisationPolicy[] getAllPolicies();

    @JsonIgnore
    default List<Integer> getPolicyNoByCaseRoles(Collection<CaseRole> caseRoles) {
        OrganisationPolicy[] allPolicy = getAllPolicies();

        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < allPolicy.length; i++) {
            if (PolicyHelper.isPolicyMatchingCaseRoles(allPolicy[i], caseRoles)) {
                result.add(i);
            }
        }

        return result;
    }
}
