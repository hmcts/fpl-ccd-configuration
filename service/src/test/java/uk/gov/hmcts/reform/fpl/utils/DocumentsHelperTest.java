package uk.gov.hmcts.reform.fpl.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import uk.gov.hmcts.reform.fpl.enums.DocumentStatus;
import uk.gov.hmcts.reform.fpl.model.common.Document;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.DocumentStatus.ATTACHED;
import static uk.gov.hmcts.reform.fpl.enums.DocumentStatus.INCLUDED_IN_SWET;
import static uk.gov.hmcts.reform.fpl.utils.DocumentsHelper.concatUrlAndMostRecentUploadedDocumentPath;
import static uk.gov.hmcts.reform.fpl.utils.DocumentsHelper.hasDocumentStatusOf;
import static uk.gov.hmcts.reform.fpl.utils.DocumentsHelper.hasDocumentStatusSet;
import static uk.gov.hmcts.reform.fpl.utils.DocumentsHelper.hasDocumentUploaded;
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
        assertThat(hasDocumentUploaded(document(true))).isEqualTo(true);
    }

    @Test
    void shouldReturnFalseIfDocumentHasNotBinaries() {
        assertThat(hasDocumentUploaded(document(false))).isEqualTo(false);
    }

    @Test
    void shouldConcatUrlAndDocumentPath() {
        String invalidMostRecentDocumentLink = "http://dm-store:8080/documents/b28f859b-7521-4c84-9057-47e56afd773f/binary";
        String url = "http://fake-url";

        String documentUrl = concatUrlAndMostRecentUploadedDocumentPath(url, invalidMostRecentDocumentLink);

        assertThat(documentUrl).isEqualTo("http://fake-url/documents/b28f859b-7521-4c84-9057-47e56afd773f/binary");
    }


    @Test
    void shouldCatchExceptionAndReturnEmptyStringWhenDocumentLinkIsNotInCorrectFormat() {
        String invalidMostRecentDocumentLink = "http://dm-store:8080/documents/b28f859b-7521-4c84-9057-47e56afd773f/binary^";
        String url = "http://fake-url";

        String documentUrl = concatUrlAndMostRecentUploadedDocumentPath(url, invalidMostRecentDocumentLink);

        assertThat(documentUrl).isEmpty();
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
