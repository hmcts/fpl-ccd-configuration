package uk.gov.hmcts.reform.fpl.service.orders.prepopulator.section;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.model.order.OrderSection;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.orders.generator.OrderDocumentGenerator;

import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DraftOrderPreviewSectionPrePopulatorTest {

    private static final Order ORDER = Order.C32_CARE_ORDER;
    private static final CaseDetails CASE_DETAILS = mock(CaseDetails.class);
    private static final byte[] DOCUMENT_DATA = {1, 2, 3, 4, 5};
    private static final DocmosisDocument DOCUMENT = new DocmosisDocument("some title", DOCUMENT_DATA);

    private final OrderDocumentGenerator orderDocumentGenerator = mock(OrderDocumentGenerator.class);
    private final UploadDocumentService uploadDocumentService = mock(UploadDocumentService.class);

    private final DraftOrderPreviewSectionPrePopulator underTest = new DraftOrderPreviewSectionPrePopulator(
        orderDocumentGenerator, uploadDocumentService
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

        when(orderDocumentGenerator.generate(ORDER, caseData, OrderStatus.DRAFT)).thenReturn(DOCUMENT);

        Map<String, Object> actual = underTest.prePopulate(caseData, CASE_DETAILS);

        assertThat(actual).isEqualTo(Map.of());
        verify(orderDocumentGenerator).generate(ORDER, caseData, OrderStatus.DRAFT);
        verify(uploadDocumentService).uploadPDF(DOCUMENT_DATA, "");
    }
}
