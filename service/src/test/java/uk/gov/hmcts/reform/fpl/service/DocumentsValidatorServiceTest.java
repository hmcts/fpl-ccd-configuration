package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import uk.gov.hmcts.reform.fpl.model.common.DocumentSocialWorkOther;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@ExtendWith(SpringExtension.class)
class DocumentsValidatorServiceTest {
    private DocumentsValidatorService documentsValidatorService = new DocumentsValidatorService();

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
}
