package uk.gov.hmcts.reform.fpl.validators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.interfaces.UploadDocumentsGroup;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Document;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Validation;
import javax.validation.Validator;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
public class IsIncludedInSwetValidatorTest {
    private Validator validator;

    private static final String ERROR_MESSAGE = "Attach the SWET or change the status from 'Included in SWET'.";

    @BeforeEach
    private void setup() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void shouldReturnAnErrorIfDocumentStatusIsIncludedInSwetButSwetDocumentWasNotAttached() {
        CaseData caseData = CaseData.builder()
            .socialWorkChronologyDocument(Document.builder()
                .documentStatus("Included in social work evidence template (SWET)")
                .build())
            .build();

        List<String> errorMessages = validator.validate(caseData, UploadDocumentsGroup.class).stream()
            .map(error -> error.getMessage())
            .collect(Collectors.toList());

        assertThat(errorMessages).contains(ERROR_MESSAGE);
    }

    @Test
    void shouldNotReturnAnErrorIfDocumentStatusIsIncludedInSwetAndSwetDocumentWasAttached() {
        CaseData caseData = CaseData.builder()
            .socialWorkChronologyDocument(Document.builder()
                .documentStatus("Included in social work evidence template (SWET)")
                .build())
            .socialWorkEvidenceTemplateDocument(Document.builder()
                .documentStatus("Attached")
                .typeOfDocument(DocumentReference.builder()
                    .filename("Mock file")
                    .build())
                .build())
            .build();

        List<String> errorMessages = validator.validate(caseData, UploadDocumentsGroup.class).stream()
            .map(error -> error.getMessage())
            .collect(Collectors.toList());

        assertThat(errorMessages).doesNotContain(ERROR_MESSAGE);
    }
}
