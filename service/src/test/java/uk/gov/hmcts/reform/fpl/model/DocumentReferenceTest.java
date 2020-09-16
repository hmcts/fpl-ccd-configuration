package uk.gov.hmcts.reform.fpl.model;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import static org.assertj.core.api.Assertions.assertThat;

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
}
