package uk.gov.hmcts.reform.fpl.service.orders.prepopulator.section;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.enums.docmosis.RenderFormat;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.model.order.OrderSection;
import uk.gov.hmcts.reform.fpl.service.orders.OrderCreationService;

import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DraftOrderPreviewSectionPrePopulatorTest {

    private static final Order ORDER = mock(Order.class);
    private static final DocumentReference DOCUMENT_REFERENCE = mock(DocumentReference.class);

    private final OrderCreationService creationService = mock(OrderCreationService.class);

    private final DraftOrderPreviewSectionPrePopulator underTest = new DraftOrderPreviewSectionPrePopulator(
        creationService
    );

    @Test
    void accept() {
        assertThat(underTest.accept()).isEqualTo(OrderSection.REVIEW);
    }

    @Test
    void prePopulate() {
        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder().manageOrdersType(ORDER).build())
            .build();

        when(creationService.createOrderDocument(caseData, OrderStatus.DRAFT, RenderFormat.PDF))
            .thenReturn(DOCUMENT_REFERENCE);

        Map<String, Object> actual = underTest.prePopulate(caseData);

        assertThat(actual).isEqualTo(Map.of("orderPreview", DOCUMENT_REFERENCE));
    }
}
