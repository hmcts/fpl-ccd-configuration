package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisOrder;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisDocumentGeneratorService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.ORDER_V2;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;

@ExtendWith(SpringExtension.class)
class DocumentServiceTest {
    private static final Document DOCUMENT = document();

    @Mock
    private DocmosisDocumentGeneratorService documentGeneratorService;

    @Mock
    private UploadDocumentService uploadDocumentService;

    @Captor
    private ArgumentCaptor<String> captor;

    @InjectMocks
    private DocumentService service;

    @Test
    void shouldReturnDocumentWithDraftTitleWhenDraftBackgroundIsPresent() {
        DocmosisOrder template = DocmosisOrder.builder()
            .draftbackground("Present")
            .build();

        initMocks(template);

        assertThat(service.getDocumentFromDocmosisOrderTemplate(template, ORDER_V2)).isEqualTo(DOCUMENT);
        assertThat(captor.getValue()).isEqualTo("draft-order.pdf");
    }

    @Test
    void shouldReturnDocumentWithIssuedTitleWhenDraftBackgroundIsNull() {
        DocmosisOrder template = DocmosisOrder.builder().build();
        initMocks(template);

        assertThat(service.getDocumentFromDocmosisOrderTemplate(template, ORDER_V2)).isEqualTo(DOCUMENT);
        assertThat(captor.getValue()).isEqualTo("order.pdf");
    }

    private void initMocks(DocmosisOrder template) {
        byte[] bytes = new byte[] {1, 2, 3};

        when(documentGeneratorService.generateDocmosisDocument(template, ORDER_V2))
            .thenReturn(DocmosisDocument.builder().bytes(bytes).documentTitle("order.pdf").build());

        when(uploadDocumentService.uploadPDF(eq(bytes), captor.capture())).thenReturn(DOCUMENT);
    }
}
