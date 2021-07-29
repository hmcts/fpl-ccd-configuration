package uk.gov.hmcts.reform.fpl.model.interfaces;

import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;

public interface ModifiableItem {

    String asLabel();

    String getModifiedItemType();

    List<Element<Other>> getSelectedOthers();
}
