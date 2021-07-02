package uk.gov.hmcts.reform.fpl.model.order;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.interfaces.AmendableOrder;

import java.time.LocalDate;

@Value
@Builder
@Jacksonized
public class UrgentHearingOrder implements AmendableOrder {
    DocumentReference order;
    DocumentReference unsealedOrder;
    String allocation;
    LocalDate dateAdded;

    @Override
    public String asLabel() {
        return "Urgent hearing order";
    }
}
