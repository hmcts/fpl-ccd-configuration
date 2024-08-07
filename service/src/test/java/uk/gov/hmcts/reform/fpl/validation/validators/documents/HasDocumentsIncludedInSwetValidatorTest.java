package uk.gov.hmcts.reform.fpl.validation.validators.documents;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.enums.DocumentStatus;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Document;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.service.ValidateGroupService;
import uk.gov.hmcts.reform.fpl.validation.groups.UploadDocumentsGroup;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.DocumentStatus.ATTACHED;
import static uk.gov.hmcts.reform.fpl.enums.DocumentStatus.INCLUDED_IN_SWET;
import static uk.gov.hmcts.reform.fpl.enums.DocumentStatus.TO_FOLLOW;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@ExtendWith(SpringExtension.class)
class HasDocumentsIncludedInSwetValidatorTest {
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
        CaseData caseData = getCaseData(
            getDocument(null, ATTACHED), getDocument(null, INCLUDED_IN_SWET));
        List<String> errorMessages = validateGroupService.validateGroup(caseData, UploadDocumentsGroup.class);
        assertThat(errorMessages).contains(ERROR_MESSAGE);
    }

    @Test
    void shouldReturnAnErrorIfDocumentStatusIsIncludedInSwetButSwetDocumentWasNotAttachedX() {
        CaseData caseData = getCaseData(
            getDocument(testDocumentReference(), TO_FOLLOW), getDocument(null, INCLUDED_IN_SWET));
        List<String> errorMessages = validateGroupService.validateGroup(caseData, UploadDocumentsGroup.class);
        assertThat(errorMessages).contains(ERROR_MESSAGE);
    }

    @Test
    void shouldNotReturnAnErrorIfDocumentStatusIsIncludedInSwetAndSwetDocumentWasAttached() {
        CaseData caseData = getCaseData(
            getDocument(testDocumentReference(), ATTACHED), getDocument(null, INCLUDED_IN_SWET));

        List<String> errorMessages = validateGroupService.validateGroup(caseData, UploadDocumentsGroup.class);
        assertThat(errorMessages).isEmpty();
    }

    @Test
    void shouldNotReturnAnErrorIfDocumentStatusIsNotIncludedInSwet() {
        CaseData caseData = getCaseData(null, getDocument(null, ATTACHED));
        List<String> errorMessages = validateGroupService.validateGroup(caseData, UploadDocumentsGroup.class);
        assertThat(errorMessages).isNotEmpty().doesNotContain(ERROR_MESSAGE);
    }

    private static Document getDocument(DocumentReference reference, DocumentStatus status) {
        return Document.builder()
            .documentStatus(status.getLabel())
            .typeOfDocument(reference)
            .build();
    }

    private static CaseData getCaseData(Document swetDocument, Document workChronologyDocument) {
        CaseData.CaseDataBuilder caseBuilder = CaseData.builder();

        Optional.ofNullable(swetDocument).ifPresent(caseBuilder::socialWorkEvidenceTemplateDocument);
        Optional.ofNullable(workChronologyDocument).ifPresent(caseBuilder::socialWorkChronologyDocument);

        return caseBuilder.build();
    }
}
