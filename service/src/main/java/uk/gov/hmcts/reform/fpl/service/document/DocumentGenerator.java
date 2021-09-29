package uk.gov.hmcts.reform.fpl.service.document;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.enums.docmosis.RenderFormat;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.configuration.Language;
import uk.gov.hmcts.reform.fpl.service.CaseConverter;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.DocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.generator.DocmosisCommonElementDecorator;
import uk.gov.hmcts.reform.fpl.service.orders.generator.DocmosisParameterGenerator;

import java.util.Map;

import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;
import static uk.gov.hmcts.reform.fpl.enums.docmosis.RenderFormat.PDF;

@Component
@RequiredArgsConstructor
public class DocumentGenerator {

    private final DocmosisCommonElementDecorator decorator;
    private final CaseConverter caseConverter;
    private final DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;

    public DocmosisDocument generateDocument(CaseData caseData,
                                             DocmosisParameterGenerator docmosisParameterGenerator,
                                             RenderFormat format,
                                             OrderStatus orderStatus) {
        DocmosisParameters customParameters = docmosisParameterGenerator.generate(caseData);
        DocmosisParameters docmosisParameters =
            decorator.decorate(customParameters, caseData, orderStatus, docmosisParameterGenerator.accept());
        Map<String, Object> templateData = caseConverter.toMap(docmosisParameters);

        return docmosisDocumentGeneratorService.generateDocmosisDocument(
            templateData, docmosisParameterGenerator.template(), format, Language.ENGLISH
        );
    }

    public DocmosisDocument generateDocument(CaseData caseData, DocmosisParameterGenerator parameterGenerator) {
        return generateDocument(caseData, parameterGenerator, PDF, SEALED);
    }
}
