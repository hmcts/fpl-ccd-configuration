package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Document;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.DocumentSocialWorkOther;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import javax.validation.Validation;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.DocumentStatus.ATTACHED;
import static uk.gov.hmcts.reform.fpl.enums.DocumentStatus.INCLUDED_IN_SWET;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@ExtendWith(SpringExtension.class)
class DocumentsValidatorServiceTest {
    private DocumentsValidatorService documentsValidatorService;
    private static final DocumentReference POPULATED_DOCUMENT = DocumentReference.builder()
        .filename("Mock filename")
        .build();

    @BeforeEach()
    private void setup() {
        documentsValidatorService = new DocumentsValidatorService(Validation
            .buildDefaultValidatorFactory()
            .getValidator());
    }

    @Test
    void shouldGenerateErrorsWhenAdditionalDocumentsAreMissingTitleOrFile() {
        List<Element<DocumentSocialWorkOther>> additionalDocuments = wrapElements(
            DocumentSocialWorkOther.builder().typeOfDocument(testDocumentReference()).build(),
            DocumentSocialWorkOther.builder().documentTitle("doc 1").build(),
            DocumentSocialWorkOther.builder().build()
        );

        List<String> validationErrors = documentsValidatorService.validateSocialWorkOtherDocuments(additionalDocuments);

        assertThat(validationErrors).containsExactly(
            "You must give additional document 1 a name.",
            "You must upload a file for additional document 2.",
            "You must give additional document 3 a name.",
            "You must upload a file for additional document 3."
        );
    }

    @Test
    void shouldNotGenerateErrorsWhenAdditionalDocumentsHaveNameAndFileAttached() {
        List<Element<DocumentSocialWorkOther>> additionalDocuments = wrapElements(
            DocumentSocialWorkOther.builder()
                .documentTitle("doc 1")
                .typeOfDocument(testDocumentReference())
                .build());

        List<String> validationErrors = documentsValidatorService.validateSocialWorkOtherDocuments(additionalDocuments);

        assertThat(validationErrors).isEmpty();
    }

    @Test
    void shouldGenerateErrorsWhenDocumentStatusesIsAttachedButDocumentsWereNotAttached() {
        CaseData caseData = generateMandatoryDocuments(ATTACHED.getLabel(), null);
        List<String> validationErrors = documentsValidatorService.validateDocuments(caseData);

        assertThat(validationErrors)
            .containsOnlyOnce("Check document 1. Attach the document or change the status from 'Attached'.",
                "Check document 2. Attach the document or change the status from 'Attached'.",
                "Check document 3. Attach the document or change the status from 'Attached'.",
                "Check document 4. Attach the document or change the status from 'Attached'.",
                "Check document 5. Attach the document or change the status from 'Attached'.",
                "Check document 6. Attach the document or change the status from 'Attached'.",
                "Check document 7. Attach the document or change the status from 'Attached'.");
    }

    @Test
    void shouldGenerateErrorsWhenDocumentStatusIsIncludedInSwetButSwetWasNotAttached() {
        CaseData caseData = generateMandatoryDocuments(INCLUDED_IN_SWET.getLabel(), null);
        List<String> validationErrors = documentsValidatorService.validateDocuments(caseData);

        assertThat(validationErrors)
            .containsOnlyOnce("Check document 1. Attach the SWET or change the status from 'Included in SWET'.",
                "Check document 2. Attach the SWET or change the status from 'Included in SWET'.",
                "Check document 3. Attach the SWET or change the status from 'Included in SWET'.",
                "Check document 4. Attach the SWET or change the status from 'Included in SWET'.",
                "Check document 6. Attach the SWET or change the status from 'Included in SWET'.",
                "Check document 7. Attach the SWET or change the status from 'Included in SWET'.");
    }

    @Test
    void shouldNotGenerateErrorsWhenDocumentStatusesAreValid() {
        CaseData caseData = generateMandatoryDocuments(ATTACHED.getLabel(), POPULATED_DOCUMENT);
        List<String> validationErrors = documentsValidatorService.validateDocuments(caseData);

        assertThat(validationErrors).isEmpty();
    }

    private CaseData generateMandatoryDocuments(String documentStatus, DocumentReference documentReference) {
        return CaseData.builder()
            .socialWorkChronologyDocument(Document.builder()
                .documentStatus(documentStatus)
                .typeOfDocument(documentReference)
                .build())
            .socialWorkStatementDocument(Document.builder()
                .documentStatus(documentStatus)
                .typeOfDocument(documentReference)
                .build())
            .socialWorkAssessmentDocument(Document.builder()
                .documentStatus(documentStatus)
                .typeOfDocument(documentReference)
                .build())
            .socialWorkCarePlanDocument(Document.builder()
                .documentStatus(documentStatus)
                .typeOfDocument(documentReference)
                .build())
            .socialWorkEvidenceTemplateDocument(Document.builder()
                .documentStatus(documentStatus)
                .typeOfDocument(documentReference)
                .build())
            .thresholdDocument(Document.builder()
                .documentStatus(documentStatus)
                .typeOfDocument(documentReference)
                .build())
            .checklistDocument(Document.builder()
                .documentStatus(documentStatus)
                .typeOfDocument(documentReference)
                .build())
            .build();
    }


}
