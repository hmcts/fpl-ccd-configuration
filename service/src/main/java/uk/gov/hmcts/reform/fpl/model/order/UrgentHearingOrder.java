package uk.gov.hmcts.reform.fpl.model.order;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.model.Allocation;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import java.time.LocalDate;

@Value
@Builder
@Jacksonized
public class UrgentHearingOrder {
    DocumentReference order;
    DocumentReference unsealedOrder;
    Allocation allocation;
    LocalDate dateAdded;
}
