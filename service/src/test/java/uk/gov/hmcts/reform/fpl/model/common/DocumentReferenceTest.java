package uk.gov.hmcts.reform.fpl.model.common;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;

class DocumentReferenceTest {

    @Test
    void shouldBuildFromDocumentWhenValidDocument() throws IOException {
        assertThat(DocumentReference.buildFromDocument(document())).isEqualTo(DocumentReference.builder()
            .url(document().links.self.href)
            .binaryUrl(document().links.binary.href)
            .filename(document().originalDocumentName)
            .build());
    }
}
