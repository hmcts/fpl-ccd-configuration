package uk.gov.hmcts.reform.fpl.service.validators;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import uk.gov.hmcts.reform.fpl.enums.DocumentStatus;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Document;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.DocumentSocialWorkOther;
import uk.gov.hmcts.reform.fpl.utils.TestDataHelper;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.DocumentStatus.ATTACHED;
import static uk.gov.hmcts.reform.fpl.enums.DocumentStatus.INCLUDED_IN_SWET;
import static uk.gov.hmcts.reform.fpl.enums.DocumentStatus.TO_FOLLOW;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {DocumentsValidator.class, LocalValidatorFactoryBean.class})
class DocumentsValidatorTest {

    @Autowired
    private DocumentsValidator documentsValidator;

    @Test
    void shouldReturnErrorWhenNoDocumentsHaveBeenAdded() {
        final CaseData caseData = CaseData.builder().build();

        final List<String> errors = documentsValidator.validate(caseData);

        assertThat(errors)
            .containsExactly("Add social work documents, or details of when you'll send them");
    }

    @Test
    void shouldReturnErrorWhenAtLeastOneDocumentNotPresent() {

        final CaseData caseData = CaseData.builder()
            .otherSocialWorkDocuments(wrapElements(DocumentSocialWorkOther.builder().build()))
            .socialWorkCarePlanDocument(documentWithBinary(TO_FOLLOW))
            .socialWorkStatementDocument(documentWithBinary(TO_FOLLOW))
            .socialWorkAssessmentDocument(documentWithBinary(TO_FOLLOW))
            .socialWorkChronologyDocument(documentWithBinary(TO_FOLLOW))
            .build();

        final List<String> errors = documentsValidator.validate(caseData);

        assertThat(errors)
            .containsExactly("Add social work documents, or details of when you'll send them");
    }

    @Test
    void shouldReturnErrorWhenAtLeastOneDocumentHasStatusAttachedButNoBinaryPresents() {
        final CaseData caseData = CaseData.builder()
            .socialWorkEvidenceTemplateDocument(documentWithoutBinary(ATTACHED))
            .socialWorkCarePlanDocument(documentWithBinary(ATTACHED))
            .socialWorkStatementDocument(documentWithBinary(ATTACHED))
            .socialWorkAssessmentDocument(documentWithBinary(ATTACHED))
            .socialWorkChronologyDocument(documentWithBinary(ATTACHED))
            .checklistDocument(documentWithBinary(ATTACHED))
            .thresholdDocument(documentWithBinary(ATTACHED))
            .otherSocialWorkDocuments(wrapElements(DocumentSocialWorkOther.builder().build()))
            .build();

        final List<String> errors = documentsValidator.validate(caseData);

        assertThat(errors).containsExactly("Attach the document or change the status from 'Attached'.");
    }

    @Test
    void shouldReturnErrorWhenAtLeastOneDocumentHasMissingStatus() {
        final CaseData caseData = CaseData.builder()
            .socialWorkEvidenceTemplateDocument(documentWithBinary(null))
            .socialWorkCarePlanDocument(documentWithoutBinary(TO_FOLLOW))
            .socialWorkStatementDocument(documentWithoutBinary(TO_FOLLOW))
            .socialWorkAssessmentDocument(documentWithoutBinary(TO_FOLLOW))
            .socialWorkChronologyDocument(documentWithoutBinary(TO_FOLLOW))
            .checklistDocument(documentWithoutBinary(TO_FOLLOW))
            .thresholdDocument(documentWithoutBinary(TO_FOLLOW))
            .otherSocialWorkDocuments(wrapElements(DocumentSocialWorkOther.builder().build()))
            .build();

        final List<String> errors = documentsValidator.validate(caseData);

        assertThat(errors)
            .containsExactly("Tell us the status of all documents including those that you haven't uploaded");
    }

    @Test
    void shouldReturnErrorWhenAtLeastOneDocumentHasIncludedInSwetStatusButSwetIsNotAttached() {
        final CaseData caseData = CaseData.builder()
            .socialWorkEvidenceTemplateDocument(documentWithBinary(TO_FOLLOW))
            .socialWorkCarePlanDocument(documentWithBinary(INCLUDED_IN_SWET))
            .socialWorkStatementDocument(documentWithBinary(ATTACHED))
            .socialWorkAssessmentDocument(documentWithBinary(ATTACHED))
            .socialWorkChronologyDocument(documentWithBinary(ATTACHED))
            .checklistDocument(documentWithBinary(ATTACHED))
            .thresholdDocument(documentWithBinary(ATTACHED))
            .otherSocialWorkDocuments(wrapElements(DocumentSocialWorkOther.builder().build()))
            .build();

        final List<String> errors = documentsValidator.validate(caseData);

        assertThat(errors).containsExactly("Attach the SWET or change the status from 'Included in SWET'.");
    }

    @Test
    void shouldReturnEmptyErrorsWhenAllDocumentsProvided() {

        final CaseData caseData = CaseData.builder()
            .otherSocialWorkDocuments(wrapElements(DocumentSocialWorkOther.builder().build()))
            .socialWorkCarePlanDocument(documentWithoutBinary(TO_FOLLOW))
            .socialWorkStatementDocument(documentWithoutBinary(TO_FOLLOW))
            .socialWorkAssessmentDocument(documentWithoutBinary(TO_FOLLOW))
            .socialWorkChronologyDocument(documentWithoutBinary(TO_FOLLOW))
            .checklistDocument(documentWithoutBinary(TO_FOLLOW))
            .thresholdDocument(documentWithoutBinary(TO_FOLLOW))
            .socialWorkEvidenceTemplateDocument(documentWithBinary(ATTACHED))
            .build();

        final List<String> errors = documentsValidator.validate(caseData);

        assertThat(errors).isEmpty();
    }

    private static Document documentWithBinary(DocumentStatus status) {
        return document(status, TestDataHelper.testDocumentReference());
    }

    private static Document documentWithoutBinary(DocumentStatus status) {
        return document(status, null);
    }

    private static Document document(DocumentStatus status, DocumentReference documentReference) {
        return Document.builder()
            .documentStatus(Optional.ofNullable(status).map(DocumentStatus::getLabel).orElse(null))
            .typeOfDocument(documentReference)
            .build();
    }
}
