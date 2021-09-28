package uk.gov.hmcts.reform.fpl.service.translations.provider;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingFurtherEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
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
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TranslatableHearingFurtherEvidenceProvider implements TranslatableListItemProvider {

    private static final String HEARING_FURTHER_EVIDENCE_DOCUMENTS_KEY = "hearingFurtherEvidenceDocuments";

    private final SupportingEvidenceBundleTranslatorDecorator decorator;

    @Override
    public List<Element<? extends TranslatableItem>> provideListItems(CaseData caseData) {
        return unmodifiableList(getElements(caseData));
    }

    private List<Element<SupportingEvidenceBundle>> getElements(CaseData caseData) {
        return caseData.getHearingFurtherEvidenceDocuments()
            .stream()
            .flatMap(x -> x.getValue().getSupportingEvidenceBundle().stream())
            .collect(Collectors.toList());
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

        return Map.of(HEARING_FURTHER_EVIDENCE_DOCUMENTS_KEY,
            translateHearingFurtherEvidenceDocuments(caseData, document, selectedOrderId));
    }

    private List<Element<HearingFurtherEvidenceBundle>>
        translateHearingFurtherEvidenceDocuments(CaseData caseData,
                                             DocumentReference document,
                                             UUID selectedOrderId) {
        return caseData.getHearingFurtherEvidenceDocuments().stream().map(
            it -> element(it.getId(), it.getValue().toBuilder()
                .supportingEvidenceBundle(it.getValue().getSupportingEvidenceBundle().stream()
                    .map(decorator.translatedBundle(document, selectedOrderId))
                    .collect(Collectors.toList()))
                .build())).collect(Collectors.toList());
    }


}
