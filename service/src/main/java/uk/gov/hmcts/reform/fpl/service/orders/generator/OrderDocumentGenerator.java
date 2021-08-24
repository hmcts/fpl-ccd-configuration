package uk.gov.hmcts.reform.fpl.service.orders.generator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.enums.docmosis.RenderFormat;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.configuration.Language;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.DocmosisParameters;

import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class OrderDocumentGenerator {

    private final DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;
    private final ObjectMapper objectMapper;
    private final OrderDocumentGeneratorHolder holder;
    private final DocmosisCommonElementDecorator decorator;
    private final DocumentMerger documentMerger;

    public DocmosisDocument generate(Order orderType, CaseData caseData, OrderStatus orderStatus, RenderFormat format) {
        DocmosisParameterGenerator documentGenerator = holder.getTypeToGenerator().get(orderType);
        AdditionalDocumentsCollector documentsHolder = holder.getTypeToAdditionalDocumentsCollector().get(orderType);

        DocmosisParameters docmosisParameters = Optional.ofNullable(documentGenerator)
            .map(generator -> {
                DocmosisParameters customParameters = generator.generate(caseData);
                return decorator.decorate(customParameters, caseData, orderStatus, generator.accept());
            })
            .orElseThrow(() -> new UnsupportedOperationException("Not implemented yet for order " + orderType.name()));

        Map<String, Object> templateData = objectMapper.convertValue(docmosisParameters, new TypeReference<>() {});

        DocmosisDocument docmosisDocument = docmosisDocumentGeneratorService.generateDocmosisDocument(
            templateData, documentGenerator.template(), format, Language.ENGLISH
        );

        if (documentsHolder != null && OrderStatus.SEALED == orderStatus) {
            return documentMerger.mergeDocuments(docmosisDocument, documentsHolder.additionalDocuments(caseData));
        }

        return docmosisDocument;
    }
}
