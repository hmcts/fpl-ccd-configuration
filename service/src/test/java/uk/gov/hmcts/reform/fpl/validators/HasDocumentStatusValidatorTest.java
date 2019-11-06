package uk.gov.hmcts.reform.fpl.validators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.interfaces.UploadDocumentsGroup;
import uk.gov.hmcts.reform.fpl.model.common.Document;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Validation;
import javax.validation.Validator;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
public class HasDocumentStatusValidatorTest {
    private Validator validator;

    private static final String ATTACHED_ERROR = "Attach the document or change the status from 'Attached'.";
    private static final String TO_FOLLOW_ERROR = "Remove the document or change the status from 'To follow'.";
    private static final String INCLUDED_IN_SWET_ERROR = "Remove the document or change the status from"
        + " 'Included in SWET'.";

    @BeforeEach
    private void setup() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void shouldReturnAnErrorWhenStatusIsAttachedButDocumentIsNotAttached() {
        Document document = Document.builder().documentStatus("Attached").build();

        List<String> errorMessages = validator.validate(document, UploadDocumentsGroup.class).stream()
            .map(error -> error.getMessage())
            .collect(Collectors.toList());

        assertThat(errorMessages).contains(ATTACHED_ERROR);
    }

    @Test
    void shouldReturnAnErrorWhenStatusIsToFollowButDocumentIsAttached() {
        Document document = Document.builder()
            .documentStatus("To follow")
            .typeOfDocument(DocumentReference.builder()
                .filename("Mock file")
                .build())
            .build();

        List<String> errorMessages = validator.validate(document, UploadDocumentsGroup.class).stream()
            .map(error -> error.getMessage())
            .collect(Collectors.toList());

        assertThat(errorMessages).contains(TO_FOLLOW_ERROR);
    }

    @Test
    void shouldReturnAnErrorWhenStatusIsIncludedInSwetButDocumentIsAttached() {
        Document document = Document.builder()
            .documentStatus("Included in social work evidence template (SWET)")
            .typeOfDocument(DocumentReference.builder()
                .filename("Mock file")
                .build())
            .build();

        List<String> errorMessages = validator.validate(document, UploadDocumentsGroup.class).stream()
            .map(error -> error.getMessage())
            .collect(Collectors.toList());

        assertThat(errorMessages).contains(INCLUDED_IN_SWET_ERROR);
    }

    @Test
    void shouldNotReturnAnErrorWhenStatusIsAttachedAndDocumentIsAttached() {
        Document document = Document.builder()
            .documentStatus("Attached")
            .typeOfDocument(DocumentReference.builder()
                .filename("Mock file")
                .build())
            .build();

        List<String> errorMessages = validator.validate(document, UploadDocumentsGroup.class).stream()
            .map(error -> error.getMessage())
            .collect(Collectors.toList());

        assertThat(errorMessages.size()).isEqualTo(0);
        assertThat(errorMessages).doesNotContain(ATTACHED_ERROR);
    }

    @Test
    void shouldNotReturnAnErrorWhenStatusIsToFollowAndDocumentIsNotAttached() {
        Document document = Document.builder().documentStatus("To follow").build();

        List<String> errorMessages = validator.validate(document, UploadDocumentsGroup.class).stream()
            .map(error -> error.getMessage())
            .collect(Collectors.toList());

        assertThat(errorMessages.size()).isEqualTo(0);
        assertThat(errorMessages).doesNotContain(TO_FOLLOW_ERROR);
    }

    @Test
    void shouldNotReturnAnErrorWhenStatusIsIncludedInSwetButDocumentIsNotAttached() {
        Document document = Document.builder()
            .documentStatus("Included in social work evidence template (SWET)")
            .build();

        List<String> errorMessages = validator.validate(document, UploadDocumentsGroup.class).stream()
            .map(error -> error.getMessage())
            .collect(Collectors.toList());

        assertThat(errorMessages.size()).isEqualTo(0);
        assertThat(errorMessages).doesNotContain(INCLUDED_IN_SWET_ERROR);
    }
}
