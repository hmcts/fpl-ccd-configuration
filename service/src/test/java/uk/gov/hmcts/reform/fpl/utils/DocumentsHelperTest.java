package uk.gov.hmcts.reform.fpl.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import uk.gov.hmcts.reform.fpl.enums.DocumentStatus;
import uk.gov.hmcts.reform.fpl.model.common.Document;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.DocumentStatus.ATTACHED;
import static uk.gov.hmcts.reform.fpl.enums.DocumentStatus.INCLUDED_IN_SWET;
import static uk.gov.hmcts.reform.fpl.utils.DocumentsHelper.hasDocumentStatusOf;
import static uk.gov.hmcts.reform.fpl.utils.DocumentsHelper.hasDocumentStatusSet;
import static uk.gov.hmcts.reform.fpl.utils.DocumentsHelper.hasDocumentUploaded;
import static uk.gov.hmcts.reform.fpl.utils.DocumentsHelper.hasExtension;
import static uk.gov.hmcts.reform.fpl.utils.DocumentsHelper.updateExtension;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

class DocumentsHelperTest {

    @ParameterizedTest
    @EnumSource(DocumentStatus.class)
    void shouldReturnTrueIfDocumentHasRequestedStatus(DocumentStatus status) {
        assertThat(hasDocumentStatusOf(document(status), status)).isTrue();
    }

    @Test
    void shouldReturnFalseIfDocumentDoesNotHaveRequestedStatus() {
        assertThat(hasDocumentStatusOf(document(ATTACHED), INCLUDED_IN_SWET)).isFalse();
    }

    @Test
    void shouldReturnTrueIfDocumentHasStatusSet() {
        assertThat(hasDocumentStatusSet(document(ATTACHED))).isTrue();
    }

    @Test
    void shouldReturnFalseIfDocumentHasNoStatusSet() {
        assertThat(hasDocumentStatusSet(document(null))).isFalse();
    }

    @Test
    void shouldReturnTrueIfDocumentHasBinaries() {
        assertThat(hasDocumentUploaded(document(true))).isTrue();
    }

    @Test
    void shouldReturnFalseIfDocumentHasNotBinaries() {
        assertThat(hasDocumentUploaded(document(false))).isFalse();
    }

    @Test
    void shouldReturnTrueWhenFilenameExtensionIncludesExpectedExtension() {
        DocumentReference documentReference = buildDocumentReferenceWithExtension("test.pdf");
        assertThat(hasExtension(documentReference, "pdf")).isTrue();
    }

    @Test
    void shouldReturnTrueWhenFilenameExtensionIncludesExpectedExtensionWithDifferentCase() {
        DocumentReference documentReference = buildDocumentReferenceWithExtension("test.PDF");
        assertThat(hasExtension(documentReference, "pdf")).isTrue();
    }

    @Test
    void shouldReturnFalseWhenFilenameExtensionDoesNotIncludeExpectedExtension() {
        DocumentReference documentReference = buildDocumentReferenceWithExtension("test.doc");
        assertThat(hasExtension(documentReference, "pdf")).isFalse();
    }

    @Test
    void shouldUpdateFilenameWithDocExtensionToPDF() {
        DocumentReference documentReference = buildDocumentReferenceWithExtension("test.doc");

        assertThat(updateExtension(documentReference.getFilename(), "pdf")).isEqualTo("test.pdf");
    }

    @Test
    void shouldPersistCurrentPdfExtension() {
        DocumentReference documentReference = buildDocumentReferenceWithExtension("test.pdf");

        assertThat(updateExtension(documentReference.getFilename(), "pdf")).isEqualTo("test.pdf");
    }

    private DocumentReference buildDocumentReferenceWithExtension(String filename) {
        return DocumentReference.builder().filename(filename).build();
    }

    private static Document document(DocumentStatus documentStatus) {
        return Document
            .builder()
            .documentStatus(Optional.ofNullable(documentStatus).map(DocumentStatus::getLabel).orElse(null))
            .build();
    }

    private static Document document(boolean withBinaries) {
        return Document
            .builder()
            .typeOfDocument(withBinaries ? testDocumentReference() : null)
            .build();
    }
}
