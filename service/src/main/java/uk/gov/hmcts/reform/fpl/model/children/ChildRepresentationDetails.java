package uk.gov.hmcts.reform.fpl.model.children;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;

@Value
@Builder
@Jacksonized
public class ChildRepresentationDetails {
    String useMainSolicitor;
    RespondentSolicitor solicitor;
}
