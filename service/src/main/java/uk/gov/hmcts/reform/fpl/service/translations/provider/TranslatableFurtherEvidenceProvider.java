package uk.gov.hmcts.reform.fpl.service.translations.provider;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.TranslatableItem;
import uk.gov.hmcts.reform.fpl.service.document.ConfidentialDocumentsSplitter;
import uk.gov.hmcts.reform.fpl.service.translations.provider.decorator.SupportingEvidenceBundleTranslatorDecorator;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Collections.unmodifiableList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TranslatableFurtherEvidenceProvider implements TranslatableListItemProvider {

    private static final String FURTHER_EVIDENCE_DOCUMENTS_KEY = "furtherEvidenceDocuments";

    private final ConfidentialDocumentsSplitter confidentialDocumentsSplitter;
    private final SupportingEvidenceBundleTranslatorDecorator decorator;

    @Override
    public List<Element<? extends TranslatableItem>> provideListItems(CaseData caseData) {
        return unmodifiableList(getElements(caseData));
    }

    private List<Element<SupportingEvidenceBundle>> getElements(CaseData caseData) {
        return defaultIfNull(caseData.getFurtherEvidenceDocuments(), new ArrayList<>());
    }

    @Override
    public DocumentReference provideSelectedItemDocument(CaseData caseData, UUID selectedOrderId) {
        return getElements(caseData)
            .stream()
            .filter(order -> order.getId().equals(selectedOrderId))
            .findFirst().map(it -> it.getValue().getDocument())
            .orElseThrow(IllegalArgumentException::new);
    }

    @Override
    public boolean accept(CaseData caseData, UUID selectedOrderId) {
        return provideListItems(caseData).stream().anyMatch(order -> Objects.equals(selectedOrderId, order.getId()));
    }

    @Override
    public Map<String, Object> applyTranslatedOrder(CaseData caseData, DocumentReference document,
                                                    UUID selectedOrderId) {
        CaseDetailsMap data = CaseDetailsMap.caseDetailsMap(new HashMap<>());

        List<Element<SupportingEvidenceBundle>> translatedFurtherEvidenceDocuments =
            translateFurtherEvidenceDocuments(caseData, document, selectedOrderId);

        data.put(FURTHER_EVIDENCE_DOCUMENTS_KEY, translatedFurtherEvidenceDocuments);

        confidentialDocumentsSplitter.updateConfidentialDocsInCaseDetails(data,
            translatedFurtherEvidenceDocuments,
            FURTHER_EVIDENCE_DOCUMENTS_KEY);

        return data;
    }

    private List<Element<SupportingEvidenceBundle>> translateFurtherEvidenceDocuments(CaseData caseData,
                                                                                      DocumentReference document,
                                                                                      UUID selectedOrderId) {
        return ObjectUtils.<List<Element<SupportingEvidenceBundle>>>defaultIfNull(
                caseData.getFurtherEvidenceDocuments(),
                new ArrayList<>())
            .stream()
            .map(decorator.translatedBundle(document, selectedOrderId))
            .collect(Collectors.toList());
    }
}
