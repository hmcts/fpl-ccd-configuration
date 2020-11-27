package uk.gov.hmcts.reform.fpl.service.validators;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.enums.DocumentType;
import uk.gov.hmcts.reform.fpl.model.ApplicationDocument;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ExtendWith(MockitoExtension.class)
class ApplicationDocumentCheckerTest {

    @InjectMocks
    private ApplicationDocumentChecker applicationDocumentChecker;

    @Test
    void shouldReturnEmptyErrorsAndNonCompletedStateForOptionalEvent() {
        final CaseData caseData = CaseData.builder()
                .documents(documents())
                .build();

        final List<String> errors = applicationDocumentChecker.validate(caseData);
        final boolean isCompleted = applicationDocumentChecker.isCompleted(caseData);

        assertThat(errors).isEmpty();
        assertThat(isCompleted).isFalse();
    }

    @Test
    void shouldReturnTrueIfDocumentsToFollowFieldIsAdded() {
        final CaseData caseData = CaseData.builder()
            .documents(documents())
            .documentsToFollow("Document to follow")
            .build();

        final boolean isStarted = applicationDocumentChecker.isStarted(caseData);

        assertThat(isStarted).isTrue();
    }

    @Test
    void shouldReturnTrueIfDocumentIsAdded() {
        final CaseData caseData = CaseData.builder()
            .documents(documents())
            .build();

        final boolean isStarted = applicationDocumentChecker.isStarted(caseData);

        assertThat(isStarted).isTrue();
    }

    private static List<Element<ApplicationDocument>> documents() {
        return List.of(element(ApplicationDocument.builder()
            .documentType(DocumentType.THRESHOLD)
            .document(DocumentReference.builder().build())
            .build()));
    }
}
