package uk.gov.hmcts.reform.fpl.model.event;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.model.LegalCounsellor;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;

@Builder
@Value
@Jacksonized
public class ManageLegalCounselEventData {

    List<Element<LegalCounsellor>> legalCounsellors;

}
