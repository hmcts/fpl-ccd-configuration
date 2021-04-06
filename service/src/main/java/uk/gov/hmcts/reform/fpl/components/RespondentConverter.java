package uk.gov.hmcts.reform.fpl.components;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.fpl.enums.SolicitorRole;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitorOrganisation;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class RespondentConverter {
    private final ObjectMapper mapper;

    public RespondentSolicitorOrganisation convert(Element<Respondent> respondentElement, SolicitorRole solicitorRole) {
        RespondentSolicitorOrganisation respondentCAA = mapper.convertValue(respondentElement.getValue(),
            RespondentSolicitorOrganisation.class);

        respondentCAA.setRespondentId(respondentElement.getId());

        respondentCAA.setOrganisationPolicy(setOrganisationPolicy(
            respondentElement.getValue().getSolicitor(), solicitorRole));

        return respondentCAA;
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
