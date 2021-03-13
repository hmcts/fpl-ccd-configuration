package uk.gov.hmcts.reform.fpl.model;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.OtherApplicationsBundle;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

class OtherApplicationsBundleTest {

    @Test
    void shouldWrapOtherDocumentReferenceAsElementDocumentReference() {
        DocumentReference mainDocumentReference = DocumentReference.builder().build();

        OtherApplicationsBundle otherApplicationsBundle = OtherApplicationsBundle.builder()
            .document(mainDocumentReference)
            .build();

        assertThat(otherApplicationsBundle.getAllDocumentReferences().get(0).getValue())
            .isEqualTo(mainDocumentReference);
    }

    @Test
    void shouldAppendSupportingEvidenceDocumentReferencesToOtherApplicationDocumentCollection() {
        DocumentReference mainDocumentReference = DocumentReference.builder().build();

        DocumentReference supportingDocumentReference1 = DocumentReference.builder().filename("file1.doc").build();
        DocumentReference supportingDocumentReference2 = DocumentReference.builder().filename("file2.doc").build();

        SupportingEvidenceBundle supportingEvidenceBundle1 = SupportingEvidenceBundle.builder()
            .document(supportingDocumentReference1).build();

        SupportingEvidenceBundle supportingEvidenceBundle2 = SupportingEvidenceBundle.builder()
            .document(supportingDocumentReference2).build();

        OtherApplicationsBundle otherApplicationsDocumentBundle = OtherApplicationsBundle.builder()
            .document(mainDocumentReference)
            .supportingEvidenceBundle(List.of(element(supportingEvidenceBundle1), element(supportingEvidenceBundle2)))
            .build();

        List<Element<DocumentReference>> documentReferences
            = otherApplicationsDocumentBundle.getAllDocumentReferences();

        assertThat(documentReferences.get(0).getValue()).isEqualTo(mainDocumentReference);
        assertThat(documentReferences.get(1).getValue()).isEqualTo(supportingDocumentReference1);
        assertThat(documentReferences.get(2).getValue()).isEqualTo(supportingDocumentReference2);
    }

    @Test
    void shouldReturnEmptyListWhenDocumentReferencesDoNotExist() {
        OtherApplicationsBundle applicationsDocumentBundle = OtherApplicationsBundle.builder().build();

        assertThat(applicationsDocumentBundle.getAllDocumentFileNames()).isEmpty();
    }

    @Test
    void shouldReturnMainDocumentReferenceFileNameAsString() {
        String fileName = "fileNameOne.doc";
        DocumentReference mainDocumentReference = DocumentReference.builder().filename(fileName).build();

        OtherApplicationsBundle otherApplicationsBundle = OtherApplicationsBundle.builder()
            .document(mainDocumentReference).build();

        assertThat(otherApplicationsBundle.getAllDocumentFileNames()).isEqualTo(String.format("%s", fileName));
    }

    @Test
    void shouldReturnAllRelatedOtherApplicationsBundleDocumentReferenceFileNamesAsString() {
        String documentFileName = "document1.doc";
        String supportingDocument1 = "supportingDocument1.doc";
        String supportingDocument2 = "supportingDocument2.doc";

        DocumentReference mainDocumentReference = DocumentReference.builder()
            .filename(documentFileName)
            .build();

        SupportingEvidenceBundle supportingEvidenceBundle1 = SupportingEvidenceBundle.builder()
            .document(DocumentReference.builder().filename(supportingDocument1).build())
            .build();

        SupportingEvidenceBundle supportingEvidenceBundle2 = SupportingEvidenceBundle.builder()
            .document(DocumentReference.builder().filename(supportingDocument2).build())
            .build();

        OtherApplicationsBundle otherApplicationsDocumentBundle = OtherApplicationsBundle.builder()
            .document(mainDocumentReference)
            .supportingEvidenceBundle(List.of(
                element(supportingEvidenceBundle1),
                element(supportingEvidenceBundle2)
            )).build();

        String stringBuilder = documentFileName + "\n" + supportingDocument1 + "\n" + supportingDocument2;

        assertThat(otherApplicationsDocumentBundle.getAllDocumentFileNames()).isEqualTo(stringBuilder);
    }

    @Test
    void shouldReturnEmptyStringIfOtherApplicationBundleDocumentsDoNotExist() {
        OtherApplicationsBundle otherApplicationsBundle = OtherApplicationsBundle.builder().build();
        assertThat(otherApplicationsBundle.getAllDocumentFileNames()).isEmpty();
    }

    @Test
    void shouldReturnOnlyNonConfidentialSupportingDocumentsForSupportingEvidenceDocumentsNC() {
        Element<SupportingEvidenceBundle> supportingEvidenceBundle1 = createSupportingEvidenceBundle();
        Element<SupportingEvidenceBundle> supportingEvidenceBundle2
            = createConfidentialSupportingEvidenceBundle("HMCTS");

        OtherApplicationsBundle otherApplicationsBundle = buildOtherApplicationsBundle(
            List.of(supportingEvidenceBundle1, supportingEvidenceBundle2));

        assertThat(otherApplicationsBundle.getSupportingEvidenceNC()).containsExactly(supportingEvidenceBundle1);
    }

    private Element<SupportingEvidenceBundle> createConfidentialSupportingEvidenceBundle(String uploadedBy) {
        return element(SupportingEvidenceBundle.builder()
            .document(DocumentReference.builder().filename("file2.doc").build())
            .confidential(List.of("CONFIDENTIAL"))
            .uploadedBy(uploadedBy)
            .build());
    }

    @Test
    void shouldReturnOnlyNonConfidentialSupportingDocumentsUploadedByHmctsForSupportingEvidenceDocumentsLA() {
        Element<SupportingEvidenceBundle> supportingEvidenceBundle1 = createSupportingEvidenceBundle();
        Element<SupportingEvidenceBundle> supportingEvidenceBundle2
            = createConfidentialSupportingEvidenceBundle("HMCTS");
        Element<SupportingEvidenceBundle> supportingEvidenceBundle3
            = createConfidentialSupportingEvidenceBundle("Other user");

        OtherApplicationsBundle otherApplicationsBundle = buildOtherApplicationsBundle(
            List.of(supportingEvidenceBundle1, supportingEvidenceBundle2, supportingEvidenceBundle3));

        assertThat(otherApplicationsBundle.getSupportingEvidenceLA())
            .containsExactly(supportingEvidenceBundle1, supportingEvidenceBundle3);
    }

    private Element<SupportingEvidenceBundle> createSupportingEvidenceBundle() {
        return element(SupportingEvidenceBundle.builder()
            .document(DocumentReference.builder().filename("file1.doc").build())
            .build());
    }

    private OtherApplicationsBundle buildOtherApplicationsBundle(
        List<Element<SupportingEvidenceBundle>> supportingEvidenceBundle) {

        return OtherApplicationsBundle.builder()
            .supportingEvidenceBundle(supportingEvidenceBundle)
            .document(DocumentReference.builder().build())
            .build();
    }

}
