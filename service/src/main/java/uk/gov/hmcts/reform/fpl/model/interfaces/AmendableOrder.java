package uk.gov.hmcts.reform.fpl.model.interfaces;

import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import java.time.LocalDate;

public interface AmendableOrder extends ModifiableItem {

    DocumentReference getDocument();

    LocalDate amendableSortDate();

}
