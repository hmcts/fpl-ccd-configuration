package uk.gov.hmcts.reform.fpl.validation.validators.documents;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.common.Document;
import uk.gov.hmcts.reform.fpl.service.ValidateGroupService;
import uk.gov.hmcts.reform.fpl.validation.groups.UploadDocumentsGroup;

import java.util.List;
import javax.validation.Validation;
import javax.validation.Validator;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.DocumentStatus.ATTACHED;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createDocumentWithAttachedFile;

@ExtendWith(SpringExtension.class)
public class HasAttachedDocumentValidatorTest {
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
        Document document = createDocumentWithAttachedFile();
        List<String> errorMessages = validateGroupService.validateGroup(document, UploadDocumentsGroup.class);
        assertThat(errorMessages).doesNotContain(ERROR_MESSAGE);
    }
}
