package uk.gov.hmcts.reform.fpl;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.fnp.model.payment.Payment;
import uk.gov.hmcts.reform.fpl.model.Allocation;
import uk.gov.hmcts.reform.fpl.model.CallbackResponse;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Email;
import uk.gov.hmcts.reform.fpl.service.DocumentService;
import uk.gov.hmcts.reform.fpl.service.EmailService;
import uk.gov.hmcts.reform.fpl.service.PaymentService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.Period;
import java.util.List;
import java.util.Map;

import static java.math.RoundingMode.FLOOR;
import static java.time.LocalDate.now;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.ResourceReader.readString;

public class SubmitCaseApiTest extends AbstractApiTest {

    @Autowired
    private EmailService emailService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private DocumentService documentService;

    @Test
    public void shouldSubmitAndPayForApplication() {

        CaseData caseData = createCase("case-submission/case.json", LA_SWANSEA_USER_1);

        caseData = callAboutToStart(caseData);
        caseData = callMidEvent(caseData);
        caseData = callAboutToSubmit(caseData);
        // callSubmitted(caseData);
    }

    public CaseData callAboutToStart(CaseData caseData) {

        CallbackResponse response = callback(caseData, LA_SWANSEA_USER_1, "case-submission/about-to-start");

        String actualApplication = documentService.getPdfContent(response.getCaseData().getDraftApplicationDocument(),
            LA_SWANSEA_USER_1, "C110A");

        final String expectedApplication = readString("case-submission/application-draft.txt",
            Map.of("id", caseData.getId(), "issueDate", formatLocalDateToString(now(), DATE),
                    "age", getAge(LocalDate.of(2020, Month.JANUARY, 1))));

        assertThat(response.getCaseData().getAmountToPay()).isEqualTo("251500");
        assertThat(actualApplication).isEqualToNormalizingWhitespace(expectedApplication);

        return response.getCaseData();
    }

    public CaseData callMidEvent(CaseData caseData) {

        CallbackResponse response = callback(caseData, LA_SWANSEA_USER_1, "case-submission/mid-event");

        assertThat(response.getErrors()).containsExactly(
            "In the allocation proposal section:",
            "• Add the allocation proposal");

        CaseData updated = caseData.toBuilder()
            .allocationProposal(Allocation.builder()
                .proposal("District judge")
                .proposalReason("Example reason")
                .build())
            .build();

        response = callback(updated, LA_SWANSEA_USER_1, "case-submission/mid-event");

        assertThat(response.getErrors()).isEmpty();

        return response.getCaseData();
    }

    public CaseData callAboutToSubmit(CaseData caseData) {

        CallbackResponse response = callback(caseData, LA_SWANSEA_USER_1, "case-submission/about-to-submit");

        final String actualApplication = documentService
            .getPdfContent(response.getCaseData().getC110A().getSubmittedForm(), LA_SWANSEA_USER_1, "C110A");

        final String expectedApplication = readString("case-submission/application.txt",
            Map.of("id", caseData.getId(), "issueDate", formatLocalDateToString(now(), DATE),
                    "age", getAge(LocalDate.of(2020, Month.JANUARY, 1))));

        assertThat(response.getCaseData().getDateSubmitted()).isEqualTo(now());
        assertThat(actualApplication).isEqualToNormalizingWhitespace(expectedApplication);

        return response.getCaseData();
    }

    public void callSubmitted(CaseData caseData) {

        submittedCallback(caseData, LA_SWANSEA_USER_1, "case-submission/submitted");

        List<Payment> payments = paymentService.pollPayments(caseData.getId(), COURT_ADMIN);
        List<Email> emails = emailService.pollEmails(caseData.getId(), LA_SWANSEA_USER_1);

        assertThat(payments).contains(
            Payment.builder()
                .accountNumber("PBA0082848")
                .amount(BigDecimal.valueOf(2437).setScale(2, FLOOR))
                .build());

        assertThat(emails)
            .extracting(Email::getSubject)
            .contains("Urgent application – same day hearing, White");
    }

    private int getAge(LocalDate birthDate) {
        return Period.between(birthDate, LocalDate.now()).getYears();
    }
}
