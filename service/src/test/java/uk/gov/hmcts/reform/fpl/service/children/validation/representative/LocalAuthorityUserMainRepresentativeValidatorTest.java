package uk.gov.hmcts.reform.fpl.service.children.validation.representative;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.event.ChildrenEventData;
import uk.gov.hmcts.reform.fpl.service.UserService;
import uk.gov.hmcts.reform.fpl.service.children.validation.ChildrenEventSection;
import uk.gov.hmcts.reform.fpl.service.representative.RepresentativeSolicitorSanitizer;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.service.children.validation.ChildrenEventSection.MAIN_REPRESENTATIVE;

class LocalAuthorityUserMainRepresentativeValidatorTest {
    private static final List<String> NO_SOLICITOR_ERRORS = List.of();
    private static final List<String> SOLICITOR_ERRORS = List.of("error");

    private final CaseData caseData = mock(CaseData.class);
    private final CaseData caseDataBefore = mock(CaseData.class);

    private final ChildrenEventData currentEventData = mock(ChildrenEventData.class);
    private final ChildrenEventData previousEventData = mock(ChildrenEventData.class);

    private final RespondentSolicitor currentSolicitor = mock(RespondentSolicitor.class);
    private final RespondentSolicitor previousSolicitor = mock(RespondentSolicitor.class);

    private final UserService user = mock(UserService.class);
    private final MainRepresentativeValidator mainRepValidator = mock(MainRepresentativeValidator.class);
    private final RepresentativeSolicitorSanitizer sanitizer = mock(RepresentativeSolicitorSanitizer.class);

    private final LocalAuthorityUserMainRepresentativeValidator underTest =
        new LocalAuthorityUserMainRepresentativeValidator(user, mainRepValidator, sanitizer);

    @DisplayName("Accept users that do not have HMCTS roles when section is MAIN_REPRESENTATIVE")
    @Test
    void acceptsNonHMCTS() {
        when(user.isHmctsUser()).thenReturn(false);

        assertThat(underTest.accepts(MAIN_REPRESENTATIVE)).isTrue();
    }

    @DisplayName("Reject users that have HMCTS roles when section is MAIN_REPRESENTATIVE")
    @Test
    void acceptsHMCTS() {
        when(user.isHmctsUser()).thenReturn(true);

        assertThat(underTest.accepts(MAIN_REPRESENTATIVE)).isFalse();
    }

    @DisplayName("Reject when the user is valid but the given section is not MAIN_REPRESENTATIVE")
    @ParameterizedTest(name = "when section is {0}")
    @EnumSource(value = ChildrenEventSection.class, mode = EnumSource.Mode.EXCLUDE, names = {"MAIN_REPRESENTATIVE"})
    void acceptsDifferentSection(ChildrenEventSection section) {
        when(user.isHmctsUser()).thenReturn(false);

        assertThat(underTest.accepts(section)).isFalse();
    }

    @DisplayName("Validate with exception when the children representation wasn't set and still isn't")
    @Test
    void validateNoChangeNotSet() {
        when(caseData.getChildrenEventData()).thenReturn(currentEventData);
        when(caseDataBefore.getChildrenEventData()).thenReturn(previousEventData);

        when(currentEventData.getChildrenHaveRepresentation()).thenReturn(null);
        when(previousEventData.getChildrenHaveRepresentation()).thenReturn(null);
        when(mainRepValidator.validate(caseData)).thenReturn(NO_SOLICITOR_ERRORS);

        assertThatThrownBy(() -> underTest.validate(caseData, caseDataBefore))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("The field childrenHaveRepresentation should not be null");
    }

    @DisplayName("Validate with no errors when the children representation was set to No and hasn't changed")
    @Test
    void validateNoChangeSetWhenNo() {
        when(caseData.getChildrenEventData()).thenReturn(currentEventData);
        when(caseDataBefore.getChildrenEventData()).thenReturn(previousEventData);

        when(currentEventData.getChildrenHaveRepresentation()).thenReturn("No");
        when(previousEventData.getChildrenHaveRepresentation()).thenReturn("No");
        when(mainRepValidator.validate(caseData)).thenReturn(NO_SOLICITOR_ERRORS);

        assertThat(underTest.validate(caseData, caseDataBefore)).isEmpty();
    }

    @DisplayName("Validate with no errors when the children representation was set to Yes and hasn't changed")
    @Test
    void validateNoChangeSetWhenYes() {
        when(caseData.getChildrenEventData()).thenReturn(currentEventData);
        when(caseDataBefore.getChildrenEventData()).thenReturn(previousEventData);

        when(currentEventData.getChildrenHaveRepresentation()).thenReturn("Yes");
        when(previousEventData.getChildrenHaveRepresentation()).thenReturn("Yes");

        assertThat(underTest.validate(caseData, caseDataBefore)).isEmpty();
    }

    @DisplayName("Validate with no errors when the children representation was not set and now is set to No")
    @Test
    void validateNotSetToSetToNo() {
        when(caseData.getChildrenEventData()).thenReturn(currentEventData);
        when(caseDataBefore.getChildrenEventData()).thenReturn(previousEventData);

        when(currentEventData.getChildrenHaveRepresentation()).thenReturn("No");
        when(previousEventData.getChildrenHaveRepresentation()).thenReturn(null);
        when(mainRepValidator.validate(caseData)).thenReturn(NO_SOLICITOR_ERRORS);

        assertThat(underTest.validate(caseData, caseDataBefore)).isEmpty();
    }

    @DisplayName("Validate with no errors when the children representation was not set and now is set to Yes")
    @Test
    void validateNotSetToSetToYes() {
        when(caseData.getChildrenEventData()).thenReturn(currentEventData);
        when(caseDataBefore.getChildrenEventData()).thenReturn(previousEventData);

        when(currentEventData.getChildrenHaveRepresentation()).thenReturn("Yes");
        when(previousEventData.getChildrenHaveRepresentation()).thenReturn(null);
        when(mainRepValidator.validate(caseData)).thenReturn(NO_SOLICITOR_ERRORS);

        assertThat(underTest.validate(caseData, caseDataBefore)).isEmpty();
    }

    @DisplayName("Validate with no errors when the children representation was to set No and now is set to Yes")
    @Test
    void validateSetToNoToSetToYes() {
        when(caseData.getChildrenEventData()).thenReturn(currentEventData);
        when(caseDataBefore.getChildrenEventData()).thenReturn(previousEventData);

        when(currentEventData.getChildrenHaveRepresentation()).thenReturn("Yes");
        when(previousEventData.getChildrenHaveRepresentation()).thenReturn("No");
        when(mainRepValidator.validate(caseData)).thenReturn(NO_SOLICITOR_ERRORS);

        assertThat(underTest.validate(caseData, caseDataBefore)).isEmpty();
    }

    @DisplayName("Validate with errors when the children representation was set to Yes and is now No")
    @Test
    void validateSetToNotSet() {
        when(caseData.getChildrenEventData()).thenReturn(currentEventData);
        when(caseDataBefore.getChildrenEventData()).thenReturn(previousEventData);

        when(currentEventData.getChildrenHaveRepresentation()).thenReturn("No");
        when(previousEventData.getChildrenHaveRepresentation()).thenReturn("Yes");

        assertThat(underTest.validate(caseData, caseDataBefore))
            .isEqualTo(List.of("You cannot remove the main representative from the case"));
    }

    @DisplayName("Validate with no errors when the main solicitor was not changed")
    @Test
    void validateSolicitorNotChanged() {
        when(caseData.getChildrenEventData()).thenReturn(currentEventData);
        when(caseDataBefore.getChildrenEventData()).thenReturn(previousEventData);

        when(currentEventData.getChildrenHaveRepresentation()).thenReturn("Yes");
        when(currentEventData.getChildrenMainRepresentative()).thenReturn(previousSolicitor);
        when(previousEventData.getChildrenHaveRepresentation()).thenReturn("Yes");
        when(previousEventData.getChildrenMainRepresentative()).thenReturn(previousSolicitor);

        assertThat(underTest.validate(caseData, caseDataBefore)).isEmpty();
    }

    @DisplayName("Validate with errors when the main solicitor was changed")
    @Test
    void validateSolicitorChanged() {
        when(caseData.getChildrenEventData()).thenReturn(currentEventData);
        when(caseDataBefore.getChildrenEventData()).thenReturn(previousEventData);

        when(currentEventData.getChildrenHaveRepresentation()).thenReturn("Yes");
        when(currentEventData.getChildrenMainRepresentative()).thenReturn(currentSolicitor);
        when(previousEventData.getChildrenHaveRepresentation()).thenReturn("Yes");
        when(previousEventData.getChildrenMainRepresentative()).thenReturn(previousSolicitor);

        when(sanitizer.sanitize(currentSolicitor)).thenReturn(currentSolicitor);
        when(sanitizer.sanitize(previousSolicitor)).thenReturn(previousSolicitor);

        assertThat(underTest.validate(caseData, caseDataBefore))
            .isEqualTo(List.of("You cannot change the main representative"));
    }

    @DisplayName("Validate with errors when the main solicitor is newly set and has invalid details")
    @Test
    void validateSolicitorDetails() {
        when(caseData.getChildrenEventData()).thenReturn(currentEventData);
        when(caseDataBefore.getChildrenEventData()).thenReturn(previousEventData);

        when(currentEventData.getChildrenHaveRepresentation()).thenReturn("Yes");
        when(previousEventData.getChildrenHaveRepresentation()).thenReturn(null);
        when(mainRepValidator.validate(caseData)).thenReturn(SOLICITOR_ERRORS);

        assertThat(underTest.validate(caseData, caseDataBefore)).isEqualTo(SOLICITOR_ERRORS);
    }
}
