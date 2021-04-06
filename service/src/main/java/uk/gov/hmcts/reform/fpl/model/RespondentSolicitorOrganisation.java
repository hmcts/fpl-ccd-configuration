package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;

import java.util.UUID;

@Data
@Builder
@Jacksonized
public class RespondentSolicitorOrganisation {
    private UUID respondentId;
    private final RespondentParty party;
    private RespondentSolicitor solicitor;
    private OrganisationPolicy organisationPolicy;
}
