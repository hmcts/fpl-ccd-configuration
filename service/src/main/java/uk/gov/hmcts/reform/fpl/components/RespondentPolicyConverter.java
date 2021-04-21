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
    public OrganisationPolicy generateForSubmission(Element<Respondent> respondentElement,
                                                    SolicitorRole solicitorRole) {
        OrganisationPolicy.OrganisationPolicyBuilder organisationPolicyBuilder = OrganisationPolicy.builder();

        RespondentSolicitor respondentSolicitor = respondentElement.getValue().getSolicitor();

        if (hasOrganisation(respondentSolicitor)) {
            organisationPolicyBuilder.organisation(respondentSolicitor.getOrganisation());
        }

        organisationPolicyBuilder.orgPolicyCaseAssignedRole(solicitorRole.getCaseRoleLabel());
        return organisationPolicyBuilder.build();
    }

    private boolean hasOrganisation(RespondentSolicitor respondentSolicitor) {
        return isNotEmpty(respondentSolicitor) && isNotEmpty(respondentSolicitor.getOrganisation());
    }
}
