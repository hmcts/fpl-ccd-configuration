package uk.gov.hmcts.reform.fpl.model;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.DocumentExtension.PDF;

class DocumentReferenceTest {
    @Test
    void shouldReturnTrueIfDocumentReferenceIsEmpty() {
        DocumentReference documentReference = DocumentReference.builder().build();
        assertThat(documentReference.isEmpty()).isTrue();
    }

    @Test
    void shouldReturnFalseIfDocumentReferenceIsFullyPopulated() {
        DocumentReference documentReference = DocumentReference.builder()
            .filename("file.pdf")
            .url("http://test")
            .binaryUrl("http://test")
            .build();

        assertThat(documentReference.isEmpty()).isFalse();
    }

    @Test
    void shouldReturnFalseIfDocumentReferenceIsPartiallyEmpty() {
        DocumentReference documentReference = DocumentReference.builder()
            .filename("file.pdf")
            .binaryUrl("http://test")
            .build();

        assertThat(documentReference.isEmpty()).isFalse();
    }

    @Test
    void shouldReturnTrueWhenFilenameExtensionIncludesExpectedExtension() {
        DocumentReference documentReference = buildDocumentReferenceWithExtension(PDF.getLabel());
        assertThat(documentReference.hasExtensionTypeOf(PDF)).isTrue();
    }

    @Test
    void shouldReturnFalseWhenFilenameExtensionDoesNotIncludeExpectedExtension() {
        DocumentReference documentReference = buildDocumentReferenceWithExtension("doc");
        assertThat(documentReference.hasExtensionTypeOf(PDF)).isFalse();
    }

    private DocumentReference buildDocumentReferenceWithExtension(String documentExtension) {
        return DocumentReference.builder().filename("test." + documentExtension).build();
    }
}
