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
public class HasAttachedDocumentValidatorTest {
    private Validator validator;

    private static final String ERROR_MESSAGE = "Attach the document or change the status from 'Attached'.";

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

        assertThat(errorMessages).contains(ERROR_MESSAGE);
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

        assertThat(errorMessages).doesNotContain(ERROR_MESSAGE);
    }
}
