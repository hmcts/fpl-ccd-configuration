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

@ExtendWith(SpringExtension.class)
class DocumentsValidatorServiceTest {
    private DocumentsValidatorService documentsValidatorService;
    private static final String STATUS_ATTACHED = "Attached";
    private static final String STATUS_TO_FOLLOW = "To follow";
    private static final String STATUS_SWET = "Included in social work evidence template (SWET)";
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
        CaseData caseData = generateMandatoryDocuments(STATUS_ATTACHED, null);
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
    void shouldGenerateErrorsWhenDocumentStatusesIsToFollowButDocumentsWereAttached() {
        CaseData caseData = generateMandatoryDocuments(STATUS_TO_FOLLOW, POPULATED_DOCUMENT);
        List<String> validationErrors = documentsValidatorService.validateDocuments(caseData);

        assertThat(validationErrors)
            .containsOnlyOnce("Check document 1. Remove the document or change the status from 'To follow'.",
                "Check document 2. Remove the document or change the status from 'To follow'.",
                "Check document 3. Remove the document or change the status from 'To follow'.",
                "Check document 4. Remove the document or change the status from 'To follow'.",
                "Check document 5. Remove the document or change the status from 'To follow'.",
                "Check document 6. Remove the document or change the status from 'To follow'.",
                "Check document 7. Remove the document or change the status from 'To follow'.");
    }

    @Test
    void shouldGenerateErrorsWhenDocumentStatusIsIncludedInSwetButWasAttached() {
        CaseData caseData = generateMandatoryDocuments(STATUS_SWET, POPULATED_DOCUMENT);
        List<String> validationErrors = documentsValidatorService.validateDocuments(caseData);

        assertThat(validationErrors)
            .containsOnlyOnce("Check document 1. Remove the document or change the status from 'Included in SWET'.",
                "Check document 2. Remove the document or change the status from 'Included in SWET'.",
                "Check document 3. Remove the document or change the status from 'Included in SWET'.",
                "Check document 4. Remove the document or change the status from 'Included in SWET'.",
                "Check document 6. Remove the document or change the status from 'Included in SWET'.",
                "Check document 7. Remove the document or change the status from 'Included in SWET'.");
    }

    @Test
    void shouldGenerateErrorsWhenDocumentStatusIsIncludedInSwetButSwetWasNotAttached() {
        CaseData caseData = generateMandatoryDocuments(STATUS_SWET, null);
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
        CaseData caseData = generateMandatoryDocuments(STATUS_ATTACHED, POPULATED_DOCUMENT);
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
