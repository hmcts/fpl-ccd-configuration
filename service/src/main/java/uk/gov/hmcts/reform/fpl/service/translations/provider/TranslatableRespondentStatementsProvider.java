package uk.gov.hmcts.reform.fpl.service.translations.provider;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.RespondentStatementV2;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.TranslatableItem;
import uk.gov.hmcts.reform.fpl.service.translations.provider.decorator.SupportingEvidenceBundleTranslatorDecorator;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Collections.unmodifiableList;
import static java.util.stream.Stream.concat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.nullSafeList;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TranslatableRespondentStatementsProvider implements TranslatableListItemProvider {

    private final SupportingEvidenceBundleTranslatorDecorator decorator;

    @Override
    public List<Element<? extends TranslatableItem>> provideListItems(CaseData caseData) {
        return unmodifiableList(getElements(caseData));
    }

    private List<Element<RespondentStatementV2>> getElements(CaseData caseData) {
        return concat(concat(nullSafeList(caseData.getRespStmtListLA()).stream(),
                nullSafeList(caseData.getRespStmtListCTSC()).stream()),
            nullSafeList(caseData.getRespStmtList()).stream()).collect(Collectors.toList());
    }

    @Override
    public TranslatableItem provideSelectedItem(CaseData caseData, UUID selectedOrderId) {
        return getElements(caseData)
            .stream()
            .filter(order -> order.getId().equals(selectedOrderId))
            .findFirst().map(Element::getValue)
            .orElseThrow(IllegalArgumentException::new);
    }

    @Override
    public boolean accept(CaseData caseData, UUID selectedOrderId) {
        return provideListItems(caseData).stream().anyMatch(order -> Objects.equals(selectedOrderId, order.getId()));
    }

    @Override
    public Map<String, Object> applyTranslatedOrder(CaseData caseData, DocumentReference document,
                                                    UUID selectedOrderId) {
        return Map.of("respStmtList",
            nullSafeList(caseData.getRespStmtList()).stream()
                .map(decorator.translatedRespondentStatement(document, selectedOrderId))
                .collect(Collectors.toList()),
        "respStmtListCTSC",
            nullSafeList(caseData.getRespStmtListCTSC()).stream()
                .map(decorator.translatedRespondentStatement(document, selectedOrderId))
                .collect(Collectors.toList()),
        "respStmtListLA",
            nullSafeList(caseData.getRespStmtListLA()).stream()
                .map(decorator.translatedRespondentStatement(document, selectedOrderId))
                .collect(Collectors.toList()));
    }

}
