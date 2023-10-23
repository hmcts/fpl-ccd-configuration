package uk.gov.hmcts.reform.fpl.model.common;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.exceptions.removaltool.MissingApplicationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AdditionalApplicationsBundleTest {

    private static final String APPLICANT_NAME = "John Smith";

    @Test
    void shouldThrowExceptionIfNoC2OrOtherBundleInApplication() {
        AdditionalApplicationsBundle bundle = AdditionalApplicationsBundle.builder()
            .build();

        assertThrows(MissingApplicationException.class, bundle::getApplicantName);
    }

    @Test
    void shouldGetApplicantNameFromC2Bundle() {
        AdditionalApplicationsBundle bundle = AdditionalApplicationsBundle.builder()
            .c2DocumentBundle(C2DocumentBundle.builder()
                .applicantName(APPLICANT_NAME)
                .build())
            .build();

        assertThat(bundle.getApplicantName()).isEqualTo(APPLICANT_NAME);
    }

    @Test
    void shouldGetApplicantNameFromOtherBundle() {
        AdditionalApplicationsBundle bundle = AdditionalApplicationsBundle.builder()
            .otherApplicationsBundle(OtherApplicationsBundle.builder()
                .applicantName(APPLICANT_NAME)
                .build())
            .build();

        assertThat(bundle.getApplicantName()).isEqualTo(APPLICANT_NAME);
    }

}
