package uk.gov.hmcts.reform.fpl.service.validators;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.enums.ProceedingStatus;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Proceeding;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.fpl.model.tasklist.TaskState.COMPLETED_FINISHED;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
class ProceedingsCheckerTest {

    private static final CaseData ANY_CASE_DATA = mock(CaseData.class);

    @InjectMocks
    private ProceedingsChecker proceedingsChecker;

    @Test
    void testValidate() {
        assertThat(proceedingsChecker.validate(ANY_CASE_DATA)).isEmpty();
    }

    @Test
    void testCompletedState() {
        assertThat(proceedingsChecker.completedState()).isEqualTo(COMPLETED_FINISHED);
    }

    @Nested
    class IsCompleted {

        @ParameterizedTest
        @NullSource
        @MethodSource("uk.gov.hmcts.reform.fpl.service.validators.ProceedingsCheckerTest#incompleteProceedings")
        void shouldReturnEmptyErrorsAndNonCompletedState(List<Element<Proceeding>> proceedings) {
            final CaseData caseData = CaseData.builder()
                .proceedings(proceedings)
                .build();

            final boolean isCompleted = proceedingsChecker.isCompleted(caseData);

            assertThat(isCompleted).isFalse();
        }

        @ParameterizedTest
        @MethodSource("uk.gov.hmcts.reform.fpl.service.validators.ProceedingsCheckerTest#completeProceedings")
        void shouldReturnEmptyErrorsAndCompletedState(List<Element<Proceeding>> proceedings) {
            final CaseData caseData = CaseData.builder()
                .proceedings(proceedings)
                .build();

            final boolean isCompleted = proceedingsChecker.isCompleted(caseData);

            assertThat(isCompleted).isTrue();
        }
    }

    private static Stream<Arguments> incompleteProceedings() {
        return Stream.of(
            wrapElements(Proceeding.builder()
                .build()),
            wrapElements(completedProceeding()
                .proceedingStatus(null)
                .build()),
            wrapElements(completedProceeding()
                .caseNumber("")
                .build()),
            wrapElements(completedProceeding()
                .caseNumber(null)
                .build()),
            wrapElements(completedProceeding()
                .started(null)
                .build()),
            wrapElements(completedProceeding()
                .started("")
                .build()),
            wrapElements(completedProceeding()
                .ended(null)
                .build()),
            wrapElements(completedProceeding()
                .ended("")
                .build()),
            wrapElements(completedProceeding()
                .ordersMade("")
                .build()),
            wrapElements(completedProceeding()
                .ordersMade(null)
                .build()),
            wrapElements(completedProceeding()
                .judge("")
                .build()),
            wrapElements(completedProceeding()
                .judge(null)
                .build()),
            wrapElements(completedProceeding()
                .children("")
                .build()),
            wrapElements(completedProceeding()
                .children(null)
                .build()),
            wrapElements(completedProceeding()
                .guardian("")
                .build()),
            wrapElements(completedProceeding()
                .guardian(null)
                .build()),
            wrapElements(completedProceeding()
                .sameGuardianNeeded(null)
                .build()),
            wrapElements(completedProceeding()
                .sameGuardianDetails("")
                .build()),
            wrapElements(completedProceeding()
                .sameGuardianDetails(null)
                .build())
        ).map(Arguments::of);
    }

    private static Stream<Arguments> completeProceedings() {
        return Stream.of(
            wrapElements(completedProceeding().build()),
            wrapElements(completedProceeding()
                .proceedingStatus(ProceedingStatus.ONGOING)
                .ended("")
                .build()),
            wrapElements(completedProceeding()
                .proceedingStatus(ProceedingStatus.ONGOING)
                .ended(null)
                .build()),
            wrapElements(completedProceeding()
                .sameGuardianNeeded(YesNo.YES)
                .sameGuardianDetails(null)
                .build()),
            wrapElements(completedProceeding()
                .sameGuardianNeeded(YesNo.YES)
                .sameGuardianDetails("")
                .build())
        ).map(Arguments::of);
    }

    private static Proceeding.ProceedingBuilder completedProceeding() {
        return Proceeding.builder()
            .proceedingStatus(ProceedingStatus.PREVIOUS)
            .caseNumber("Test")
            .started("Test")
            .ended("Test")
            .ordersMade("Test")
            .judge("Test")
            .children("Test")
            .guardian("Test")
            .sameGuardianNeeded(YesNo.NO)
            .sameGuardianDetails("Test");
    }
}
