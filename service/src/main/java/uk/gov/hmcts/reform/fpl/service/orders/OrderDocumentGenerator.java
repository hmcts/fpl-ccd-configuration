package uk.gov.hmcts.reform.fpl.service.orders;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisDocumentGeneratorService;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OrderDocumentGenerator {

    private final DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;
    private final ObjectMapper objectMapper;

    private final List<SingleOrderDocumentParameterGenerator> validators = List.of(
        new C32CareOrderDocumentParameterGenerator()

    );


    private final Map<Order, SingleOrderDocumentParameterGenerator> typeToGenerator =
        validators.stream().collect(Collectors.toMap(
            SingleOrderDocumentParameterGenerator::accept,
            Function.identity()
        ));

    public DocmosisDocument generate(Order orderType, CaseDetails caseDetails) {
        SingleOrderDocumentParameterGenerator documentTemplateGenerator = typeToGenerator.get(orderType);
        DocmosisParameters docmosisParameters = documentTemplateGenerator.generate(caseDetails);
        DocmosisDocument docmosisDocument = docmosisDocumentGeneratorService.generateDocmosisDocument(
            objectMapper.convertValue(docmosisParameters, new TypeReference<Map<String, Object>>() {}),
            documentTemplateGenerator.template());
        return docmosisDocument;
    }
}
