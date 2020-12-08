package uk.gov.hmcts.reform.fpl.model;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;

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
    void shouldWrapC2DocumentReferenceAsElementDocumentReference() {
        DocumentReference mainC2DocumentReference = DocumentReference.builder().build();

        C2DocumentBundle c2DocumentBundle = C2DocumentBundle.builder()
            .document(mainC2DocumentReference)
            .build();

        assertThat(c2DocumentBundle.getAllC2DocumentReferences().get(0).getValue()).isEqualTo(mainC2DocumentReference);
    }

    @Test
    void shouldAppendSupportingEvidenceDocumentReferencesToC2DocumentCollection() {
        DocumentReference mainC2DocumentReference = DocumentReference.builder().build();

        DocumentReference supportingDocumentReferenceOne = DocumentReference.builder()
            .filename("test_file_1.doc")
            .build();

        DocumentReference supportingDocumentReferenceTwo = DocumentReference.builder()
            .filename("test_file_1.doc")
            .build();

        SupportingEvidenceBundle supportingEvidenceBundleOne = SupportingEvidenceBundle.builder()
            .document(supportingDocumentReferenceOne)
            .build();

        SupportingEvidenceBundle supportingEvidenceBundleTwo = SupportingEvidenceBundle.builder()
            .document(supportingDocumentReferenceTwo)
            .build();

        C2DocumentBundle c2DocumentBundle = C2DocumentBundle.builder()
            .document(mainC2DocumentReference)
            .supportingEvidenceBundle(List.of(
                element(supportingEvidenceBundleOne),
                element(supportingEvidenceBundleTwo)
            )).build();

        List<Element<DocumentReference>> documentReferences = c2DocumentBundle.getAllC2DocumentReferences();

        assertThat(documentReferences.get(0).getValue()).isEqualTo(mainC2DocumentReference);
        assertThat(documentReferences.get(1).getValue()).isEqualTo(supportingDocumentReferenceOne);
        assertThat(documentReferences.get(2).getValue()).isEqualTo(supportingDocumentReferenceTwo);
    }

    @Test
    void shouldReturnEmptyListWhenC2DocumentReferencesDoNotExist() {
        C2DocumentBundle c2DocumentBundle = C2DocumentBundle.builder().build();

        assertThat(c2DocumentBundle.getAllC2DocumentReferences()).isEmpty();
    }

    @Test
    void shouldReturnMainC2DocumentReferenceFileNameAsString() {
        String fileName = "fileName.doc";
        DocumentReference mainC2DocumentReference = DocumentReference.builder()
            .filename(fileName)
            .build();

        C2DocumentBundle c2DocumentBundle = C2DocumentBundle.builder()
            .document(mainC2DocumentReference)
            .build();

        assertThat(c2DocumentBundle.getC2DocumentFileNames()).isEqualTo(
            String.format("%s", fileName));
    }

    @Test
    void shouldReturnAllRelatedC2DocumentReferenceFileNamesAsString() {
        String c2DocumentFileName = "c2.doc";
        String supportingDocumentOne = "c2_additional_one.doc";
        String supportingDocumentTwo = "c2_additional_two.doc";

        DocumentReference mainC2DocumentReference = DocumentReference.builder()
            .filename(c2DocumentFileName)
            .build();

        SupportingEvidenceBundle supportingEvidenceBundleOne = SupportingEvidenceBundle.builder()
            .document(DocumentReference.builder()
                .filename(supportingDocumentOne)
                .build())
            .build();

        SupportingEvidenceBundle supportingEvidenceBundleTwo = SupportingEvidenceBundle.builder()
            .document(DocumentReference.builder()
                .filename(supportingDocumentTwo)
                .build())
            .build();

        C2DocumentBundle c2DocumentBundle = C2DocumentBundle.builder()
            .document(mainC2DocumentReference)
            .supportingEvidenceBundle(List.of(
                element(supportingEvidenceBundleOne),
                element(supportingEvidenceBundleTwo)
            )).build();

        String stringBuilder = c2DocumentFileName + "\n" + supportingDocumentOne + "\n" + supportingDocumentTwo;

        assertThat(c2DocumentBundle.getC2DocumentFileNames()).isEqualTo(
            stringBuilder);
    }

    @Test
    void shouldReturnEmptyStringIfC2DocumentsDoNotExist() {
        C2DocumentBundle c2DocumentBundle = C2DocumentBundle.builder().build();

        assertThat(c2DocumentBundle.getC2DocumentFileNames()).isEqualTo("");
    }
}
