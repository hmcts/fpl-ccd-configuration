package uk.gov.hmcts.reform.fpl.service.document;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentBundleView;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentView;
import uk.gov.hmcts.reform.fpl.service.CaseUrlService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.ResourceReader.readString;

@ExtendWith(MockitoExtension.class)
class DocumentsListRendererTest {

    static final String IMAGE_BASE_URL = "http://fake-url";

    @Mock
    private CaseUrlService caseUrlService;

    private DocumentsListRenderer underTest = new DocumentsListRenderer(IMAGE_BASE_URL, caseUrlService);

    @Test
    @Disabled
    void shouldRenderDocumentBundles() {
        List<DocumentBundleView> documentBundleViews = List.of(
            DocumentBundleView.builder()
                .name("Application Documents")
                .documents(List.of(DocumentView.builder()
                    .title("Email with evidence attached")
                    .documentName("Email with evidence attached")
                    .fileName("respondent-document.docx")
                    .confidential(true)
                    .type("Respondent 1 statements")
                    .document(DocumentReference.builder()
                        .filename("respondent-document.docx").url("fake-url.com").binaryUrl("test.com").build())
                    .build()))
                .build());

        String expectedDocumentView = readString("further-evidence-documents-tab/expected-documents-view.md").trim();

        when(caseUrlService.getBaseUrl()).thenReturn(IMAGE_BASE_URL);
        assertThat(underTest.render(documentBundleViews)).isEqualTo(expectedDocumentView);
    }
}
