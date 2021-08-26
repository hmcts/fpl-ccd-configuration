package uk.gov.hmcts.reform.fpl.model.interfaces;

import java.time.LocalDate;

public interface AmendableOrder extends ModifiableItem {

    LocalDate amendableSortDate();

}
