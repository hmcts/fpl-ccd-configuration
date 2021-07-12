package uk.gov.hmcts.reform.fpl.model.interfaces;

import org.w3c.dom.DocumentType;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.time.LocalDate;
import java.util.List;

public interface AmendableOrder {
    String asLabel();

    LocalDate amendableSortDate();

    DocumentReference getDocument();

    String getAmendedOrderType();

    List<Element<Other>> getSelectedOthers();
}
