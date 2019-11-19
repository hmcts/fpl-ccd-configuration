package uk.gov.hmcts.reform.fpl.validation.validators.documents;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Document;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.service.ValidateGroupService;
import uk.gov.hmcts.reform.fpl.validation.groups.UploadDocumentsGroup;

import java.util.List;
import javax.validation.Validation;
import javax.validation.Validator;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
public class HasDocumentsIncludedInSwetValidatorTest {
    private Validator validator;
    private ValidateGroupService validateGroupService;

    private static final String ERROR_MESSAGE = "Attach the SWET or change the status from 'Included in SWET'.";

    @BeforeEach
    private void setup() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
        validateGroupService = new ValidateGroupService(validator);
    }

    @Test
    void shouldReturnAnErrorIfDocumentStatusIsIncludedInSwetButSwetDocumentWasNotAttached() {
        CaseData caseData = getDocumentIncludedInSwet().build();
        List<String> errorMessages = validateGroupService.validateGroup(caseData, UploadDocumentsGroup.class);
        assertThat(errorMessages).contains(ERROR_MESSAGE);
    }

    @Test
    void shouldNotReturnAnErrorIfDocumentStatusIsIncludedInSwetAndSwetDocumentWasAttached() {
        CaseData caseData = getCaseDataWithSwetAttached();
        List<String> errorMessages =  validateGroupService.validateGroup(caseData, UploadDocumentsGroup.class);
        assertThat(errorMessages).doesNotContain(ERROR_MESSAGE);
    }

    private CaseData.CaseDataBuilder getDocumentIncludedInSwet() {
        return CaseData.builder()
            .socialWorkChronologyDocument(Document.builder()
                .documentStatus("Included in social work evidence template (SWET)")
                .build());
    }

    private CaseData getCaseDataWithSwetAttached() {
        return getDocumentIncludedInSwet()
            .socialWorkEvidenceTemplateDocument(Document.builder()
                .documentStatus("Attached")
                .typeOfDocument(DocumentReference.builder()
                    .filename("Mock file")
                    .build())
                .build())
            .build();
    }
}
