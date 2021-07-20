package uk.gov.hmcts.reform.fpl.model.order;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.enums.AmendedOrderType;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.AmendableOrder;
import uk.gov.hmcts.reform.fpl.model.interfaces.TranslatableItem;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;

@Value
@Builder(toBuilder = true)
@Jacksonized
public class UrgentHearingOrder implements AmendableOrder, TranslatableItem {
    public static final UUID COLLECTION_ID = UUID.fromString("5d05d011-5d01-5d01-5d01-5d05d05d05d0");

    DocumentReference order;
    DocumentReference translatedOrder;
    DocumentReference unsealedOrder;
    String allocation;
    LocalDate dateAdded;
    LocalDate amendedDate;
    List<Element<Other>> others;
    LocalDateTime translationUploadDateTime;

    @Override
    public String asLabel() {
        return "Urgent hearing order - " + formatLocalDateToString(dateAdded, DATE);
    }

    @Override
    @JsonIgnore
    public boolean hasBeenTranslated() {
        return Objects.nonNull(translatedOrder);
    }

    @Override
    public LocalDateTime translationUploadDateTime() {
        return translationUploadDateTime;
    }

    @Override
    public LocalDate amendableSortDate() {
        return dateAdded;
    }

    @Override
    @JsonIgnore
    public DocumentReference getDocument() {
        return order;
    }

    @JsonIgnore
    @Override
    public String getAmendedOrderType() {
        return AmendedOrderType.URGENT_HEARING_ORDER.getLabel();
    }

    @JsonIgnore
    @Override
    public List<Element<Other>> getSelectedOthers() {
        return defaultIfNull(this.getOthers(), new ArrayList<>());
    }
}
