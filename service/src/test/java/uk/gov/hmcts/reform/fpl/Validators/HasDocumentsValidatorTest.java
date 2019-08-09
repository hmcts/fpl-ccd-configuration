package uk.gov.hmcts.reform.fpl.Validators;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Document;
import uk.gov.hmcts.reform.fpl.validators.HasDocumentsValidator;

import javax.validation.ConstraintValidatorContext;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
class HasDocumentsValidatorTest {
    private HasDocumentsValidator validator = new HasDocumentsValidator();
    private ConstraintValidatorContext constraintValidatorContext;

    @Test
    void shouldReturnFalseIfNoMandatoryDocumentsExistOnCaseData() {
        CaseData caseData = CaseData.builder().build();
        Boolean isValid = validator.isValid(caseData, constraintValidatorContext);

        assertThat(isValid).isFalse();
    }

    @Test
    void shouldReturnTrueIfPartialMandatoryDocumentsExistOnCaseData() {
        CaseData caseData = CaseData.builder()
            .documents_checklist_document(Document.builder().build())
            .documents_socialWorkAssessment_document(Document.builder().build())
            .documents_threshold_document(Document.builder().build())
            .build();

        Boolean isValid = validator.isValid(caseData, constraintValidatorContext);
        assertThat(isValid).isTrue();
    }

    @Test
    void ShouldReturnTrueIfAllMandatoryDocumentsExistOnCaseData() {
        CaseData caseData = CaseData.builder()
            .documents_checklist_document(Document.builder().build())
            .documents_socialWorkAssessment_document(Document.builder().build())
            .documents_socialWorkCarePlan_document(Document.builder().build())
            .documents_socialWorkChronology_document(Document.builder().build())
            .documents_socialWorkEvidenceTemplate_document(Document.builder().build())
            .documents_threshold_document(Document.builder().build())
            .build();

        Boolean isValid = validator.isValid(caseData, constraintValidatorContext);
        assertThat(isValid).isTrue();
    }
}
