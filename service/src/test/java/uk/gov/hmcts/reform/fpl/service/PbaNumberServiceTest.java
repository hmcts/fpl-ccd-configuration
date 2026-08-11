package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.PBAPayment;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

class PbaNumberServiceTest {

    private static final String VALIDATION_ERROR_MESSAGE =
        "Payment by account (PBA) number must include 7 numbers and the PBA prefix";
    private static final String PBA_PREFIX = "PBA";
    private static final String FIRST_NUMBER = "1234567";
    private static final String SECOND_NUMBER = "9876543";

    private PbaNumberService pbaNumberService = new PbaNumberService();

    @Test
    void shouldUpdateForApplicants() {
        List<Element<Applicant>> applicantElementsList = List.of(
            buildApplicantElementWithPbaNumber(""),
            buildApplicantElementWithPbaNumber(null),
            buildApplicantElementWithPbaNumber(FIRST_NUMBER),
            buildApplicantElementWithPbaNumber(SECOND_NUMBER)
        );

        var updatedApplicants = pbaNumberService.update(applicantElementsList);

        assertThat(updatedApplicants.get(0)).isEqualTo(applicantElementsList.get(0));
        assertThat(updatedApplicants.get(1)).isEqualTo(applicantElementsList.get(1));

        assertThat(getPbaNumberAtIndex(updatedApplicants, 2)).isEqualTo(PBA_PREFIX + FIRST_NUMBER);
        assertThat(getPbaNumberAtIndex(updatedApplicants, 3)).isEqualTo(PBA_PREFIX + SECOND_NUMBER);

        assertThat(updatedApplicants.get(2).getId()).isEqualTo(applicantElementsList.get(2).getId());
        assertThat(updatedApplicants.get(3).getId()).isEqualTo(applicantElementsList.get(3).getId());
    }

    @Test
    void shouldUpdateForC2DocumentBundle() {
        C2DocumentBundle testDocumentBundle = C2DocumentBundle.builder().pbaNumber(FIRST_NUMBER).build();
        C2DocumentBundle expectedUpdatedDocumentBundle = testDocumentBundle.toBuilder()
            .pbaNumber(PBA_PREFIX + FIRST_NUMBER)
            .build();

        var updatedC2DocumentBundle = pbaNumberService.update(testDocumentBundle);

        assertThat(updatedC2DocumentBundle).isEqualTo(expectedUpdatedDocumentBundle);
    }

    @Test
    void shouldUpdatePbaNumberToIncludePrefix() {
        String pbaNumber = "1234567";

        String updatedPbaNumber = pbaNumberService.update(pbaNumber);

        assertThat(updatedPbaNumber).isEqualTo("PBA1234567");
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldReturnNullWhenEmptyPbaNumber(String pbaNumber) {
        assertThat(pbaNumberService.update(pbaNumber)).isNull();
    }

    @Test
    void shouldUpdatePbaPaymentPbaNumberToIncludePrefix() {
        String pbaNumber = "1234567";
        PBAPayment pbaPayment = PBAPayment.builder().pbaNumber(pbaNumber).build();

        PBAPayment updatedPbaNumber = pbaNumberService.updatePBAPayment(pbaPayment);

        assertThat(updatedPbaNumber).isEqualTo(PBAPayment.builder().pbaNumber("PBA1234567").build());
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldUpdatePbaPaymentPbaNumberToIncludePrefix(String pbaNumber) {
        PBAPayment pbaPayment = PBAPayment.builder().pbaNumber(pbaNumber).build();

        PBAPayment updatedPbaNumber = pbaNumberService.updatePBAPayment(pbaPayment);

        assertThat(updatedPbaNumber).isNull();
    }

    @Test
    void shouldReturnNoErrorsWhenPBAPaymentHasValidPbaNumber() {
        PBAPayment pbaPayment = PBAPayment.builder().pbaNumber("PBA1234567").build();
        List<String> errors = pbaNumberService.validate(pbaPayment);

        assertThat(errors).isEmpty();
    }

    @Test
    void shouldReturnErrorWhenPBAPaymentHasInvalidPbaNumber() {
        PBAPayment pbaPayment = PBAPayment.builder().pbaNumber("1").build();
        List<String> errors = pbaNumberService.validate(pbaPayment);

        assertThat(errors).containsExactly(VALIDATION_ERROR_MESSAGE);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldReturnEmptyListWhenPBAPaymentHasNullPbaNumber(String pbaNumber) {
        assertThat(pbaNumberService.validate(PBAPayment.builder().pbaNumber(pbaNumber).build())).isEmpty();
    }

    @Test
    void shouldReturnNoErrorsWhenValidPbaNumber() {
        String pbaNumber = "PBA1234567";

        List<String> errors = pbaNumberService.validate(pbaNumber);

        assertThat(errors).isEmpty();
    }

    @Test
    void shouldReturnErrorWhenInvalidPbaNumber() {
        String pbaNumber = "1";

        List<String> errors = pbaNumberService.validate(pbaNumber);

        assertThat(errors).containsExactly(VALIDATION_ERROR_MESSAGE);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldReturnEmptyListWhenPbaNumberIsNull(String pbaNumber) {
        assertThat(pbaNumberService.validate(pbaNumber)).isEmpty();
    }

    @Test
    void shouldNotUpdateForC2DocumentBundleWithEmptyPbaNumber() {
        C2DocumentBundle testDocumentBundle = C2DocumentBundle.builder().pbaNumber("").build();

        var updatedC2DocumentBundle = pbaNumberService.update(testDocumentBundle);

        assertThat(updatedC2DocumentBundle).isEqualTo(testDocumentBundle);
    }

    @Test
    void shouldReturnErrorMessageForApplicantsWithInvalidPbaNumber() {
        List<Element<Applicant>> applicantElementsList = List.of(
            buildApplicantElementWithPbaNumber(FIRST_NUMBER),
            buildApplicantElementWithPbaNumber(PBA_PREFIX + SECOND_NUMBER)
        );

        List<String> errors = pbaNumberService.validate(applicantElementsList);

        assertThat(errors).containsExactly(VALIDATION_ERROR_MESSAGE);
    }

    @Test
    void shouldNotReturnErrorMessageForApplicantsWithValidPbaNumbers() {
        List<Element<Applicant>> applicantElementsList = List.of(
            buildApplicantElementWithPbaNumber(PBA_PREFIX + FIRST_NUMBER),
            buildApplicantElementWithPbaNumber(PBA_PREFIX + SECOND_NUMBER)
        );

        List<String> errors = pbaNumberService.validate(applicantElementsList);

        assertThat(errors).isEmpty();
    }

    @Test
    void shouldNotReturnErrorMessageForApplicantsWithEmptyPbaNumbers() {
        List<Element<Applicant>> applicantElementsList = List.of(
            buildApplicantElementWithPbaNumber(EMPTY),
            buildApplicantElementWithPbaNumber(EMPTY)
        );

        List<String> errors = pbaNumberService.validate(applicantElementsList);

        assertThat(errors).isEmpty();
    }

    private Element<Applicant> buildApplicantElementWithPbaNumber(String pbaNumber) {
        return element(Applicant.builder()
            .party(ApplicantParty.builder()
                .pbaNumber(pbaNumber)
                .build())
            .build());
    }

    private String getPbaNumberAtIndex(List<Element<Applicant>> applicantElementsList, int index) {
        return applicantElementsList.get(index).getValue().getParty().getPbaNumber();
    }
}
