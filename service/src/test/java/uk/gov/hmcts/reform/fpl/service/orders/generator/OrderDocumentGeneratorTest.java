package uk.gov.hmcts.reform.fpl.service.orders.generator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
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

    private static final Order ORDER = Order.C32_CARE_ORDER;
    private static final CaseData CASE_DATA = mock(CaseData.class);
    private static final DocmosisParameters DOCMOSIS_PARAMETERS = mock(DocmosisParameters.class);
    private static final Map<String, Object> TEMPLATE_DATA = Map.of("key", "value");
    private static final DocmosisTemplates DOCMOSIS_TEMPLATE = DocmosisTemplates.ORDER;
    private static final DocmosisDocument DOCMOSIS_DOCUMENT = mock(DocmosisDocument.class);
    private static final OrderStatus STATUS = DRAFT;

    private final DocmosisDocumentGeneratorService docmosisDocumentGeneratorService = mock(
        DocmosisDocumentGeneratorService.class);
    private final ObjectMapper objectMapper = mock(ObjectMapper.class);
    private final OrderDocumentGeneratorHolder orderDocumentGeneratorHolder = mock(OrderDocumentGeneratorHolder.class);

    private final OrderDocumentGenerator underTest = new OrderDocumentGenerator(
        docmosisDocumentGeneratorService, objectMapper, orderDocumentGeneratorHolder
    );
    private final SingleOrderDocumentParameterGenerator generator = mock(SingleOrderDocumentParameterGenerator.class);

    @Test
    void generateWithNoGenerator() {
        when(orderDocumentGeneratorHolder.getTypeToGenerator()).thenReturn(Map.of());

        assertThatThrownBy(() -> underTest.generate(ORDER, CASE_DATA, STATUS))
            .isInstanceOf(UnsupportedOperationException.class)
            .hasMessage("Not implemented yet for order " + ORDER.name());
    }

    @Test
    void generateWithGenerator() {
        when(orderDocumentGeneratorHolder.getTypeToGenerator()).thenReturn(Map.of(ORDER, generator));
        when(generator.generate(CASE_DATA, STATUS)).thenReturn(DOCMOSIS_PARAMETERS);
        when(generator.template()).thenReturn(DOCMOSIS_TEMPLATE);
        when(objectMapper.convertValue(eq(DOCMOSIS_PARAMETERS),
            Mockito.<TypeReference<Map<String, Object>>>any())).thenReturn(TEMPLATE_DATA);
        when(docmosisDocumentGeneratorService.generateDocmosisDocument(TEMPLATE_DATA, DOCMOSIS_TEMPLATE)).thenReturn(
            DOCMOSIS_DOCUMENT);

        assertThat(underTest.generate(ORDER, CASE_DATA, STATUS)).isEqualTo(DOCMOSIS_DOCUMENT);
    }
}
