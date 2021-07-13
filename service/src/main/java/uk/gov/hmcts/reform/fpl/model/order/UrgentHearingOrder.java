package uk.gov.hmcts.reform.fpl.model.order;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.interfaces.AmendableOrder;

import java.time.LocalDate;
import java.util.UUID;

import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;

@Value
@Builder(toBuilder = true)
@Jacksonized
public class UrgentHearingOrder implements AmendableOrder {
    public static final UUID COLLECTION_ID = UUID.fromString("5d05d011-5d01-5d01-5d01-5d05d05d05d0");

    DocumentReference order;
    DocumentReference unsealedOrder;
    String allocation;
    LocalDate dateAdded;
    LocalDate amendedDate;

    @Override
    public String asLabel() {
        return "Urgent hearing order - " + formatLocalDateToString(dateAdded, DATE);
    }

    @Override
    public LocalDate amendableSortDate() {
        return dateAdded;
    }
}
