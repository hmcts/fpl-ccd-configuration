package uk.gov.hmcts.reform.fpl.service.orders.generator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.DocmosisParameters;

import java.util.Map;
import java.util.Optional;


@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OrderDocumentGenerator {

    private final DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;
    private final ObjectMapper objectMapper;
    private final OrderDocumentGeneratorHolder holder;

    public DocmosisDocument generate(Order orderType, CaseDetails caseDetails) {
        DocmosisParameters docmosisParameters = Optional.ofNullable(holder.getTypeToGenerator().get(orderType))
            .map(documentTemplateGenerator -> documentTemplateGenerator.generate(caseDetails)).orElseThrow(
                () -> new UnsupportedOperationException("Not implemented yet for order " + orderType.name())
            );
        Map<String, Object> templateData = objectMapper.convertValue(docmosisParameters, new TypeReference<>() {});

        return docmosisDocumentGeneratorService.generateDocmosisDocument(
            templateData, holder.getTypeToGenerator().get(orderType).template()
        );
    }
}
