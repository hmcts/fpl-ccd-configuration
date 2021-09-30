package uk.gov.hmcts.reform.fpl.service.document;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.enums.docmosis.RenderFormat;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.configuration.Language;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.service.CaseConverter;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.DocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.generator.DocmosisCommonElementDecorator;
import uk.gov.hmcts.reform.fpl.service.orders.generator.DocmosisParameterGenerator;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;

@ExtendWith(MockitoExtension.class)
class DocumentGeneratorTest {

    private static final Map<String, Object> TEMPLATE_DATA = Map.of("key", "value");
    private static final DocmosisTemplates DOCMOSIS_TEMPLATE = DocmosisTemplates.ORDER_V2;
    private static final DocmosisDocument DOCMOSIS_DOCUMENT = mock(DocmosisDocument.class);
    private static final DocmosisParameters DOCMOSIS_PARAMETERS = mock(DocmosisParameters.class);
    private static final CaseData CASE_DATA = mock(CaseData.class);
    private final DocmosisParameterGenerator parameterGenerator = mock(DocmosisParameterGenerator.class);
    private static final Order ORDER = Order.C32A_CARE_ORDER;
    private static final RenderFormat FORMAT = RenderFormat.PDF;
    private static final OrderStatus STATUS = SEALED;

    @Mock
    private DocmosisDocumentGeneratorService docmosisRenderer;

    @Mock
    private DocmosisCommonElementDecorator decorator;

    @Mock
    private CaseConverter caseConverter;

    @InjectMocks
    private DocumentGenerator underTest;

    @BeforeEach
    void setUp() {
        when(decorator.decorate(DOCMOSIS_PARAMETERS, CASE_DATA, STATUS, ORDER)).thenReturn(DOCMOSIS_PARAMETERS);
        when(caseConverter.toMap(DOCMOSIS_PARAMETERS)).thenReturn(TEMPLATE_DATA);
        when(docmosisRenderer.generateDocmosisDocument(TEMPLATE_DATA, DOCMOSIS_TEMPLATE, FORMAT, Language.ENGLISH))
            .thenReturn(DOCMOSIS_DOCUMENT);
        when(parameterGenerator.accept()).thenReturn(ORDER);
        when(parameterGenerator.template()).thenReturn(DOCMOSIS_TEMPLATE);
        when(parameterGenerator.generate(CASE_DATA)).thenReturn(DOCMOSIS_PARAMETERS);
    }

    @Test
    void shouldGenerateDocument() {
        DocmosisDocument generatedDocument = underTest.generateDocument(CASE_DATA, parameterGenerator, FORMAT, STATUS);

        assertThat(generatedDocument).isEqualTo(DOCMOSIS_DOCUMENT);
    }

    @Test
    void shouldGenerateDocumentWithFewerParameters() {
        DocmosisDocument generatedDocument = underTest.generateDocument(CASE_DATA, parameterGenerator);

        assertThat(generatedDocument).isEqualTo(DOCMOSIS_DOCUMENT);
    }

}
