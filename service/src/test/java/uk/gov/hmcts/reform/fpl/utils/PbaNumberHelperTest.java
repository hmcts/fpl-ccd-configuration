package uk.gov.hmcts.reform.fpl.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.PBAPayment;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.PbaNumberHelper.getNonEmptyPbaNumber;
import static uk.gov.hmcts.reform.fpl.utils.PbaNumberHelper.getPBAPaymentWithNonEmptyPbaNumber;
import static uk.gov.hmcts.reform.fpl.utils.PbaNumberHelper.setPrefix;

class PbaNumberHelperTest {

    @Test
    void shouldReturnNullWhenPbaNumberIsNull() {
        assertThat(setPrefix(null)).isNull();
    }

    @Test
    void shouldReturnUnchangedPbaNumberForCorrectPrefix() {
        assertThat(setPrefix("PBA123")).isEqualTo("PBA123");
    }

    @Test
    void shouldReturnPbaNumberWithPrefixInUppercase() {
        assertThat(setPrefix("pba456")).isEqualTo("PBA456");
    }

    @Test
    void shouldReturnPbaNumberWithAddedPrefix() {
        assertThat(setPrefix("789")).isEqualTo("PBA789");
    }

    @Test
    void shouldReturnNonEmptyPbaNumbersForApplicantElementsList() {
        List<Element<Applicant>> applicantElementsList = List.of(
            buildApplicantElementWithPbaNumber(""),
            buildApplicantElementWithPbaNumber(null),
            buildApplicantElementWithPbaNumber("PBA123"),
            buildApplicantElementWithPbaNumber("test")
        );

        Stream<String> result = PbaNumberHelper.getNonEmptyPbaNumbers(applicantElementsList);

        assertThat(result).containsExactly("PBA123", "test");
    }

    @Test
    void shouldReturnNonEmptyPbaNumberForC2DocumentBundle() {
        C2DocumentBundle documentWithNonEmptyPbaNumber = C2DocumentBundle.builder().pbaNumber("123").build();
        C2DocumentBundle documentWithEmptyPbaNumber = C2DocumentBundle.builder().pbaNumber("").build();
        C2DocumentBundle documentWithNullPbaNumber = C2DocumentBundle.builder().build();

        assertThat(getNonEmptyPbaNumber(documentWithNonEmptyPbaNumber)).contains("123");
        assertThat(getNonEmptyPbaNumber(documentWithEmptyPbaNumber)).isEmpty();
        assertThat(getNonEmptyPbaNumber(documentWithNullPbaNumber)).isEmpty();
    }

    @Test
    void shouldReturnNonEmptyPbaNumberForPBAPayment() {
        PBAPayment pbaWithNonEmptyPbaNumber = PBAPayment.builder().pbaNumber("123").build();
        PBAPayment pbaWithEmptyPbaNumber = PBAPayment.builder().pbaNumber("").build();
        PBAPayment pbaWithNullPbaNumber = PBAPayment.builder().build();

        assertThat(getPBAPaymentWithNonEmptyPbaNumber(pbaWithNonEmptyPbaNumber)).contains("123");
        assertThat(getPBAPaymentWithNonEmptyPbaNumber(pbaWithEmptyPbaNumber)).isEmpty();
        assertThat(getPBAPaymentWithNonEmptyPbaNumber(pbaWithNullPbaNumber)).isEmpty();
    }

    private Element<Applicant> buildApplicantElementWithPbaNumber(String pbaNumber) {
        return element(Applicant.builder()
            .party(ApplicantParty.builder()
                .pbaNumber(pbaNumber)
                .build())
            .build());
    }
}
