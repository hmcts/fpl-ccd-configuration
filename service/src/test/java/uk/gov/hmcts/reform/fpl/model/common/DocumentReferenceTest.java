package uk.gov.hmcts.reform.fpl.model.common;

import java.time.Instant;
import java.util.Date;

import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.SecureDocumentManagementStoreLoader.document;

class DocumentReferenceTest {

    @Test
    void shouldBuildFromDocumentWhenValidDocument() {
        assertThat(DocumentReference.buildFromDocument(document())).isEqualTo(DocumentReference.builder()
            .url(document().links.self.href)
            .binaryUrl(document().links.binary.href)
            .filename(document().originalDocumentName)
            .createdOn(new DateTime("2017-11-01T10:23:55.271+00:00").toDate())
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
