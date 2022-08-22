package uk.gov.hmcts.reform.fpl;

import org.junit.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.CallbackResponse;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.util.Optional;

import static uk.gov.hmcts.reform.fpl.controllers.support.MigrateCaseController.MIGRATION_ID_KEY;
import static uk.gov.hmcts.reform.fpl.service.CaseConverter.MAP_TYPE;

public class GlobalSearchMigrationApiTest extends AbstractApiTest {

    @Test
    public void shouldEnableGlobalSearchForCaseData() {
        CaseData caseData = createCase("global-search-migration/case.json", LA_SWANSEA_USER_1);
        CaseDetails caseDetails =  CaseDetails.builder()
            .id(caseData.getId())
            .state(Optional.ofNullable(caseData.getState()).map(State::getValue).orElse(null))
            .data(objectMapper.convertValue(caseData, MAP_TYPE))
            .build();
        caseDetails.getData().put(MIGRATION_ID_KEY, "DFPL-702");
        caseData = callAboutToSubmit(caseData, caseDetails);
    }

    private CaseData callAboutToSubmit(CaseData caseData, CaseDetails caseDetails) {

        CallbackResponse response = callback(caseData, caseDetails, LA_SWANSEA_USER_1, "migrate-case/about-to-submit");

//        final String actualApplication = documentService
//            .getPdfContent(response.getCaseData().getC110A().getSubmittedForm(), LA_SWANSEA_USER_1, "C110A");
//
//        final String expectedApplication = readString("case-submission/application.txt",
//            Map.of("id", caseData.getId(), "issueDate", formatLocalDateToString(now(), DATE),
//                "age", getAge(LocalDate.of(2020, Month.JANUARY, 1))));
//
//        assertThat(response.getCaseData().getDateSubmitted()).isEqualTo(now());
//        assertThat(actualApplication).isEqualToNormalizingWhitespace(expectedApplication);

        return response.getCaseData();
    }

//    public void callSubmitted(CaseData caseData) {
//
//        submittedCallback(caseData, LA_SWANSEA_USER_1, "case-submission/submitted");
//
//        List<Payment> payments = paymentService.pollPayments(caseData.getId(), COURT_ADMIN);
//        List<Email> emails = emailService.pollEmails(caseData.getId(), LA_SWANSEA_USER_1);
//
//        assertThat(payments).contains(
//            Payment.builder()
//                .accountNumber("PBA0082848")
//                .amount(BigDecimal.valueOf(2215).setScale(2, FLOOR))
//                .build());
//
//        assertThat(emails)
//            .extracting(Email::getSubject)
//            .contains("Urgent application – same day hearing, White");
//    }
//
//    private int getAge(LocalDate birthDate) {
//        return Period.between(birthDate, LocalDate.now()).getYears();
//    }
}
