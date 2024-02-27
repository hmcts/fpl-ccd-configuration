package uk.gov.hmcts.reform.fpl.service.validators;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType;
import uk.gov.hmcts.reform.fpl.model.ApplicationDocument;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.model.tasklist.TaskState.COMPLETED_FINISHED;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ApplicationDocumentChecker.class})
class ApplicationDocumentCheckerTest {

    @Autowired
    private ApplicationDocumentChecker applicationDocumentChecker;

    @Test
    void shouldReturnNoErrorsAndCompleteStateWhenApplicationDocumentsIsStarted() {
        final CaseData caseData = CaseData.builder()
                .temporaryApplicationDocuments(documents())
                .build();

        final List<String> errors = applicationDocumentChecker.validate(caseData);
        final boolean isCompleted = applicationDocumentChecker.isCompleted(caseData);

        assertThat(errors).isEmpty();
        assertThat(isCompleted).isTrue();
    }

    @Test
    void shouldReturnNoErrorsAndUncompleteStateWhenApplicationDocumentsIsStarted() {
        final CaseData caseData = CaseData.builder()
            .build();

        final List<String> errors = applicationDocumentChecker.validate(caseData);
        final boolean isCompleted = applicationDocumentChecker.isCompleted(caseData);

        assertThat(errors).isEmpty();
        assertThat(isCompleted).isFalse();
    }

    @Test
    void shouldSetIsStartedToTrueWhenApplicationDocumentsAdded() {
        final CaseData caseData = CaseData.builder()
            .temporaryApplicationDocuments(documents())
            .build();

        final boolean isStarted = applicationDocumentChecker.isStarted(caseData);
        assertThat(isStarted).isTrue();
    }

    @Test
    void shouldReturnFalseIfNothingHasBeenEnteredIntoApplicationDocumentEvent() {
        final CaseData caseData = CaseData.builder().build();

        final boolean isStarted = applicationDocumentChecker.isStarted(caseData);
        assertThat(isStarted).isFalse();
    }

    private static List<Element<ApplicationDocument>> documents() {
        return List.of(element(ApplicationDocument.builder()
            .documentType(ApplicationDocumentType.THRESHOLD)
            .document(DocumentReference.builder().build())
            .build()));
    }

    @Test
    void testCompletedState() {
        assertThat(applicationDocumentChecker.completedState()).isEqualTo(COMPLETED_FINISHED);
    }

}
