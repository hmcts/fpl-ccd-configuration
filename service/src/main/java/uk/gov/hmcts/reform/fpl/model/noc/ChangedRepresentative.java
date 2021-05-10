package uk.gov.hmcts.reform.fpl.model.noc;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.reform.ccd.model.Organisation;

@Value
@Builder
public class ChangedRepresentative {
    String firstName;
    String lastName;
    String email;
    Organisation organisation;
}
