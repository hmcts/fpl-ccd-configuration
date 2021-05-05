package uk.gov.hmcts.reform.fpl;

import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.fnp.model.payment.Payment;
import uk.gov.hmcts.reform.fpl.model.Allocation;
import uk.gov.hmcts.reform.fpl.model.CallbackResponse;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Email;
import uk.gov.hmcts.reform.fpl.model.User;
import uk.gov.hmcts.reform.fpl.util.DocumentService;
import uk.gov.hmcts.reform.fpl.util.EmailService;
import uk.gov.hmcts.reform.fpl.util.PaymentService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static java.math.RoundingMode.FLOOR;
import static java.time.LocalDate.now;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.model.User.user;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.ResourceReader.readString;


@SpringBootTest
@RunWith(SpringIntegrationSerenityRunner.class)
public class CaseSubmission extends AbstractApiTest {

    private static final User SOLICITOR = user("kurt@swansea.gov.uk");
    private static final User COURT_ADMIN = user("hmcts-admin@example.com");

    @Autowired
    private EmailService emailService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private DocumentService documentService;

    @Test
    public void caseSubmission() {

        CaseData caseData = createCase("case-submission/case.json", SOLICITOR);

        caseData = callAboutToStart(caseData);
        caseData = callMidEvent(caseData);
        caseData = callAboutToSubmit(caseData);
        callSubmitted(caseData);
    }

    public CaseData callAboutToStart(CaseData caseData) {

        CallbackResponse response = callback(caseData, SOLICITOR, "case-submission/about-to-start");

        String actualApplication = documentService.getPdfContent(response.getCaseData().getDraftApplicationDocument(),
            SOLICITOR, "C110A");

        final String expectedApplication = readString("case-submission/application-draft.txt",
            Map.of("id", caseData.getId(), "issueDate", formatLocalDateToString(now(), DATE)));

        assertThat(response.getCaseData().getAmountToPay()).isEqualTo("205500");

        assertEqualsNormalizingSpaces(actualApplication, expectedApplication);

        return response.getCaseData();
    }

    public CaseData callMidEvent(CaseData caseData) {

        CallbackResponse response = callback(caseData, SOLICITOR, "case-submission/mid-event");

        assertThat(response.getErrors()).containsExactly(
            "In the allocation proposal section:",
            "• Add the allocation proposal");

        CaseData updated = caseData.toBuilder()
            .allocationProposal(Allocation.builder()
                .proposal("District judge")
                .build())
            .build();

        response = callback(updated, SOLICITOR, "case-submission/mid-event");

        assertThat(response.getErrors()).isEmpty();

        return response.getCaseData();
    }

    public CaseData callAboutToSubmit(CaseData caseData) {

        CallbackResponse response = callback(caseData, SOLICITOR, "case-submission/about-to-submit");

        final String actualApplication = documentService
            .getPdfContent(response.getCaseData().getSubmittedForm(), SOLICITOR, "C110A");

        final String expectedApplication = readString("case-submission/application.txt",
            Map.of("id", caseData.getId(), "issueDate", formatLocalDateToString(now(), DATE)));

        assertThat(response.getCaseData().getDateSubmitted()).isEqualTo(now());
        assertEqualsNormalizingSpaces(actualApplication, expectedApplication);

        return response.getCaseData();
    }

    public void callSubmitted(CaseData caseData) {

        callback(caseData, SOLICITOR, "case-submission/submitted");

        List<Payment> payments = paymentService.pollPayments(caseData.getId(), COURT_ADMIN);
        List<Email> emails = emailService.pollEmails(caseData.getId(), SOLICITOR);

        assertThat(payments).contains(
            Payment.builder()
                .accountNumber("PBA0082848")
                .amount(BigDecimal.valueOf(2055).setScale(2, FLOOR))
                .build());

        assertThat(emails)
            .extracting(Email::getSubject)
            .contains("Urgent application – same day hearing, Wall");

    }

}
