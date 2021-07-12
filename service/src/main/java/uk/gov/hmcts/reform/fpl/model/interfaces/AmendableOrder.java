package uk.gov.hmcts.reform.fpl.model.interfaces;

import org.w3c.dom.DocumentType;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import java.time.LocalDate;

public interface AmendableOrder {
    String asLabel();

    LocalDate amendableSortDate();

    DocumentReference getDocument();

    String getAmendedOrderType();
}
