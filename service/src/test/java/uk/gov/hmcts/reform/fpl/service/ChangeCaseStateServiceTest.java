package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CloseCase;

import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.hmcts.reform.fpl.enums.State.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.State.CLOSED;
import static uk.gov.hmcts.reform.fpl.enums.State.FINAL_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.State.SUBMITTED;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.service.ChangeCaseStateService.LABEL_CONTENT;

class ChangeCaseStateServiceTest {
    private ChangeCaseStateService changeCaseStateService = new ChangeCaseStateService();

    @ParameterizedTest
    @MethodSource("stateChangeSource")
    void shouldPrepareNextStateLabelWhenCurrentCaseStateIsNotClosed(State currentState, State expectedMigratedState) {
        CaseData caseData = CaseData.builder()
            .state(currentState)
            .build();

        assertThat(changeCaseStateService.initialiseEventFields(caseData))
            .extracting("nextStateLabelContent")
            .isEqualTo(String.format(LABEL_CONTENT, expectedMigratedState.getLabel().toLowerCase()));
    }

    @Test
    void shouldNotPrepareNextStateLabelWhenCurrentCaseStateIsClosed() {
        CaseData caseData = CaseData.builder()
            .state(CLOSED)
            .build();

        assertThat(changeCaseStateService.initialiseEventFields(caseData))
            .doesNotContainKey("nextStateLabelContent");
    }

    @Test
    void shouldThrowAnErrorWhenCaseIsInAnUnexpectedCaseStateWhenAttemptingToBuildInitialCaseFields() {
        CaseData caseData = CaseData.builder()
            .state(SUBMITTED)
            .build();

        final IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> changeCaseStateService.initialiseEventFields(caseData));

        assertThat(exception.getMessage()).isEqualTo("Should not be able to change from: SUBMITTED");
    }

    @Test
    void shouldRevertStateAndRemoveCasePropertiesAssociatedToClosedStateWhenRevertingAClosedCase() {
        CaseData caseData = CaseData.builder()
            .state(CLOSED)
            .closedStateRadioList(FINAL_HEARING)
            .closeCaseTabField(CloseCase.builder()
                .build())
            .build();

        Map<String, Object> updatedCaseData = changeCaseStateService.updateCaseState(caseData);

        assertThat(updatedCaseData).doesNotContainKeys("deprivationOfLiberty");
        assertThat(updatedCaseData).extracting("state", "closeCaseTabField")
            .containsExactly(FINAL_HEARING, null);
    }

    @ParameterizedTest
    @MethodSource("stateChangeSource")
    void shouldUpdateCaseStateWhenConfirmedChangeStateIsYes(State currentState, State expectedMigratedState) {
        CaseData caseData = CaseData.builder()
            .state(currentState)
            .confirmChangeState(YES.getValue())
            .build();

        assertThat(changeCaseStateService.updateCaseState(caseData))
            .extracting("state")
            .isEqualTo(expectedMigratedState);
    }

    @Test
    void shouldNotChangeCaseStateWhenConfirmedChangeStateIsNo() {
        CaseData caseData = CaseData.builder()
            .state(FINAL_HEARING)
            .confirmChangeState(NO.getValue())
            .build();

        assertThat(changeCaseStateService.updateCaseState(caseData)).doesNotContainKey("state");
    }

    @Test
    void shouldThrowAnErrorWhenCaseIsInAnUnexpectedCaseStateWhenAttemptingToUpdateCaseState() {
        CaseData caseData = CaseData.builder()
            .state(SUBMITTED)
            .confirmChangeState(YES.getValue())
            .build();

        final IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> changeCaseStateService.updateCaseState(caseData));

        assertThat(exception.getMessage()).isEqualTo("Should not be able to change from: SUBMITTED");
    }

    private static Stream<Arguments> stateChangeSource() {
        return Stream.of(
            Arguments.of(FINAL_HEARING, CASE_MANAGEMENT),
            Arguments.of(CASE_MANAGEMENT, FINAL_HEARING)
        );
    }
}
