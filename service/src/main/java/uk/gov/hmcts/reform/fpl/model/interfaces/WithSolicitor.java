package uk.gov.hmcts.reform.fpl.model.interfaces;

import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;


public interface WithSolicitor {

    RespondentSolicitor getSolicitor();

    void setSolicitor(RespondentSolicitor solicitor);

}
