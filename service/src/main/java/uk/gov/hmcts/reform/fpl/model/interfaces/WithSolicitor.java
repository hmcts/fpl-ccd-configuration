package uk.gov.hmcts.reform.fpl.model.interfaces;

import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.Party;


public interface WithSolicitor {

    RespondentSolicitor getSolicitor();

    void setSolicitor(RespondentSolicitor solicitor);

    Party toParty();
}
