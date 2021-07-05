package uk.gov.hmcts.reform.fpl.components;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.fpl.enums.SolicitorRole;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.WithSolicitor;

import java.util.Optional;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@Component
public class RespondentPolicyConverter {
    public OrganisationPolicy generate(SolicitorRole solicitorRole,
                                       Optional<Element<WithSolicitor>> optionalRespondentElement) {
        return OrganisationPolicy.builder()
            .organisation(getOrganisation(optionalRespondentElement))
            .orgPolicyCaseAssignedRole(solicitorRole.getCaseRoleLabel())
            .build();
    }

    private Organisation getOrganisation(Optional<Element<WithSolicitor>> optionalRespondentElement) {
        return optionalRespondentElement.map(Element::getValue)
            .filter(element ->
                isNotEmpty(element.getSolicitor()) && isNotEmpty(element.getSolicitor().getOrganisation()))
            .map(child -> child.getSolicitor().getOrganisation())
            .orElse(Organisation.builder().build());
    }
}
