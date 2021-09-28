package uk.gov.hmcts.reform.fpl.service.translations.provider;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.group.C110A;
import uk.gov.hmcts.reform.fpl.model.interfaces.TranslatableItem;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TranslatableC110AProvider implements TranslatableListItemProvider {

    private final Time time;

    @Override
    public List<Element<? extends TranslatableItem>> provideListItems(CaseData caseData) {
        C110A c110A = caseData.getC110A();
        return null != c110A ? List.of(element(C110A.COLLECTION_ID, c110A)) : List.of();
    }

    @Override
    public TranslatableItem provideSelectedItem(CaseData caseData, UUID selectedOrderId) {
        return caseData.getC110A();
    }

    @Override
    public boolean accept(CaseData caseData, UUID selectedOrderId) {
        return C110A.COLLECTION_ID.equals(selectedOrderId);
    }

    @Override
    public Map<String, Object> applyTranslatedOrder(CaseData caseData,
                                                    DocumentReference document, UUID selectedOrderId) {
        return Map.of(
            "submittedFormTranslationUploadDateTime", time.now(),
            "translatedSubmittedForm", document);
    }
}
