package uk.gov.hmcts.reform.fpl.model;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

class C2DocumentBundleTest {
    @Test
    void shouldFormatC2DocumentBundleToLabel() {
        C2DocumentBundle c2DocumentBundle = C2DocumentBundle.builder()
            .uploadedDateTime("1st June 2019")
            .build();

        String label = c2DocumentBundle.toLabel(1);

        assertThat(label).isEqualTo("Application 1: 1st June 2019");
    }

    @Test
    void shouldBuildMainC2DocumentReferenceAsFormattedString() {
        DocumentReference mainC2DocumentReference = DocumentReference.builder().build();

        C2DocumentBundle c2DocumentBundle = C2DocumentBundle.builder()
            .document(mainC2DocumentReference)
            .build();

        assertThat(c2DocumentBundle.getC2DocumentBundleDocumentReferencesAsString()).isEqualTo(
            String.format("%s", mainC2DocumentReference));
    }

    @Test
    void shouldBuildAllSupportingEvidenceBundleDocumentReferenceAsFormattedString() {
        DocumentReference mainC2DocumentReference = DocumentReference.builder().build();

        SupportingEvidenceBundle supportingEvidenceBundleOne = SupportingEvidenceBundle.builder()
            .document(DocumentReference.builder()
                .filename("test_file_1.doc")
                .build())
            .build();

        SupportingEvidenceBundle supportingEvidenceBundleTwo = SupportingEvidenceBundle.builder()
            .document(DocumentReference.builder()
                .filename("test_file_2.doc")
                .build())
            .build();

        C2DocumentBundle c2DocumentBundle = C2DocumentBundle.builder()
            .document(mainC2DocumentReference)
            .supportingEvidenceBundle(List.of(
                element(supportingEvidenceBundleOne),
                element(supportingEvidenceBundleTwo)
            )).build();

        String stringBuilder = mainC2DocumentReference + "\n" + supportingEvidenceBundleOne.getDocument()
            + "\n" + supportingEvidenceBundleTwo.getDocument();

        assertThat(c2DocumentBundle.getC2DocumentBundleDocumentReferencesAsString()).isEqualTo(
            stringBuilder);
    }

    @Test
    void shouldReturnEmptyStringIfC2DocumentsDoNotExist() {
        C2DocumentBundle c2DocumentBundle = C2DocumentBundle.builder().build();

        assertThat(c2DocumentBundle.getC2DocumentBundleDocumentReferencesAsString()).isEqualTo("");
    }
}
