package uk.gov.hmcts.reform.fpl.service.translations.provider;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.TranslatableItem;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static java.util.Collections.unmodifiableList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.nullSafeList;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TranslatableNoticeOfProceedingsProvider implements TranslatableListItemProvider {

    private static final String CASE_FIELD = "noticeOfProceedingsBundle";

    private final Time time;

    @Override
    public List<Element<? extends TranslatableItem>> provideListItems(CaseData caseData) {
        return unmodifiableList(nullSafeList(caseData.getNoticeOfProceedingsBundle()));
    }

    @Override
    public TranslatableItem provideSelectedItem(CaseData caseData, UUID selectedOrderId) {
        return nullSafeList(caseData.getNoticeOfProceedingsBundle())
            .stream()
            .filter(nop -> nop.getId().equals(selectedOrderId))
            .findFirst().map(Element::getValue)
            .orElseThrow(IllegalArgumentException::new);
    }

    @Override
    public boolean accept(CaseData caseData, UUID selectedOrderId) {
        return nullSafeList(caseData.getNoticeOfProceedingsBundle()).stream()
            .anyMatch(cmo -> Objects.equals(selectedOrderId, cmo.getId()));
    }

    @Override
    public Map<String, Object> applyTranslatedOrder(CaseData caseData,
                                                    DocumentReference document, UUID selectedOrderId) {
        List<Element<DocumentBundle>> orders = nullSafeList(caseData.getNoticeOfProceedingsBundle());

        orders.stream()
            .filter(order -> Objects.equals(order.getId(), selectedOrderId))
            .findFirst()
            .ifPresent(order -> {
                DocumentBundle translated = order.getValue().toBuilder()
                    .translatedDocument(document)
                    .translationUploadDateTime(time.now())
                    .build();
                orders.set(orders.indexOf(order), element(order.getId(), translated));
            });

        return Map.of(CASE_FIELD, orders);
    }
}
