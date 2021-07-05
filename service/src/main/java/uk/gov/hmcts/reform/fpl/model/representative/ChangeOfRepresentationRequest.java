package uk.gov.hmcts.reform.fpl.model.representative;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.ConfidentialParty;
import uk.gov.hmcts.reform.fpl.model.noc.ChangeOfRepresentation;
import uk.gov.hmcts.reform.fpl.model.noc.ChangeOfRepresentationMethod;

import java.util.List;

@Value
@Builder(toBuilder = true)
public class ChangeOfRepresentationRequest {

    List<Element<ChangeOfRepresentation>> current;
    ConfidentialParty respondent;
    RespondentSolicitor addedRepresentative;
    RespondentSolicitor removedRepresentative;
    ChangeOfRepresentationMethod method;
    String by;

}
