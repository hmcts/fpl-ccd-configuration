package uk.gov.hmcts.reform.fpl.service.translations.provider;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.TranslatableItem;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TranslatableStandardDirectionOrderProvider implements TranslatableListItemProvider {

    private static final String CASE_FIELD = "standardDirectionOrder";

    private final Time time;

    @Override
    public List<Element<? extends TranslatableItem>> provideListItems(
        CaseData caseData) {
        StandardDirectionOrder sdo = caseData.getStandardDirectionOrder();
        return null != sdo && sdo.isSealed()
            ? List.of(element(StandardDirectionOrder.SDO_COLLECTION_ID, sdo)) : List.of();
    }

    @Override
    public TranslatableItem provideSelectedItem(CaseData caseData, UUID selectedOrderId) {
        return caseData.getStandardDirectionOrder();
    }

    @Override
    public boolean accept(CaseData caseData, UUID selectedOrderId) {
        return StandardDirectionOrder.SDO_COLLECTION_ID.equals(selectedOrderId);
    }

    @Override
    public Map<String, Object> applyTranslatedOrder(CaseData caseData,
                                                    DocumentReference document, UUID selectedOrderId) {
        StandardDirectionOrder sdo = caseData.getStandardDirectionOrder().toBuilder()
            .translatedOrderDoc(document)
            .translationUploadDateTime(time.now())
            .build();

        return Map.of(CASE_FIELD, sdo);
    }
}
