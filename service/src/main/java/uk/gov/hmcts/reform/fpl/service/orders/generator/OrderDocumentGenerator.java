package uk.gov.hmcts.reform.fpl.service.orders.generator;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.enums.docmosis.RenderFormat;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.service.document.DocumentGenerator;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class OrderDocumentGenerator {

    private final OrderDocumentGeneratorHolder holder;
    private final DocumentMerger documentMerger;
    private final DocumentGenerator documentGenerator;

    public DocmosisDocument generate(Order orderType, CaseData caseData, OrderStatus orderStatus, RenderFormat format) {
        DocmosisParameterGenerator docmosisParameterGenerator = holder.getTypeToGenerator().get(orderType);

        if (docmosisParameterGenerator == null) {
            throw new UnsupportedOperationException("Not implemented yet for order " + orderType.name());
        }

        DocmosisDocument docmosisDocument = documentGenerator.generateDocument(caseData,
            docmosisParameterGenerator,
            format,
            orderStatus);

        AdditionalDocumentsCollector documentsHolder = holder.getTypeToAdditionalDocumentsCollector().get(orderType);
        if (documentsHolder != null && OrderStatus.SEALED == orderStatus) {
            return documentMerger.mergeDocuments(docmosisDocument, documentsHolder.additionalDocuments(caseData));
        }

        return docmosisDocument;
    }

}
