package uk.gov.hmcts.reform.fpl.service.document;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentBundleView;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentView;
import uk.gov.hmcts.reform.fpl.service.CaseUrlService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.ResourceReader.readString;

@ExtendWith(MockitoExtension.class)
class DocumentsListRendererTest {

    private static final String IMAGE_BASE_URL
        = "https://raw.githubusercontent.com/hmcts/fpl-ccd-configuration/master/resources/";

    private final CaseUrlService caseUrlService = mock(CaseUrlService.class);

    private final DocumentsListRenderer underTest = new DocumentsListRenderer(IMAGE_BASE_URL, caseUrlService);

    @Test
    void shouldRenderDocumentBundles() {
        List<DocumentBundleView> documentBundleViews = List.of(
            DocumentBundleView.builder()
                .name("Applicant's statements and application documents")
                .documents(List.of(DocumentView.builder()
                        .type("Application statement")
                        .fileName("supportingDoc1 conf hmcts")
                        .documentName("supportingDoc1 conf hmcts")
                        .uploadedBy("user@test.com")
                        .uploadedAt("2:22pm, 4 May 2021")
                        .confidential(false)
                        .document(DocumentReference.builder()
                            .filename("document1.docx").url("fake-url.com").binaryUrl("test.com").build())
                        .title("Application statement")
                        .includeDocumentName(true)
                        .build(),
                    DocumentView.builder()
                        .type("SWET")
                        .uploadedBy("user1@test.com")
                        .includedInSWET("SWET update")
                        .uploadedAt("1:15pm, 3 May 2021")
                        .includeSWETField(true)
                        .document(DocumentReference.builder()
                            .filename("swet-doc.docx").url("fake-url.com").binaryUrl("test.com").build())
                        .title("SWET")
                        .build()))
                .build(),
            DocumentBundleView.builder()
                .name("Respondent 1 statements")
                .documents(List.of(DocumentView.builder()
                    .title("Email with evidence attached")
                    .documentName("Email with evidence attached")
                    .fileName("respondent-document.docx")
                    .confidential(true)
                    .type("Respondent 1 statements")
                    .document(DocumentReference.builder()
                        .filename("respondent-document.docx").url("fake-url.com").binaryUrl("test.com").build())
                    .uploadedBy("HMCTS")
                    .uploadedAt("10:00am, 1 May 2021")
                    .build()))
                .build());

        String expectedDocumentView = readString("further-evidence-documents-tab/expected-documents-view.md").trim();

        when(caseUrlService.getBaseUrl()).thenReturn(IMAGE_BASE_URL);
        assertThat(underTest.render(documentBundleViews)).isEqualTo(expectedDocumentView);
    }
}
