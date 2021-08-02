package uk.gov.hmcts.reform.fpl.service.children.validation.representative;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.ChildrenEventData;
import uk.gov.hmcts.reform.fpl.service.UserService;
import uk.gov.hmcts.reform.fpl.service.children.validation.ChildrenEventSection;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.service.children.validation.ChildrenEventSection.MAIN_REPRESENTATIVE;

class AdminUserMainRepresentativeValidatorTest {
    private static final List<String> NO_SOLICITOR_ERRORS = List.of();
    private static final List<String> SOLICITOR_ERRORS = List.of("error");

    private final CaseData caseData = mock(CaseData.class);
    private final CaseData caseDataBefore = mock(CaseData.class);

    private final ChildrenEventData eventData = mock(ChildrenEventData.class);

    private final UserService user = mock(UserService.class);
    private final MainRepresentativeValidator mainRepValidator = mock(MainRepresentativeValidator.class);

    private final AdminUserMainRepresentativeValidator underTest = new AdminUserMainRepresentativeValidator(
        user, mainRepValidator
    );

    @DisplayName("Accept users that are admins when section is MAIN_REPRESENTATIVE")
    @Test
    void acceptsAdmin() {
        when(user.isHmctsAdminUser()).thenReturn(true);

        assertThat(underTest.accepts(MAIN_REPRESENTATIVE)).isTrue();
    }

    @DisplayName("Reject users that are not admins when section is MAIN_REPRESENTATIVE")
    @Test
    void acceptsNonAdmin() {
        when(user.isHmctsAdminUser()).thenReturn(false);

        assertThat(underTest.accepts(MAIN_REPRESENTATIVE)).isFalse();
    }

    @DisplayName("Reject when the user is valid but the given section is not MAIN_REPRESENTATIVE")
    @ParameterizedTest(name = "when section is {0}")
    @EnumSource(value = ChildrenEventSection.class, mode = EnumSource.Mode.EXCLUDE, names = {"MAIN_REPRESENTATIVE"})
    void acceptsDifferentSection(ChildrenEventSection section) {
        when(user.isHmctsAdminUser()).thenReturn(true);

        assertThat(underTest.accepts(section)).isFalse();
    }

    @DisplayName("Validate with exception when the children representation wasn't set and still isn't")
    @Test
    void validateNoChangeNotSet() {
        when(caseData.getChildrenEventData()).thenReturn(eventData);
        when(caseDataBefore.getChildrenEventData()).thenReturn(eventData);

        when(eventData.getChildrenHaveRepresentation()).thenReturn(null);
        when(mainRepValidator.validate(caseData)).thenReturn(NO_SOLICITOR_ERRORS);

        assertThatThrownBy(() -> underTest.validate(caseData, caseDataBefore))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("The field childrenHaveRepresentation should not be null");
    }

    @DisplayName("Validate with no errors when the children representation was set to No and hasn't changed")
    @Test
    void validateNoChangeSetWhenNo() {
        when(caseData.getChildrenEventData()).thenReturn(eventData);
        when(caseDataBefore.getChildrenEventData()).thenReturn(eventData);

        when(eventData.getChildrenHaveRepresentation()).thenReturn("No");
        when(mainRepValidator.validate(caseData)).thenReturn(NO_SOLICITOR_ERRORS);

        assertThat(underTest.validate(caseData, caseDataBefore)).isEmpty();
    }

    @DisplayName("Validate with no errors when the children representation was set to Yes and hasn't changed")
    @Test
    void validateNoChangeSetWhenYes() {
        when(caseData.getChildrenEventData()).thenReturn(eventData);
        when(caseDataBefore.getChildrenEventData()).thenReturn(eventData);

        when(eventData.getChildrenHaveRepresentation()).thenReturn("Yes");
        when(mainRepValidator.validate(caseData)).thenReturn(NO_SOLICITOR_ERRORS);

        assertThat(underTest.validate(caseData, caseDataBefore)).isEmpty();
    }

    @DisplayName("Validate with no errors when the children representation was not set and now is set to No")
    @Test
    void validateNotSetToSetToNo() {
        when(caseData.getChildrenEventData()).thenReturn(eventData);
        when(caseDataBefore.getChildrenEventData()).thenReturn(eventData);

        when(eventData.getChildrenHaveRepresentation()).thenReturn(null, "No");
        when(mainRepValidator.validate(caseData)).thenReturn(NO_SOLICITOR_ERRORS);

        assertThat(underTest.validate(caseData, caseDataBefore)).isEmpty();
    }

    @DisplayName("Validate with no errors when the children representation was not set and now is set to Yes")
    @Test
    void validateNotSetToSetToYes() {
        when(caseData.getChildrenEventData()).thenReturn(eventData);
        when(caseDataBefore.getChildrenEventData()).thenReturn(eventData);

        when(eventData.getChildrenHaveRepresentation()).thenReturn(null, "Yes");
        when(mainRepValidator.validate(caseData)).thenReturn(NO_SOLICITOR_ERRORS);

        assertThat(underTest.validate(caseData, caseDataBefore)).isEmpty();
    }

    @DisplayName("Validate with no errors when the children representation was to set No and now is set to Yes")
    @Test
    void validateSetToNoToSetToYes() {
        when(caseData.getChildrenEventData()).thenReturn(eventData);
        when(caseDataBefore.getChildrenEventData()).thenReturn(eventData);

        when(eventData.getChildrenHaveRepresentation()).thenReturn("No", "Yes");
        when(mainRepValidator.validate(caseData)).thenReturn(NO_SOLICITOR_ERRORS);

        assertThat(underTest.validate(caseData, caseDataBefore)).isEmpty();
    }

    @DisplayName("Validate with errors when the children representation was set to Yes and is now No")
    @Test
    void validateSetToNotSet() {
        when(caseData.getChildrenEventData()).thenReturn(eventData);
        when(caseDataBefore.getChildrenEventData()).thenReturn(eventData);

        when(eventData.getChildrenHaveRepresentation()).thenReturn("Yes", "No");

        assertThat(underTest.validate(caseData, caseDataBefore))
            .isEqualTo(List.of("You cannot remove the main representative from the case"));
    }

    @DisplayName("Validate with errors when the children representation was not set and solicitor has invalid details")
    @Test
    void validateInvalidDetailWhenSetPreviously() {
        when(caseData.getChildrenEventData()).thenReturn(eventData);
        when(caseDataBefore.getChildrenEventData()).thenReturn(eventData);

        when(eventData.getChildrenHaveRepresentation()).thenReturn("Yes");
        when(mainRepValidator.validate(caseData)).thenReturn(SOLICITOR_ERRORS);

        assertThat(underTest.validate(caseData, caseDataBefore)).isEqualTo(SOLICITOR_ERRORS);
    }

    @DisplayName("Validate with errors when the children representation is Yes and solicitor has invalid details")
    @Test
    void validateInvalidDetailWhenNotSetPreviously() {
        when(caseData.getChildrenEventData()).thenReturn(eventData);
        when(caseDataBefore.getChildrenEventData()).thenReturn(eventData);

        when(eventData.getChildrenHaveRepresentation()).thenReturn(null, "Yes");
        when(mainRepValidator.validate(caseData)).thenReturn(SOLICITOR_ERRORS);

        assertThat(underTest.validate(caseData, caseDataBefore)).isEqualTo(SOLICITOR_ERRORS);
    }
}
