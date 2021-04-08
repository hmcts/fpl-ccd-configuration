package uk.gov.hmcts.reform.fpl.service.orders.prepopulator.section;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.model.order.OrderSection;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.orders.generator.OrderDocumentGenerator;

import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocument;

class DraftOrderPreviewSectionPrePopulatorTest {

    private static final Order ORDER = Order.C32_CARE_ORDER;
    private static final byte[] DOCUMENT_DATA = {1, 2, 3, 4, 5};
    private static final DocmosisDocument DOCUMENT = new DocmosisDocument("some title", DOCUMENT_DATA);
    private static final Document UPLOADED_DOCUMENT = testDocument();
    private static final DocumentReference DOCUMENT_REFERENCE = DocumentReference.buildFromDocument(UPLOADED_DOCUMENT);

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

        when(orderDocumentGenerator.generate(ORDER, caseData, OrderStatus.DRAFT))
            .thenReturn(DOCUMENT);
        when(uploadDocumentService.uploadPDF(DOCUMENT_DATA, "Preview order.pdf (opens in a new tab)"))
            .thenReturn(UPLOADED_DOCUMENT);

        Map<String, Object> actual = underTest.prePopulate(caseData);

        assertThat(actual).isEqualTo(Map.of("orderPreview", DOCUMENT_REFERENCE));
    }
}
