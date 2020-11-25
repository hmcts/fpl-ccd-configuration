package uk.gov.hmcts.reform.fpl.model.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;

class DocumentReferenceTest {

    @Test
    void shouldBuildFromDocumentWhenValidDocument() {
        assertThat(DocumentReference.buildFromDocument(document())).isEqualTo(DocumentReference.builder()
            .url(document().links.self.href)
            .binaryUrl(document().links.binary.href)
            .filename(document().originalDocumentName)
            .build());
    }

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
}
