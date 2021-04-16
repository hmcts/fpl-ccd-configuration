package uk.gov.hmcts.reform.fpl.components;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.fpl.enums.SolicitorRole;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.Optional;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@Component
public class RespondentPolicyConverter {
    public OrganisationPolicy generateForSubmission(SolicitorRole solicitorRole,
                                                    Optional<Element<Respondent>> optionalRespondentElement) {
        return OrganisationPolicy.builder()
            .organisation(getOrganisation(optionalRespondentElement))
            .orgPolicyCaseAssignedRole(solicitorRole.getCaseRoleLabel())
            .build();
    }

    private Organisation getOrganisation(Optional<Element<Respondent>> optionalRespondentElement) {
        if (hasOrganisation(optionalRespondentElement)) {
            RespondentSolicitor respondentSolicitor = optionalRespondentElement.get().getValue().getSolicitor();
            return respondentSolicitor.getOrganisation();
        }

        return Organisation.builder().build();
    }

    private boolean hasOrganisation(Optional<Element<Respondent>> optionalRespondentElement) {
        if (optionalRespondentElement.isEmpty()) {
            return false;
        }

        RespondentSolicitor respondentSolicitor = optionalRespondentElement.get().getValue().getSolicitor();

        return isNotEmpty(respondentSolicitor) && isNotEmpty(respondentSolicitor.getOrganisation());
    }
}
