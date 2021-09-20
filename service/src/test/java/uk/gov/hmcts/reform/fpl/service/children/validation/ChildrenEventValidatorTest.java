package uk.gov.hmcts.reform.fpl.service.children.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ChildrenEventValidatorTest {

    private static final List<String> ERRORS = List.of("errors");
    private static final List<String> NO_ERRORS = List.of();

    private final CaseData caseData = mock(CaseData.class);
    private final CaseData caseDataBefore = mock(CaseData.class);

    private final ChildrenEventSectionValidator validator = mock(ChildrenEventSectionValidator.class);

    private final ChildrenEventValidator underTest = new ChildrenEventValidator(List.of(validator));

    @BeforeEach
    void setUp() {
        when(caseData.getState()).thenReturn(State.SUBMITTED);
    }

    @Test
    void validateCollectionUpdates() {
        when(validator.accepts(ChildrenEventSection.COLLECTION)).thenReturn(true);
        when(validator.validate(caseData, caseDataBefore)).thenReturn(ERRORS);

        assertThat(underTest.validateCollectionUpdates(caseData, caseDataBefore)).isEqualTo(ERRORS);
    }

    @Test
    void validateCollectionUpdatesNoErrors() {
        when(validator.accepts(ChildrenEventSection.COLLECTION)).thenReturn(false);

        assertThat(underTest.validateCollectionUpdates(caseData, caseDataBefore)).isEqualTo(NO_ERRORS);
    }

    @Test
    void validateMainRepresentativeUpdates() {
        when(validator.accepts(ChildrenEventSection.MAIN_REPRESENTATIVE)).thenReturn(true);
        when(validator.validate(caseData, caseDataBefore)).thenReturn(ERRORS);

        assertThat(underTest.validateMainRepresentativeUpdates(caseData, caseDataBefore)).isEqualTo(ERRORS);
    }

    @Test
    void validateMainRepresentativeUpdatesNoErrors() {
        when(validator.accepts(ChildrenEventSection.MAIN_REPRESENTATIVE)).thenReturn(false);

        assertThat(underTest.validateMainRepresentativeUpdates(caseData, caseDataBefore)).isEqualTo(NO_ERRORS);
    }

    @Test
    void validateChildRepresentativeUpdates() {
        when(validator.accepts(ChildrenEventSection.CHILD_REPRESENTATIVES)).thenReturn(true);
        when(validator.validate(caseData, caseDataBefore)).thenReturn(ERRORS);

        assertThat(underTest.validateChildRepresentativeUpdates(caseData, caseDataBefore)).isEqualTo(ERRORS);
    }

    @Test
    void validateChildRepresentativeUpdatesNoErrors() {
        when(validator.accepts(ChildrenEventSection.CHILD_REPRESENTATIVES)).thenReturn(false);

        assertThat(underTest.validateChildRepresentativeUpdates(caseData, caseDataBefore)).isEqualTo(NO_ERRORS);
    }

    @ParameterizedTest
    @EnumSource(value = State.class, names = {"OPEN", "RETURNED"})
    void invalidState(State state) {
        when(caseData.getState()).thenReturn(state);

        assertThat(underTest.validateCollectionUpdates(caseData, caseDataBefore)).isEqualTo(NO_ERRORS);
        assertThat(underTest.validateMainRepresentativeUpdates(caseData, caseDataBefore)).isEqualTo(NO_ERRORS);
        assertThat(underTest.validateChildRepresentativeUpdates(caseData, caseDataBefore)).isEqualTo(NO_ERRORS);
    }
}
