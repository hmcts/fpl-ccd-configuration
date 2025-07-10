package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.ReturnApplication;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.ReturnedApplicationReasons.INCOMPLETE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;

@WebMvcTest(ReturnApplicationController.class)
@OverrideAutoConfiguration(enabled = true)
class ReturnApplicationAboutToSubmitTest extends AbstractCallbackTest {
    ReturnApplicationAboutToSubmitTest() {
        super("return-application");
    }

    @Test
    void shouldMigrateSubmittedDocumentToReturnedDocumentBundle() {
        DocumentReference submittedForm = buildSubmittedForm("mockSubmittedForm.pdf");

        ReturnApplication returnApplication = ReturnApplication.builder()
            .note("Some note")
            .reason(List.of(INCOMPLETE))
            .build();

        CaseData caseData = CaseData.builder()
            .returnApplication(returnApplication)
            .dateSubmitted(LocalDate.of(2050, 5, 19))
            .c110A(uk.gov.hmcts.reform.fpl.model.group.C110A.builder()
                .submittedForm(submittedForm)
                .build())
            .state(State.OPEN)
            .build();

        CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseData));

        ReturnApplication expectedReturnApplication = returnApplication.toBuilder()
            .returnedDate(getFormattedDate())
            .submittedDate("19 May 2050")
            .document(buildSubmittedForm("mockSubmittedForm_returned.pdf"))
            .build();

        assertThat(extractedCaseData.getC110A().getSubmittedForm()).isNull();
        assertThat(extractedCaseData.getReturnApplication()).isEqualTo(expectedReturnApplication);
        assertThat(extractedCaseData.getDateSubmitted()).isNull();
        assertThat(extractedCaseData.getLastSubmittedDate()).isEqualTo(LocalDate.of(2050, 5, 19));
    }

    @Test
    void shouldMigrateSubmittedDocumentToReturnedDocumentBundleIfMainApplicationIsRemovedBySuperAdmin() {
        DocumentReference submittedForm = null;

        ReturnApplication returnApplication = ReturnApplication.builder()
            .note("Some note")
            .reason(List.of(INCOMPLETE))
            .build();

        CaseData caseData = CaseData.builder()
            .returnApplication(returnApplication)
            .dateSubmitted(LocalDate.of(2050, 5, 19))
            .c110A(uk.gov.hmcts.reform.fpl.model.group.C110A.builder()
                .submittedForm(submittedForm)
                .build())
            .state(State.OPEN)
            .build();

        CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseData));

        ReturnApplication expectedReturnApplication = returnApplication.toBuilder()
            .returnedDate(getFormattedDate())
            .submittedDate("19 May 2050")
            .document(null)
            .build();

        assertThat(extractedCaseData.getC110A().getSubmittedForm()).isNull();
        assertThat(extractedCaseData.getReturnApplication()).isEqualTo(expectedReturnApplication);
        assertThat(extractedCaseData.getDateSubmitted()).isNull();
        assertThat(extractedCaseData.getLastSubmittedDate()).isEqualTo(LocalDate.of(2050, 5, 19));
    }

    private String getFormattedDate() {
        return formatLocalDateToString(dateNow(), "d MMMM yyyy");
    }

    private DocumentReference buildSubmittedForm(String filename) {
        return DocumentReference.builder()
            .filename(filename)
            .url("http://mock-dm-store")
            .binaryUrl("http://mock-dm-store")
            .build();
    }
}
