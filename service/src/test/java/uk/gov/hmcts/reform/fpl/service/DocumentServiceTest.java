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
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisCaseManagementOrder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.CMO;
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
        DocmosisCaseManagementOrder template = DocmosisCaseManagementOrder.builder()
            .draftbackground("Present")
            .build();

        initMocks(template);

        assertThat(service.getDocumentFromDocmosisOrderTemplate(template, CMO)).isEqualTo(DOCUMENT);
        assertThat(captor.getValue()).isEqualTo("draft-case-management-order.pdf");
    }

    @Test
    void shouldReturnDocumentWithIssuedTitleWhenDraftBackgroundIsNull() {
        DocmosisCaseManagementOrder template = DocmosisCaseManagementOrder.builder().build();
        initMocks(template);

        assertThat(service.getDocumentFromDocmosisOrderTemplate(template, CMO)).isEqualTo(DOCUMENT);
        assertThat(captor.getValue()).isEqualTo("case-management-order.pdf");
    }

    private void initMocks(DocmosisCaseManagementOrder template) {
        byte[] bytes = new byte[]{1, 2, 3};

        when(documentGeneratorService.generateDocmosisDocument(template, CMO))
            .thenReturn(DocmosisDocument.builder().bytes(bytes).documentTitle("case-management-order.pdf").build());

        when(uploadDocumentService.uploadPDF(eq(bytes), captor.capture())).thenReturn(DOCUMENT);
    }
}
