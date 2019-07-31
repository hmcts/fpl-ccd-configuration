package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.OldApplicant;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
class ApplicantMigrationServiceTest {
    private final ApplicantMigrationService service = new ApplicantMigrationService();

    @Test
    void shouldReturnValidationErrorsIfNewApplicantContainsPBANumberWithSixDigits() {
        CaseData caseData = CaseData.builder()
            .applicants(
                ImmutableList.of(Element.<Applicant>builder()
                    .value(
                        Applicant.builder()
                            .party(ApplicantParty.builder()
                                .pbaNumber("123456")
                                .build())
                            .build())
                    .build()))
            .build();

        List<String> errors = service.validatePBANumbers(caseData);

        assertThat(errors.size()).isEqualTo(1);
        assertThat(errors.get(0)).isEqualTo("Payment by account (PBA) number must include 7 numbers");
    }

    @Test
    void shouldReturnValidationErrorsIfNewApplicantContainsPBANumberWithEightDigits() {
        CaseData caseData = CaseData.builder()
            .applicants(
                ImmutableList.of(Element.<Applicant>builder()
                    .value(
                        Applicant.builder()
                            .party(ApplicantParty.builder()
                                .pbaNumber("12345678")
                                .build())
                            .build())
                    .build()))
            .build();

        List<String> errors = service.validatePBANumbers(caseData);

        assertThat(errors.size()).isEqualTo(1);
        assertThat(errors.get(0)).isEqualTo("Payment by account (PBA) number must include 7 numbers");
    }

    @Test
    void shouldNotReturnErrorsIfNewApplicantPBANumberIsValid() {
        CaseData caseData = CaseData.builder()
            .applicants(
                ImmutableList.of(Element.<Applicant>builder()
                    .value(
                        Applicant.builder()
                            .party(ApplicantParty.builder()
                                .pbaNumber("PBA1234567")
                                .build())
                            .build())
                    .build()))
            .build();

        List<String> errors = service.validatePBANumbers(caseData);

        assertThat(errors.size()).isEqualTo(0);
    }

    @Test
    void shouldReturnValidationErrorsIfOlApplicantContainsPBANumberWithSixDigits() {
        CaseData caseData = CaseData.builder()
            .applicant(OldApplicant.builder()
                .pbaNumber("123456")
                .build())
            .build();

        List<String> errors = service.validatePBANumbers(caseData);

        assertThat(errors.size()).isEqualTo(1);
        assertThat(errors.get(0)).isEqualTo("Payment by account (PBA) number must include 7 numbers");
    }

    @Test
    void shouldReturnValidationErrorsIfOlApplicantContainsPBANumberWithEightDigits() {
        CaseData caseData = CaseData.builder()
            .applicant(OldApplicant.builder()
                .pbaNumber("12345678")
                .build())
            .build();

        List<String> errors = service.validatePBANumbers(caseData);

        assertThat(errors.size()).isEqualTo(1);
        assertThat(errors.get(0)).isEqualTo("Payment by account (PBA) number must include 7 numbers");
    }

    @Test
    void shouldNotReturnErrorsIfOldApplicantPBANumberIsValid() {
        CaseData caseData = CaseData.builder()
            .applicant(OldApplicant.builder()
                .pbaNumber("PBA1234567")
                .build())
            .build();

        List<String> errors = service.validatePBANumbers(caseData);

        assertThat(errors.size()).isEqualTo(0);
    }

    @Test
    void shouldReturnNoErrorsIfCaseDataDoesNotContainApplicants() {
        CaseData caseData = CaseData.builder().build();
        List<String> errors = service.validatePBANumbers(caseData);

        assertThat(errors.size()).isEqualTo(0);
    }

    @SuppressWarnings("unchecked")
    @Test
    void ShouldAppendPBANToNewApplicantWithPBANumber() {
        List<Element<Applicant>> applicants = ImmutableList.of(
            Element.<Applicant>builder()
                .id(UUID.randomUUID())
                .value(Applicant.builder()
                    .party(ApplicantParty.builder()
                        .pbaNumber("1234567")
                        .build())
                    .build())
                .build()
        );

        CaseDetails caseDetails = CaseDetails.builder()
            .data(ImmutableMap.of(
                "applicants", applicants,
                "applicant", OldApplicant.builder().build(),
                "applicantsMigrated", ""
            ))
            .build();

        CaseDetails updatedCaseDetails = service.updatePBANumbers(caseDetails);
        List<Element<Applicant>> updatedApplicants = (List<Element<Applicant>>) updatedCaseDetails.getData().get("applicants");

        assertThat(updatedApplicants.get(0).getValue().getParty().getPbaNumber()).isEqualTo("PBA1234567");
    }

    @SuppressWarnings("unchecked")
    @Test
    void ShouldAppendPBANToOldApplicantWithPBANumber() {
        OldApplicant oldApplicant = OldApplicant.builder().pbaNumber("1234567").build();

        CaseDetails caseDetails = CaseDetails.builder()
            .data(ImmutableMap.of(
                "applicant", oldApplicant
            ))
            .build();

        CaseDetails updatedCaseDetails = service.updatePBANumbers(caseDetails);
        OldApplicant updatedApplicant = (OldApplicant) updatedCaseDetails.getData().get("applicant");

        assertThat(updatedApplicant.getPbaNumber()).isEqualTo("PBA1234567");
    }
}
