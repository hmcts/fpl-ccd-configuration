package uk.gov.hmcts.reform.fpl.service.orders;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.document.DocumentGenerator;
import uk.gov.hmcts.reform.fpl.service.orders.generator.DocmosisParameterGenerator;
import uk.gov.hmcts.reform.fpl.service.orders.generator.OrderDocumentGeneratorHolder;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;
import static uk.gov.hmcts.reform.fpl.enums.docmosis.RenderFormat.PDF;
import static uk.gov.hmcts.reform.fpl.model.order.Order.A70_PLACEMENT_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C21_BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocument;

@ExtendWith(MockitoExtension.class)
class OrderNotificationDocumentServiceTest {

    private static final DocmosisParameterGenerator TEST_PARAMETER_GENERATOR = mock(DocmosisParameterGenerator.class);
    private static final DocmosisDocument TEST_GENERATED_DOCUMENT = DocmosisDocument.builder()
        .bytes(new byte[]{1, 2, 3})
        .documentTitle("generatedDoc.pdf")
        .build();
    private static final Document TEST_UPLOADED_DOCUMENT = testDocument();

    @Mock
    private UploadDocumentService uploadService;

    @Mock
    private DocumentGenerator documentGenerator;

    @Mock
    private OrderDocumentGeneratorHolder orderDocumentGeneratorHolder;

    @InjectMocks
    private OrderNotificationDocumentService underTest;

    @BeforeEach
    void setUp() {
        when(orderDocumentGeneratorHolder.getNotificationDocumentParameterGeneratorByOrderType(any()))
            .thenReturn(Optional.empty());
    }

    @Test
    void shouldCreateUploadAndReturnNotificationDocumentWhenOrderIsRegisteredForIt() {
        when(orderDocumentGeneratorHolder.getNotificationDocumentParameterGeneratorByOrderType(A70_PLACEMENT_ORDER))
            .thenReturn(Optional.of(TEST_PARAMETER_GENERATOR));
        CaseData caseData = createCaseDataWithSelectedOrderType(A70_PLACEMENT_ORDER);
        when(documentGenerator.generateDocument(caseData, TEST_PARAMETER_GENERATOR, PDF, SEALED))
            .thenReturn(TEST_GENERATED_DOCUMENT);
        when(uploadService.uploadPDF(TEST_GENERATED_DOCUMENT.getBytes(), TEST_GENERATED_DOCUMENT.getDocumentTitle()))
            .thenReturn(TEST_UPLOADED_DOCUMENT);

        Optional<DocumentReference> returnedDocument = underTest.createNotificationDocument(caseData);

        assertThat(returnedDocument).isPresent().hasValue(DocumentReference.buildFromDocument(TEST_UPLOADED_DOCUMENT));
        verify(uploadService).uploadPDF(TEST_GENERATED_DOCUMENT.getBytes(), TEST_GENERATED_DOCUMENT.getDocumentTitle());
    }

    @Test
    void shouldNotCreateUploadOrReturnNotificationDocumentWhenOrderNotIsRegisteredForIt() {
        Optional<DocumentReference> returnedDocument =
            underTest.createNotificationDocument(createCaseDataWithSelectedOrderType(C21_BLANK_ORDER));

        assertThat(returnedDocument).isEmpty();
        verifyNoInteractions(documentGenerator, uploadService);
    }

    private CaseData createCaseDataWithSelectedOrderType(Order ordersType) {
        return CaseData.builder().manageOrdersEventData(
            ManageOrdersEventData.builder()
                .manageOrdersType(ordersType)
                .build()
        ).build();
    }

}
