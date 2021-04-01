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
