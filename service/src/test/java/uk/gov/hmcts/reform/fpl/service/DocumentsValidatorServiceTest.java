package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableList;
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
import java.util.UUID;
import javax.validation.Validation;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.DocumentStatus.ATTACHED;
import static uk.gov.hmcts.reform.fpl.enums.DocumentStatus.INCLUDED_IN_SWET;

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
    void shouldGenerateErrorsWhenAdditionalDocumentsAreMissingDocumentTitle() {
        List<Element<DocumentSocialWorkOther>> additionalDocuments = createSocialWorkOtherDocuments("");
        List<String> validationErrors = documentsValidatorService.validateSocialWorkOtherDocuments(additionalDocuments);
        assertThat(validationErrors).containsOnlyOnce("You must give additional document 1 a name.",
            "You must give additional document 2 a name.");
    }

    @Test
    void shouldNotGenerateErrorsWhenAdditionalDocumentsHaveDocumentTitles() {
        List<Element<DocumentSocialWorkOther>> additionalDocuments =
            createSocialWorkOtherDocuments("Mock title");
        List<String> validationErrors = documentsValidatorService.validateSocialWorkOtherDocuments(additionalDocuments);
        assertThat(validationErrors.size()).isEqualTo(0);
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

        assertThat(validationErrors.size()).isEqualTo(0);
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

    private List<Element<DocumentSocialWorkOther>> createSocialWorkOtherDocuments(String documentTitle) {
        return ImmutableList.of(
            Element.<DocumentSocialWorkOther>builder()
                .id(UUID.randomUUID())
                .value(DocumentSocialWorkOther.builder()
                    .documentTitle(documentTitle)
                    .build())
                .build(),
            Element.<DocumentSocialWorkOther>builder()
                .id(UUID.randomUUID())
                .value(DocumentSocialWorkOther.builder()
                    .documentTitle(documentTitle)
                    .build())
                .build());
    }
}
