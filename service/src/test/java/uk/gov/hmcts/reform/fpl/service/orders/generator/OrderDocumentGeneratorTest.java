package uk.gov.hmcts.reform.fpl.service.orders.generator;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.enums.docmosis.RenderFormat;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.service.document.DocumentGenerator;

import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.DRAFT;

class OrderDocumentGeneratorTest {

    private static final Order ORDER = Order.C32A_CARE_ORDER;
    private static final CaseData CASE_DATA = mock(CaseData.class);
    private static final DocmosisDocument DOCMOSIS_DOCUMENT = mock(DocmosisDocument.class);
    private static final RenderFormat FORMAT = mock(RenderFormat.class);
    private static final OrderStatus STATUS = DRAFT;

    private final DocmosisParameterGenerator generator = mock(DocmosisParameterGenerator.class);

    private final OrderDocumentGeneratorHolder generatorHolder = mock(OrderDocumentGeneratorHolder.class);
    private final DocumentMerger documentMerger = mock(DocumentMerger.class);
    private DocumentGenerator documentGenerator = mock(DocumentGenerator.class);

    private final OrderDocumentGenerator underTest = new OrderDocumentGenerator(
        generatorHolder, documentMerger, documentGenerator
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
        when(documentGenerator.generateDocument(CASE_DATA, generator, FORMAT, STATUS)).thenReturn(DOCMOSIS_DOCUMENT);

        assertThat(underTest.generate(ORDER, CASE_DATA, STATUS, FORMAT)).isEqualTo(DOCMOSIS_DOCUMENT);
    }

}
