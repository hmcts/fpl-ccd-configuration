package uk.gov.hmcts.reform.fpl.model;

import lombok.Data;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;

@Jacksonized
@SuperBuilder(toBuilder = true)
@Data
public class CaseDataParent {

    protected final List<Element<Direction>> allParties;
    protected final List<Element<Direction>> allPartiesCustom;
    protected final List<Element<Direction>> localAuthorityDirections;
    protected final List<Element<Direction>> localAuthorityDirectionsCustom;
    protected final List<Element<Direction>> courtDirections;
    protected final List<Element<Direction>> courtDirectionsCustom;
    protected final List<Element<Direction>> cafcassDirections;
    protected final List<Element<Direction>> cafcassDirectionsCustom;
    protected final List<Element<Direction>> otherPartiesDirections;
    protected final List<Element<Direction>> otherPartiesDirectionsCustom;
    protected final List<Element<Direction>> respondentDirections;
    protected final List<Element<Direction>> respondentDirectionsCustom;
}
