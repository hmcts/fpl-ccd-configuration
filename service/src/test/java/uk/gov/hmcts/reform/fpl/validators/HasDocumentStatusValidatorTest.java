package uk.gov.hmcts.reform.fpl.validators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Document;

import javax.validation.ConstraintValidatorContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(SpringExtension.class)
class HasDocumentStatusValidatorTest {
    HasDocumentStatusValidator validator = new HasDocumentStatusValidator();

    @Mock
    private ConstraintValidatorContext constraintValidatorContext;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder constraintViolationBuilder;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext
        nodeBuilderCustomizableContext;

    @BeforeEach
    private void prepareMocks() {
        given(constraintValidatorContext.getDefaultConstraintMessageTemplate()).willReturn("");
        given(constraintValidatorContext.buildConstraintViolationWithTemplate(Mockito.anyString()))
            .willReturn(constraintViolationBuilder);
        given(constraintViolationBuilder.addPropertyNode(Mockito.anyString()))
            .willReturn(nodeBuilderCustomizableContext);
    }

    @Test
    void shouldReturnFalseIfMandatoryDocumentsDoNotExistOnCaseData() {
        CaseData caseData = CaseData.builder().build();

        Boolean isValid = validator.isValid(caseData, constraintValidatorContext);
        assertThat(isValid).isFalse();
    }

    @Test
    void shouldReturnFalseIfMandatoryDocumentsExistAndThresholdDocumentStatusDoesNotExistOnCaseData() {
        CaseData caseData = CaseData.builder()
            .documents_threshold_document(Document.builder().build())
            .build();

        Boolean isValid = validator.isValid(caseData, constraintValidatorContext);
        assertThat(isValid).isFalse();
    }

    @Test
    void shouldReturnTrueIfMandatoryDocumentsExistAndAllDocumentStatusesExistOnCaseData() {
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

        Boolean isValid = validator.isValid(caseData, constraintValidatorContext);
        assertThat(isValid).isTrue();
    }

    @Test
    void shouldReturnFalseIfAllDocumentStatusesContainEmptyString() {
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

        Boolean isValid = validator.isValid(caseData, constraintValidatorContext);
        assertThat(isValid).isFalse();
    }
}
