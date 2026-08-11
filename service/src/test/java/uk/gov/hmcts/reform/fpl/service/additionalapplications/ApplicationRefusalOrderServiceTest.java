package uk.gov.hmcts.reform.fpl.service.additionalapplications;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Court;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.CaseDataExtractionService;
import uk.gov.hmcts.reform.fpl.service.DocumentSealingService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.REFUSAL_ORDER;
import static uk.gov.hmcts.reform.fpl.model.document.SealType.ENGLISH;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentWithName;

@ExtendWith(MockitoExtension.class)
class ApplicationRefusalOrderServiceTest {

    private static final String APPLICATION_DATE = "1 January 2021, 12:00pm";
    private static final String DATE_OF_REFUSAL = "29 July 2026";
    private static final String REFUSAL_REASON = "the application has been listed at the next hearing";
    private static final String JUDGE = "District Judge Example";
    private static final byte[] DOC_BYTES = {1, 2, 3};
    private static final byte[] SEALED_DOC_BYTES = {4, 5, 6};

    @Mock
    private CaseDataExtractionService dataService;

    @Mock
    private Time time;

    @Mock
    private DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;

    @Mock
    private UploadDocumentService documentUploadService;

    @Mock
    private DocumentSealingService documentSealingService;

    @InjectMocks
    private ApplicationRefusalOrderService underTest;

    @Test
    void shouldUploadRefusalOrderUsingPdfFileName() {
        stubCommonDependencies(false);

        CaseData caseData = CaseData.builder()
            .court(Court.builder().build())
            .build();

        underTest.buildApplicationRefusalOrderDocument(
            caseData,
            JUDGE,
            DATE_OF_REFUSAL,
            APPLICATION_DATE,
            REFUSAL_REASON,
            true
        );

        verify(documentUploadService).uploadPDF(
            eq(SEALED_DOC_BYTES),
            eq(REFUSAL_ORDER.getFileName())
        );
    }

    @Test
    void shouldKeepGeneratedOrderTitleWithoutPdfSuffix() {
        stubCommonDependencies(true);

        CaseData caseData = CaseData.builder()
            .court(Court.builder().build())
            .build();

        Element<GeneratedOrder> refusalOrder = underTest.buildRefusalOrder(
            caseData,
            JUDGE,
            DATE_OF_REFUSAL,
            APPLICATION_DATE,
            REFUSAL_REASON,
            false
        );

        assertThat(refusalOrder.getValue().getTitle())
            .isEqualTo(REFUSAL_ORDER.getLabel() + " for application date " + APPLICATION_DATE);
        assertThat(refusalOrder.getValue().getDocument()).isNotNull();
        assertThat(refusalOrder.getValue().getDocumentConfidential()).isNull();
    }

    private void stubCommonDependencies(boolean includeTimeStub) {
        when(dataService.getCourtName(any())).thenReturn("Test Court");
        when(dataService.getChildrenDetails(any())).thenReturn(List.of());
        if (includeTimeStub) {
            when(time.now()).thenReturn(LocalDateTime.of(2026, 7, 29, 10, 0));
        }

        DocmosisDocument docmosisDocument = DocmosisDocument.builder()
            .bytes(DOC_BYTES)
            .build();

        when(docmosisDocumentGeneratorService.generateDocmosisDocument(any(DocmosisData.class),
            any(DocmosisTemplates.class))).thenReturn(docmosisDocument);
        when(documentSealingService.sealDocument(eq(DOC_BYTES), any(), eq(ENGLISH))).thenReturn(SEALED_DOC_BYTES);

        Document uploadedDocument = testDocumentWithName("refusal-order.pdf");
        when(documentUploadService.uploadPDF(eq(SEALED_DOC_BYTES), any())).thenReturn(uploadedDocument);
    }
}

