package uk.gov.hmcts.reform.fpl.service.orders.generator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.enums.docmosis.RenderFormat;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.DocmosisParameters;

import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.DRAFT;

class OrderDocumentGeneratorTest {

    private static final Order ORDER = Order.C32A_CARE_ORDER;
    private static final CaseData CASE_DATA = mock(CaseData.class);
    private static final DocmosisParameters DOCMOSIS_PARAMETERS = mock(DocmosisParameters.class);
    private static final Map<String, Object> TEMPLATE_DATA = Map.of("key", "value");
    private static final DocmosisTemplates DOCMOSIS_TEMPLATE = DocmosisTemplates.ORDER_V2;
    private static final DocmosisDocument DOCMOSIS_DOCUMENT = mock(DocmosisDocument.class);
    private static final RenderFormat FORMAT = mock(RenderFormat.class);
    private static final OrderStatus STATUS = DRAFT;

    private final DocmosisParameterGenerator generator = mock(DocmosisParameterGenerator.class);

    private final DocmosisDocumentGeneratorService docmosisRenderer = mock(DocmosisDocumentGeneratorService.class);
    private final ObjectMapper objectMapper = mock(ObjectMapper.class);
    private final OrderDocumentGeneratorHolder generatorHolder = mock(OrderDocumentGeneratorHolder.class);
    private final DocmosisCommonElementDecorator decorator = mock(DocmosisCommonElementDecorator.class);
    private final DocumentMerger documentMerger = mock(DocumentMerger.class);

    private final OrderDocumentGenerator underTest = new OrderDocumentGenerator(
        docmosisRenderer, objectMapper, generatorHolder, decorator, documentMerger
    );

    @Test
    void generateWithNoGenerator() {
        when(generatorHolder.getTypeToGenerator()).thenReturn(Map.of());

        assertThatThrownBy(() -> underTest.generate(ORDER, CASE_DATA, STATUS, FORMAT))
            .isInstanceOf(UnsupportedOperationException.class)
            .hasMessage("Not implemented yet for order " + ORDER.name());
    }

    @Test
    void generateWithGenerator() {
        when(generatorHolder.getTypeToGenerator()).thenReturn(Map.of(ORDER, generator));
        when(generator.accept()).thenReturn(ORDER);
        when(generator.generate(CASE_DATA)).thenReturn(DOCMOSIS_PARAMETERS);
        when(generator.template()).thenReturn(DOCMOSIS_TEMPLATE);
        when(decorator.decorate(DOCMOSIS_PARAMETERS, CASE_DATA, STATUS, ORDER)).thenReturn(DOCMOSIS_PARAMETERS);
        when(objectMapper.convertValue(
            eq(DOCMOSIS_PARAMETERS),
            Mockito.<TypeReference<Map<String, Object>>>any())).thenReturn(TEMPLATE_DATA);
        when(docmosisRenderer.generateDocmosisDocument(TEMPLATE_DATA, DOCMOSIS_TEMPLATE, FORMAT))
            .thenReturn(DOCMOSIS_DOCUMENT);

        assertThat(underTest.generate(ORDER, CASE_DATA, STATUS, FORMAT)).isEqualTo(DOCMOSIS_DOCUMENT);
    }
}
