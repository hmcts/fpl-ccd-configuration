package uk.gov.hmcts.reform.fpl.components;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.fpl.enums.SolicitorRole;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@Component
public class RespondentPolicyConverter {
    public OrganisationPolicy generateForSubmission(SolicitorRole solicitorRole) {
        return OrganisationPolicy.builder()
            .organisation(Organisation.builder().build())
            .orgPolicyCaseAssignedRole(solicitorRole.getCaseRoleLabel())
            .build();
    }

    public OrganisationPolicy generateForSubmission(SolicitorRole solicitorRole,
                                                    Element<Respondent> respondentElement) {
        OrganisationPolicy organisationPolicy = generateForSubmission(solicitorRole);
        RespondentSolicitor respondentSolicitor = respondentElement.getValue().getSolicitor();

        if (hasOrganisation(respondentSolicitor)) {
            organisationPolicy.setOrganisation(respondentSolicitor.getOrganisation());
        }

        return organisationPolicy;
    }

    private boolean hasOrganisation(RespondentSolicitor respondentSolicitor) {
        return isNotEmpty(respondentSolicitor) && isNotEmpty(respondentSolicitor.getOrganisation());
    }
}
