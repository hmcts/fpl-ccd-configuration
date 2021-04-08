package uk.gov.hmcts.reform.fpl.components;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.fpl.enums.SolicitorRole;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@Component
public class RespondentPolicyConverter {
    public OrganisationPolicy convert(Element<Respondent> respondentElement, SolicitorRole solicitorRole) {
        return setOrganisationPolicy(respondentElement.getValue().getSolicitor(), solicitorRole);
    }

    private OrganisationPolicy setOrganisationPolicy(RespondentSolicitor respondentSolicitor,
                                                     SolicitorRole solicitorRole) {
        OrganisationPolicy.OrganisationPolicyBuilder organisationPolicy = OrganisationPolicy.builder();

        if (hasOrganisation(respondentSolicitor)) {
            organisationPolicy.organisation(respondentSolicitor.getOrganisation());
        }

        organisationPolicy.orgPolicyCaseAssignedRole(solicitorRole.getCaseRoleLabel());
        return organisationPolicy.build();
    }

    private boolean hasOrganisation(RespondentSolicitor respondentSolicitor) {
        return isNotEmpty(respondentSolicitor) && isNotEmpty(respondentSolicitor.getOrganisation());
    }
}
