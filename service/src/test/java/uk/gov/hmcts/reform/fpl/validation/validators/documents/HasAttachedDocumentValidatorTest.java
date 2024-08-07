package uk.gov.hmcts.reform.fpl.validation.validators.documents;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.common.Document;
import uk.gov.hmcts.reform.fpl.service.ValidateGroupService;
import uk.gov.hmcts.reform.fpl.validation.groups.UploadDocumentsGroup;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.DocumentStatus.ATTACHED;
import static uk.gov.hmcts.reform.fpl.enums.DocumentStatus.TO_FOLLOW;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createDocumentWithStatus;

@ExtendWith(SpringExtension.class)
class HasAttachedDocumentValidatorTest {
    private Validator validator;
    private ValidateGroupService validateGroupService;

    private static final String ERROR_MESSAGE = "Attach the document or change the status from 'Attached'.";

    @BeforeEach
    private void setup() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
        validateGroupService = new ValidateGroupService(validator);
    }

    @Test
    void shouldReturnAnErrorWhenStatusIsAttachedButDocumentIsNotAttached() {
        Document document = Document.builder().documentStatus(ATTACHED.getLabel()).build();
        List<String> errorMessages = validateGroupService.validateGroup(document, UploadDocumentsGroup.class);
        assertThat(errorMessages).contains(ERROR_MESSAGE);
    }

    @Test
    void shouldNotReturnAnErrorWhenStatusIsAttachedAndDocumentIsAttached() {
        Document document = createDocumentWithStatus(ATTACHED);
        List<String> errorMessages = validateGroupService.validateGroup(document, UploadDocumentsGroup.class);
        assertThat(errorMessages).isEmpty();
    }

    @Test
    void shouldNotReturnAnErrorWhenDocumentIsAttachedWithoutStatus() {
        Document document = createDocumentWithStatus(null);
        List<String> errorMessages = validateGroupService.validateGroup(document, UploadDocumentsGroup.class);
        assertThat(errorMessages).isEmpty();
    }

    @Test
    void shouldNotReturnAnErrorWhenDocumentIsNotAttached() {
        Document document = Document.builder().build();
        List<String> errorMessages = validateGroupService.validateGroup(document, UploadDocumentsGroup.class);
        assertThat(errorMessages).isEmpty();
    }

    @Test
    void shouldNotReturnAnErrorWhenDocumentStatusIsNotAttached() {
        Document document = createDocumentWithStatus(TO_FOLLOW);
        List<String> errorMessages = validateGroupService.validateGroup(document, UploadDocumentsGroup.class);
        assertThat(errorMessages).isEmpty();
    }
}
