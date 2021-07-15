package uk.gov.hmcts.reform.fpl.model.interfaces;

import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.Party;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.isNotBlank;


public interface WithSolicitor {

    RespondentSolicitor getSolicitor();

    void setSolicitor(RespondentSolicitor solicitor);

    Party toParty();

    default boolean hasRegisteredOrganisation() {
        return ofNullable(getSolicitor())
            .flatMap(respondentSolicitor ->
                ofNullable(respondentSolicitor.getOrganisation())
                    .map(organisation -> isNotBlank(organisation.getOrganisationID()))
            )
            .orElse(false);
    }

    default boolean hasUnregisteredOrganisation() {
        return ofNullable(getSolicitor())
            .flatMap(respondentSolicitor ->
                ofNullable(respondentSolicitor.getUnregisteredOrganisation())
                    .map(organisation -> isNotBlank(organisation.getName()))
            )
            .orElse(false);
    }
}
