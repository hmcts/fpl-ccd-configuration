package uk.gov.hmcts.reform.fpl.service.cmo;

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
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisApplicationListNextHearingOrder;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisData;
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
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.LIST_AT_NEXT_HEARING_ORDER;
import static uk.gov.hmcts.reform.fpl.model.document.SealType.ENGLISH;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentWithName;

@ExtendWith(MockitoExtension.class)
class ApplicationListNextHearingOrderServiceTest {

    private static final String APPLICATION_DATE = "1 January 2026, 12:00pm";
    private static final String APPLICATION_DATE_ONLY = "1 January 2026";
    private static final String NEXT_HEARING_DATE = "1 August 2026";
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
    private ApplicationListNextHearingOrderService underTest;

    @Test
    void shouldGenerateListNextHearingOrderUsingDedicatedTemplate() {
        stubCommonDependencies(true);

        CaseData caseData = CaseData.builder()
            .court(Court.builder().build())
            .build();

        underTest.buildListAtNextHearingOrder(
            caseData,
            JUDGE,
            APPLICATION_DATE,
            NEXT_HEARING_DATE,
            false
        );

        verify(docmosisDocumentGeneratorService).generateDocmosisDocument(
            argThat((DocmosisApplicationListNextHearingOrder data) -> NEXT_HEARING_DATE.equals(data.getNextHearingDate())),
            eq(DocmosisTemplates.APPLICATION_LIST_NEXT_HEARING)
        );

        verify(documentUploadService).uploadPDF(
            eq(SEALED_DOC_BYTES),
            eq(LIST_AT_NEXT_HEARING_ORDER.getLabel() + " for application date " + APPLICATION_DATE_ONLY + ".pdf")
        );
    }

    @Test
    void shouldSetGeneratedOrderTypeForListNextHearingOrder() {
        stubCommonDependencies(true);

        CaseData caseData = CaseData.builder()
            .court(Court.builder().build())
            .build();

        Element<GeneratedOrder> listedOrder = underTest.buildListAtNextHearingOrder(
            caseData,
            JUDGE,
            APPLICATION_DATE,
            NEXT_HEARING_DATE,
            false
        );

        assertThat(listedOrder.getValue().getType()).isEqualTo(LIST_AT_NEXT_HEARING_ORDER.getLabel());
        assertThat(listedOrder.getValue().getTitle())
            .isEqualTo(LIST_AT_NEXT_HEARING_ORDER.getLabel() + " for application date " + APPLICATION_DATE_ONLY);
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

        Document uploadedDocument = testDocumentWithName("list-next-hearing-order.pdf");
        when(documentUploadService.uploadPDF(eq(SEALED_DOC_BYTES), any())).thenReturn(uploadedDocument);
    }
}



