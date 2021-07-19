package uk.gov.hmcts.reform.fpl.service.translations.provider;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.TranslatableItem;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Component
public class TranslatableStandardDirectionOrderProvider implements TranslatableListItemProvider {

    private static final String CASE_FIELD = "standardDirectionOrder";

    @Override
    public List<Element<? extends TranslatableItem>> provideListItems(
        CaseData caseData) {
        StandardDirectionOrder sdo = caseData.getStandardDirectionOrder();
        return null != sdo && sdo.isSealed() ? List.of(element(StandardDirectionOrder.COLLECTION_ID, sdo)) : List.of();
    }

    @Override
    public DocumentReference provideSelectedItemDocument(CaseData caseData, UUID selectedOrderId) {
        return caseData.getStandardDirectionOrder().getOrderDoc();
    }

    @Override
    public boolean accept(CaseData caseData, UUID selectedOrderId) {
        return StandardDirectionOrder.COLLECTION_ID.equals(selectedOrderId);
    }

    @Override
    public Map<String, Object> applyTranslatedOrder(CaseData caseData,
                                                    DocumentReference document, UUID selectedOrderId) {
        StandardDirectionOrder sdo = caseData.getStandardDirectionOrder().toBuilder()
            .translatedOrderDoc(document)
            .build();

        return Map.of(CASE_FIELD, sdo);
    }
}
