package uk.gov.hmcts.reform.ccd.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.CaseRole;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrganisationPolicy {

    @JsonProperty("Organisation")
    private Organisation organisation;

    @JsonProperty("OrgPolicyReference")
    private String orgPolicyReference;

    @JsonProperty("OrgPolicyCaseAssignedRole")
    private String orgPolicyCaseAssignedRole;

    public static OrganisationPolicy organisationPolicy(String organisationId,
                                                        String organisationName,
                                                        CaseRole caseRole) {
        if (organisationId == null) {
            return null;
        }

        return OrganisationPolicy.builder()
            .organisation(Organisation.builder()
                .organisationID(organisationId)
                .organisationName(organisationName)
                .build())
            .orgPolicyCaseAssignedRole(caseRole.formattedName())
            .build();
    }
}
