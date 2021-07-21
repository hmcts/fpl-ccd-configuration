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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.service.children.validation.ChildrenEventSection.CHILD_REPRESENTATIVES;

class HMCTSAdminUserIndividualRepresentativeValidatorTest {
    private final CaseData caseData = mock(CaseData.class);
    private final CaseData caseDataBefore = mock(CaseData.class);

    private final ChildrenEventData eventData = mock(ChildrenEventData.class);

    private final UserService user = mock(UserService.class);
    private final ChildRepresentativeValidator childRepValidator = mock(ChildRepresentativeValidator.class);

    private final HMCTSAdminUserIndividualRepresentativeValidator underTest =
        new HMCTSAdminUserIndividualRepresentativeValidator(user, childRepValidator);

    @DisplayName("Accept users that are admins when section is CHILD_REPRESENTATIVES")
    @Test
    void acceptsAdmin() {
        when(user.isHmctsAdminUser()).thenReturn(true);

        assertThat(underTest.accepts(CHILD_REPRESENTATIVES)).isTrue();
    }

    @DisplayName("Reject users that are not admins when section is CHILD_REPRESENTATIVES")
    @Test
    void acceptsNonAdmin() {
        when(user.isHmctsAdminUser()).thenReturn(false);

        assertThat(underTest.accepts(CHILD_REPRESENTATIVES)).isFalse();
    }

    @DisplayName("Reject when the user is valid but the given section is not CHILD_REPRESENTATIVES")
    @ParameterizedTest(name = "when section is {0}")
    @EnumSource(value = ChildrenEventSection.class, mode = EnumSource.Mode.EXCLUDE, names = {"CHILD_REPRESENTATIVES"})
    void acceptsDifferentSection(ChildrenEventSection section) {
        when(user.isHmctsAdminUser()).thenReturn(true);

        assertThat(underTest.accepts(section)).isFalse();
    }

    @DisplayName("Validate with exception when children have same representative is not set")
    @Test
    void validateNoErrorNotSet() {
        when(caseData.getChildrenEventData()).thenReturn(eventData);
        when(eventData.getChildrenHaveSameRepresentation()).thenReturn(null);

        assertThatThrownBy(() -> underTest.validate(caseData, caseDataBefore))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("The field childrenHaveSameRepresentation should not be null");
    }

    @DisplayName("Validate with no errors when the all children have same representative")
    @Test
    void validateNoErrorSetYes() {
        when(caseData.getChildrenEventData()).thenReturn(eventData);
        when(eventData.getChildrenHaveSameRepresentation()).thenReturn("Yes");

        assertThat(underTest.validate(caseData, caseDataBefore)).isEmpty();

        verifyNoInteractions(childRepValidator);
    }

    @DisplayName("Validate with no errors when the children's representative details are valid")
    @Test
    void validateNoErrorSetNo() {
        when(caseData.getChildrenEventData()).thenReturn(eventData);
        when(eventData.getChildrenHaveSameRepresentation()).thenReturn("No");

        when(childRepValidator.validate(caseData)).thenReturn(List.of());

        assertThat(underTest.validate(caseData, caseDataBefore)).isEmpty();
    }

    @DisplayName("Validate with errors when the children's representative details are invalid")
    @Test
    void validateErrors() {
        when(caseData.getChildrenEventData()).thenReturn(eventData);
        when(eventData.getChildrenHaveSameRepresentation()).thenReturn("No");

        when(childRepValidator.validate(caseData)).thenReturn(List.of("error"));

        assertThat(underTest.validate(caseData, caseDataBefore)).isEqualTo(List.of("error"));
    }
}
