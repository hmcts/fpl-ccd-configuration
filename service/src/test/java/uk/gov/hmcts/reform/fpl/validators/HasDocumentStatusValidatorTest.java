package uk.gov.hmcts.reform.fpl.validators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Document;

import java.util.List;

import java.util.stream.Collectors;

import javax.validation.Validation;
import javax.validation.Validator;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
class HasDocumentStatusValidatorTest {
    private Validator validator;

    private static final String ERROR_MESSAGE = "Tell us the status of all documents including those that you haven't"
        + " uploaded";

    @BeforeEach
    private void setup() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void shouldReturnAnErrorIfDocumentStatusesExistOnCaseData() {
        CaseData caseData = CaseData.builder()
            .documents_checklist_document(Document.builder()
                .documentStatus("Status")
                .build())
            .documents_socialWorkCarePlan_document(Document.builder()
                .documentStatus("Status")
                .build())
            .documents_socialWorkStatement_document(Document.builder()
                .documentStatus("Status")
                .build())
            .documents_threshold_document(Document.builder()
                .documentStatus("Status")
                .build())
            .documents_socialWorkChronology_document(Document.builder()
                .documentStatus("Status")
                .build())
            .documents_socialWorkAssessment_document(Document.builder()
                .documentStatus("Status")
                .build())
            .build();

        List<String> errorMessages = validator.validate(caseData).stream()
            .map(error -> error.getMessage())
            .collect(Collectors.toList());

        assertThat(errorMessages).doesNotContain(ERROR_MESSAGE);
    }

    @Test
    void shouldNotReturnAnErrorIfMandatoryDocumentsDoNotExistOnCaseData() {
        CaseData caseData = CaseData.builder().build();

        List<String> errorMessages = validator.validate(caseData).stream()
            .map(error -> error.getMessage())
            .collect(Collectors.toList());

        assertThat(errorMessages).contains(ERROR_MESSAGE);
    }

    @Test
    void shouldNotReturnAnErrorIfMandatoryDocumentsExistAndThresholdDocumentStatusDoesNotExistOnCaseData() {
        CaseData caseData = CaseData.builder()
            .documents_threshold_document(Document.builder().build())
            .build();

        List<String> errorMessages = validator.validate(caseData).stream()
            .map(error -> error.getMessage())
            .collect(Collectors.toList());

        assertThat(errorMessages).contains(ERROR_MESSAGE);
    }

    @Test
    void shouldNotReturnAnErrorIfAllDocumentStatusesContainEmptyString() {
        CaseData caseData = CaseData.builder()
            .documents_checklist_document(Document.builder()
                .documentStatus("")
                .build())
            .documents_socialWorkCarePlan_document(Document.builder()
                .documentStatus("")
                .build())
            .documents_socialWorkStatement_document(Document.builder()
                .documentStatus("")
                .build())
            .documents_threshold_document(Document.builder()
                .documentStatus("")
                .build())
            .documents_socialWorkChronology_document(Document.builder()
                .documentStatus("")
                .build())
            .documents_socialWorkAssessment_document(Document.builder()
                .documentStatus("")
                .build())
            .build();

        List<String> errorMessages = validator.validate(caseData).stream()
            .map(error -> error.getMessage())
            .collect(Collectors.toList());

        assertThat(errorMessages).contains(ERROR_MESSAGE);
    }
}
