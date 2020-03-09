package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.utils.PbaNumberHelper;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

class PbaNumberServiceTest {

    private static final String VALIDATION_ERROR_MESSAGE = "Payment by account (PBA) number must include 7 numbers";
    private static final String PBA_PREFIX = "PBA";
    private static final String FIRST_NUMBER = "1234567";
    private static final String SECOND_NUMBER = "9876543";

    private PbaNumberService pbaNumberService;

    @BeforeEach
    void setup() {
        this.pbaNumberService = new PbaNumberService(new PbaNumberHelper());
    }

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
