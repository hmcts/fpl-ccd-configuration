package uk.gov.hmcts.reform.fpl.service.children.validation.representative;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.children.ChildRepresentationDetails;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.ChildrenEventData;
import uk.gov.hmcts.reform.fpl.service.UserService;
import uk.gov.hmcts.reform.fpl.service.children.validation.ChildrenEventSection;
import uk.gov.hmcts.reform.fpl.service.representative.RepresentativeSolicitorSanitizer;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.service.children.validation.ChildrenEventSection.CHILD_REPRESENTATIVES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

class LocalAuthorityUserIndividualRepresentativeValidatorTest {
    private static final List<Element<Child>> CHILDREN = List.of(element(mock(Child.class)));

    private final CaseData caseData = mock(CaseData.class);
    private final CaseData caseDataBefore = mock(CaseData.class);
    private final ChildrenEventData eventData = mock(ChildrenEventData.class);
    private final ChildrenEventData eventDataBefore = mock(ChildrenEventData.class);
    private final ChildRepresentationDetails details = mock(ChildRepresentationDetails.class);
    private final ChildRepresentationDetails changedDetails = mock(ChildRepresentationDetails.class);
    private final RespondentSolicitor solicitor = mock(RespondentSolicitor.class);
    private final RespondentSolicitor differentSolicitor = mock(RespondentSolicitor.class);

    private final UserService user = mock(UserService.class);
    private final ChildRepresentativeValidator childRepValidator = mock(ChildRepresentativeValidator.class);
    private final RepresentativeSolicitorSanitizer sanitizer = mock(RepresentativeSolicitorSanitizer.class);

    private final LocalAuthorityUserIndividualRepresentativeValidator underTest =
        new LocalAuthorityUserIndividualRepresentativeValidator(user, childRepValidator, sanitizer);

    @DisplayName("Accept users that do not have HMCTS roles when section is CHILD_REPRESENTATIVES")
    @Test
    void acceptsNonHMCTS() {
        when(user.isHmctsUser()).thenReturn(false);

        assertThat(underTest.accepts(CHILD_REPRESENTATIVES)).isTrue();
    }

    @DisplayName("Reject users that have HMCTS roles when section is CHILD_REPRESENTATIVES")
    @Test
    void acceptsHMCTS() {
        when(user.isHmctsUser()).thenReturn(true);

        assertThat(underTest.accepts(CHILD_REPRESENTATIVES)).isFalse();
    }

    @DisplayName("Reject when the user is valid but the given section is not CHILD_REPRESENTATIVES")
    @ParameterizedTest(name = "when section is {0}")
    @EnumSource(value = ChildrenEventSection.class, mode = EnumSource.Mode.EXCLUDE, names = {"CHILD_REPRESENTATIVES"})
    void acceptsDifferentSection(ChildrenEventSection section) {
        when(user.isHmctsUser()).thenReturn(false);

        assertThat(underTest.accepts(section)).isFalse();
    }

    @DisplayName("Validate with exception when nothing changes and nothing has been set")
    @Test
    void validateStaysNotSet() {
        when(caseData.getChildrenEventData()).thenReturn(eventData);
        when(caseDataBefore.getChildrenEventData()).thenReturn(eventDataBefore);

        when(eventData.getChildrenHaveSameRepresentation()).thenReturn(null);
        when(eventDataBefore.getChildrenHaveSameRepresentation()).thenReturn(null);

        assertThatThrownBy(() -> underTest.validate(caseData, caseDataBefore))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("The field childrenHaveSameRepresentation should not be null");
    }

    @DisplayName("Validate with no errors when user initially selects all children don't have same representative and"
                 + "the details have no errors ")
    @Test
    void validateNotSetToSetToNoWithNoErrors() {
        when(caseData.getChildrenEventData()).thenReturn(eventData);
        when(caseDataBefore.getChildrenEventData()).thenReturn(eventDataBefore);

        when(eventData.getChildrenHaveSameRepresentation()).thenReturn("No");
        when(eventDataBefore.getChildrenHaveSameRepresentation()).thenReturn(null);

        when(childRepValidator.validate(caseData)).thenReturn(List.of());

        assertThat(underTest.validate(caseData, caseDataBefore)).isEmpty();
    }

    @DisplayName("Validate with no errors when user initially selects all children don't have same representative and"
                 + "the details have errors ")
    @Test
    void validateNotSetToSetToNoWithErrors() {
        when(caseData.getChildrenEventData()).thenReturn(eventData);
        when(caseDataBefore.getChildrenEventData()).thenReturn(eventDataBefore);

        when(eventData.getChildrenHaveSameRepresentation()).thenReturn("No");
        when(eventDataBefore.getChildrenHaveSameRepresentation()).thenReturn(null);

        when(childRepValidator.validate(caseData)).thenReturn(List.of("error"));

        assertThat(underTest.validate(caseData, caseDataBefore)).isEqualTo(List.of("error"));
    }

    @DisplayName("Validate with no errors when nothing changes and nothing has been set")
    @Test
    void validateNotSetToSetToYes() {
        when(caseData.getChildrenEventData()).thenReturn(eventData);
        when(caseDataBefore.getChildrenEventData()).thenReturn(eventDataBefore);

        when(eventData.getChildrenHaveSameRepresentation()).thenReturn("Yes");
        when(eventDataBefore.getChildrenHaveSameRepresentation()).thenReturn(null);

        assertThat(underTest.validate(caseData, caseDataBefore)).isEmpty();
    }

    @DisplayName("Validate with errors when the children had different representation and this is changed")
    @Test
    void validateSetToNoAndChanged() {
        when(caseData.getChildrenEventData()).thenReturn(eventData);
        when(caseDataBefore.getChildrenEventData()).thenReturn(eventDataBefore);

        when(eventData.getChildrenHaveSameRepresentation()).thenReturn("No");
        when(eventDataBefore.getChildrenHaveSameRepresentation()).thenReturn("Yes");

        assertThat(underTest.validate(caseData, caseDataBefore))
            .isEqualTo(List.of("You cannot change a child's legal representative"));
    }

    @DisplayName("Validate with errors when the children had the same representation and this is changed")
    @Test
    void validateSetToYesAndChanged() {
        when(caseData.getChildrenEventData()).thenReturn(eventData);
        when(caseDataBefore.getChildrenEventData()).thenReturn(eventDataBefore);

        when(eventData.getChildrenHaveSameRepresentation()).thenReturn("Yes");
        when(eventDataBefore.getChildrenHaveSameRepresentation()).thenReturn("No");

        assertThat(underTest.validate(caseData, caseDataBefore))
            .isEqualTo(List.of("You cannot change a child's legal representative"));
    }

    @DisplayName("Validate with no errors when nothing changes and the children have the same representative")
    @Test
    void validateNoChangeSetYes() {
        when(caseData.getChildrenEventData()).thenReturn(eventData);
        when(caseDataBefore.getChildrenEventData()).thenReturn(eventDataBefore);

        when(eventData.getChildrenHaveSameRepresentation()).thenReturn("Yes");
        when(eventDataBefore.getChildrenHaveSameRepresentation()).thenReturn("Yes");

        assertThat(underTest.validate(caseData, caseDataBefore)).isEmpty();
    }

    @DisplayName("Validate with no errors when nothing changes and the children have different representatives which "
                 + "are not using the main solicitor")
    @Test
    void validateNoChangeSetNoNotMainSolicitor() {
        when(caseData.getChildrenEventData()).thenReturn(eventData);
        when(caseDataBefore.getChildrenEventData()).thenReturn(eventDataBefore);

        when(eventData.getChildrenHaveSameRepresentation()).thenReturn("No");
        when(eventDataBefore.getChildrenHaveSameRepresentation()).thenReturn("No");

        when(caseData.getAllChildren()).thenReturn(CHILDREN);

        when(eventData.getAllRepresentationDetails()).thenReturn(List.of(details));
        when(eventDataBefore.getAllRepresentationDetails()).thenReturn(List.of(details));

        when(details.getUseMainSolicitor()).thenReturn("No");
        when(details.getSolicitor()).thenReturn(solicitor);

        when(sanitizer.sanitize(solicitor)).thenReturn(solicitor);

        assertThat(underTest.validate(caseData, caseDataBefore)).isEmpty();
    }

    @DisplayName("Validate with no errors when nothing changes and the children have different representatives which "
                 + "are using the main solicitor")
    @Test
    void validateNoChangeSetNoWithMainSolicitor() {
        when(caseData.getChildrenEventData()).thenReturn(eventData);
        when(caseDataBefore.getChildrenEventData()).thenReturn(eventDataBefore);

        when(eventData.getChildrenHaveSameRepresentation()).thenReturn("No");
        when(eventDataBefore.getChildrenHaveSameRepresentation()).thenReturn("No");

        when(caseData.getAllChildren()).thenReturn(CHILDREN);

        when(eventData.getAllRepresentationDetails()).thenReturn(List.of(details));
        when(eventDataBefore.getAllRepresentationDetails()).thenReturn(List.of(details));

        when(details.getUseMainSolicitor()).thenReturn("Yes");

        assertThat(underTest.validate(caseData, caseDataBefore)).isEmpty();
    }

    @DisplayName("Validate with with errors when the children have different representatives and details changed with"
                 + " main representative being used changed from yes to no")
    @Test
    void validateSetNoWithChangedRepresentativeUseMainFromYesToNo() {
        when(caseData.getChildrenEventData()).thenReturn(eventData);
        when(caseDataBefore.getChildrenEventData()).thenReturn(eventDataBefore);

        when(eventData.getChildrenHaveSameRepresentation()).thenReturn("No");
        when(eventDataBefore.getChildrenHaveSameRepresentation()).thenReturn("No");

        when(caseData.getAllChildren()).thenReturn(CHILDREN);

        when(eventData.getAllRepresentationDetails()).thenReturn(List.of(changedDetails));
        when(eventDataBefore.getAllRepresentationDetails()).thenReturn(List.of(details));

        when(details.getUseMainSolicitor()).thenReturn("Yes");
        when(changedDetails.getUseMainSolicitor()).thenReturn("No");

        assertThat(underTest.validate(caseData, caseDataBefore))
            .isEqualTo(List.of("You cannot change a child's legal representative"));
    }

    @DisplayName("Validate with with errors when the children have different representatives and details changed with"
                 + " main representative being used changed from no to yes")
    @Test
    void validateSetNoWithChangedRepresentativeUseMainFromNoToYes() {
        when(caseData.getChildrenEventData()).thenReturn(eventData);
        when(caseDataBefore.getChildrenEventData()).thenReturn(eventDataBefore);

        when(eventData.getChildrenHaveSameRepresentation()).thenReturn("No");
        when(eventDataBefore.getChildrenHaveSameRepresentation()).thenReturn("No");

        when(caseData.getAllChildren()).thenReturn(CHILDREN);

        when(eventData.getAllRepresentationDetails()).thenReturn(List.of(changedDetails));
        when(eventDataBefore.getAllRepresentationDetails()).thenReturn(List.of(details));

        when(details.getUseMainSolicitor()).thenReturn("No");
        when(changedDetails.getUseMainSolicitor()).thenReturn("Yes");

        assertThat(underTest.validate(caseData, caseDataBefore))
            .isEqualTo(List.of("You cannot change a child's legal representative"));
    }

    @DisplayName("Validate with with errors when the children have different representatives and details solicitor "
                 + "changed")
    @Test
    void validateSetNoWithChangedRepresentativeDetailsSolicitorChanged() {
        when(caseData.getChildrenEventData()).thenReturn(eventData);
        when(caseDataBefore.getChildrenEventData()).thenReturn(eventDataBefore);

        when(eventData.getChildrenHaveSameRepresentation()).thenReturn("No");
        when(eventDataBefore.getChildrenHaveSameRepresentation()).thenReturn("No");

        when(caseData.getAllChildren()).thenReturn(CHILDREN);

        when(eventData.getAllRepresentationDetails()).thenReturn(List.of(changedDetails));
        when(eventDataBefore.getAllRepresentationDetails()).thenReturn(List.of(details));

        when(details.getUseMainSolicitor()).thenReturn("No");
        when(changedDetails.getUseMainSolicitor()).thenReturn("No");

        when(details.getSolicitor()).thenReturn(solicitor);
        when(changedDetails.getSolicitor()).thenReturn(differentSolicitor);

        when(sanitizer.sanitize(solicitor)).thenReturn(solicitor);
        when(sanitizer.sanitize(differentSolicitor)).thenReturn(differentSolicitor);

        assertThat(underTest.validate(caseData, caseDataBefore))
            .isEqualTo(List.of("You cannot change a child's legal representative"));
    }

    @DisplayName("Validate with with exception when the children have different representatives and details have main"
                 + " representative set to null")
    @Test
    void validateSetNoWithChangedRepresentativeUseMainIsNull() {
        when(caseData.getChildrenEventData()).thenReturn(eventData);
        when(caseDataBefore.getChildrenEventData()).thenReturn(eventDataBefore);

        when(eventData.getChildrenHaveSameRepresentation()).thenReturn("No");
        when(eventDataBefore.getChildrenHaveSameRepresentation()).thenReturn("No");

        when(caseData.getAllChildren()).thenReturn(CHILDREN);

        when(eventData.getAllRepresentationDetails()).thenReturn(List.of(changedDetails));
        when(eventDataBefore.getAllRepresentationDetails()).thenReturn(List.of(details));

        when(details.getUseMainSolicitor()).thenReturn(null);

        assertThatThrownBy(() -> underTest.validate(caseData, caseDataBefore))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("The field useMainSolicitor in childRepresentationDetails0 should not be null");
    }
}
