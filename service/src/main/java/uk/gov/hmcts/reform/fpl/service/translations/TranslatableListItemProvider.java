package uk.gov.hmcts.reform.fpl.service.translations;

import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.TranslatableItem;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface TranslatableListItemProvider {

    List<Element<? extends TranslatableItem>> provideListItems(CaseData caseData);

    DocumentReference provideSelectedItemDocument(CaseData caseData, UUID selectedOrderId);

    boolean accept(CaseData caseData, UUID selectedOrderId);

    Map<String, Object> applyTranslatedOrder(CaseData caseData, DocumentReference document, UUID selectedOrderId);
}
