package uk.gov.hmcts.reform.fpl.service.children.validation.collection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.UserService;
import uk.gov.hmcts.reform.fpl.service.children.validation.ChildrenEventSection;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.service.children.validation.ChildrenEventSection.COLLECTION;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

class HMCTSAdminUserChildCollectionValidatorTest {

    private final CaseData caseData = mock(CaseData.class);
    private final CaseData caseDataBefore = mock(CaseData.class);

    private final UUID child1Id = UUID.randomUUID();
    private final Child child1 = mock(Child.class);
    private final Element<Child> child1Element = element(child1Id, child1);

    private final UUID child2Id = UUID.randomUUID();
    private final Child child2 = mock(Child.class);
    private final Element<Child> child2Element = element(child2Id, child2);

    private final UserService user = mock(UserService.class);

    private final HMCTSAdminUserChildCollectionValidator underTest = new HMCTSAdminUserChildCollectionValidator(user);

    @DisplayName("Accept users that are admins when section is COLLECTION")
    @Test
    void acceptsAdmin() {
        when(user.isHmctsAdminUser()).thenReturn(true);

        assertThat(underTest.accepts(COLLECTION)).isTrue();
    }

    @DisplayName("Reject users that are not admins when section is COLLECTION")
    @Test
    void acceptsNonAdmin() {
        when(user.isHmctsAdminUser()).thenReturn(false);

        assertThat(underTest.accepts(COLLECTION)).isFalse();
    }

    @DisplayName("Reject when the user is valid but the given section is not COLLECTION")
    @ParameterizedTest(name = "when section is {0}")
    @EnumSource(value = ChildrenEventSection.class, mode = EnumSource.Mode.EXCLUDE, names = {"COLLECTION"})
    void acceptsDifferentSection(ChildrenEventSection section) {
        when(user.isHmctsAdminUser()).thenReturn(true);

        assertThat(underTest.accepts(section)).isFalse();
    }

    @DisplayName("Validate with no errors when nothing changes")
    @Test
    void validateNoChange() {
        when(caseData.getAllChildren()).thenReturn(List.of(child1Element));
        when(caseDataBefore.getAllChildren()).thenReturn(List.of(child1Element));

        assertThat(underTest.validate(caseData, caseDataBefore)).isEmpty();
    }

    @DisplayName("Validate with no errors when a child is added")
    @Test
    void validateAdded() {
        when(caseData.getAllChildren()).thenReturn(List.of(child1Element, child2Element));
        when(caseDataBefore.getAllChildren()).thenReturn(List.of(child1Element));

        assertThat(underTest.validate(caseData, caseDataBefore)).isEmpty();
    }

    @DisplayName("Validate with errors when a child is removed")
    @Test
    void validateRemoved() {
        when(caseData.getAllChildren()).thenReturn(List.of());
        when(caseDataBefore.getAllChildren()).thenReturn(List.of(child1Element, child2Element));

        ChildParty party = mock(ChildParty.class);
        when(child1.getParty()).thenReturn(party);
        when(child2.getParty()).thenReturn(party);
        when(party.getFullName()).thenReturn("child 1 name", "child 2 name");

        assertThat(underTest.validate(caseData, caseDataBefore)).isEqualTo(List.of(
            "You cannot remove child 1 name from the case", "You cannot remove child 2 name from the case"
        ));
    }

    @DisplayName("Validate with no errors when a child is updated")
    @Test
    void validateUpdated() {
        when(caseData.getAllChildren()).thenReturn(List.of(child1Element, element(child2Id, child1)));
        when(caseDataBefore.getAllChildren()).thenReturn(List.of(child1Element, child2Element));

        assertThat(underTest.validate(caseData, caseDataBefore)).isEmpty();
    }
}
