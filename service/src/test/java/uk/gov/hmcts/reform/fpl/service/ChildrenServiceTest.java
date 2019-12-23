package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.enums.PartyType;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.EmailAddress;
import uk.gov.hmcts.reform.fpl.model.common.Telephone;

import java.util.List;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
class ChildrenServiceTest {
    private static final UUID ID = randomUUID();

    private final ChildrenService service = new ChildrenService();

    @Test
    void shouldAddEmptyElementWhenChildrenIsEmpty() {
        List<Element<Child>> children = service.prepareChildren(CaseData.builder().build());

        assertThat(getParty(children, 0).partyId).isNotNull();
    }

    @Test
    void shouldReturnChildrenIfChildrenIsPrePopulated() {
        CaseData caseData = CaseData.builder()
            .children1(ImmutableList.of(childWithRemovedConfidentialFields(ID)))
            .build();

        List<Element<Child>> children = service.prepareChildren(caseData);

        assertThat(children).containsExactly(childWithRemovedConfidentialFields(ID));
    }

    @Test
    void shouldPrepareChildWithConfidentialValuesWhenConfidentialChildrenIsNotEmpty() {
        CaseData caseData = CaseData.builder()
            .children1(ImmutableList.of(childWithRemovedConfidentialFields(ID)))
            .confidentialChildren(ImmutableList.of(childWithConfidentialFields(ID)))
            .build();

        List<Element<Child>> children = service.prepareChildren(caseData);

        assertThat(children).containsOnly(childWithConfidentialFields(ID));
    }

    @Test
    void shouldReturnChildWithoutConfidentialDetailsWhenThereIsNoMatchingConfidentialChild() {
        CaseData caseData = CaseData.builder()
            .children1(ImmutableList.of(childWithRemovedConfidentialFields(ID)))
            .confidentialChildren(ImmutableList.of(childWithConfidentialFields(randomUUID())))
            .build();

        List<Element<Child>> children = service.prepareChildren(caseData);

        assertThat(children).containsOnly(childWithRemovedConfidentialFields(ID));
    }

    @Test
    void shouldAddExpectedChildWhenHiddenDetailsMarkedAsNo() {
        CaseData caseData = CaseData.builder()
            .children1(ImmutableList.of(childWithDetailsHiddenNo(ID)))
            .confidentialChildren(ImmutableList.of(childWithConfidentialFields(ID)))
            .build();

        List<Element<Child>> children = service.prepareChildren(caseData);

        assertThat(children).containsOnly(childWithDetailsHiddenNo(ID));
    }

    @Test
    void shouldMaintainOrderingOfChildrenWhenComplexScenario() {
        UUID otherId = randomUUID();

        List<Element<Child>> children = ImmutableList.of(
            childWithRemovedConfidentialFields(ID),
            childWithDetailsHiddenNo(randomUUID()),
            childWithRemovedConfidentialFields(otherId));

        List<Element<Child>> confidentialChildren = ImmutableList.of(
            childWithConfidentialFields(ID),
            childWithConfidentialFields(otherId));

        CaseData caseData = CaseData.builder()
            .children1(children)
            .confidentialChildren(confidentialChildren)
            .build();

        List<Element<Child>> updatedChildren = service.prepareChildren(caseData);

        assertThat(updatedChildren.get(0)).isEqualTo(confidentialChildren.get(0));
        assertThat(updatedChildren.get(1)).isEqualTo(children.get(1));
        assertThat(updatedChildren.get(2)).isEqualTo(confidentialChildren.get(1));
    }

    @Test
    void shouldAddPartyIDAndPartyTypeValuesToSingleChild() {
        List<Element<Child>> children = ImmutableList.of(
            Element.<Child>builder()
                .id(randomUUID())
                .value(Child.builder()
                    .party(ChildParty.builder()
                        .firstName("James")
                        .build())
                    .build())
                .build());

        CaseData caseData = CaseData.builder()
            .children1(children)
            .build();

        List<Element<Child>> updatedChildren = service.modifyHiddenValues(caseData.getAllChildren());

        assertThat(getParty(updatedChildren, 0).firstName).isEqualTo("James");
        assertThat(getParty(updatedChildren, 0).partyType).isEqualTo(PartyType.INDIVIDUAL);
        assertThat(getParty(updatedChildren, 0).partyId).isNotNull();
    }

    @Test
    void shouldAddPartyIDAndPartyTypeValuesToMultipleChildren() {
        List<Element<Child>> children = ImmutableList.of(
            Element.<Child>builder()
                .id(randomUUID())
                .value(Child.builder()
                    .party(ChildParty.builder()
                        .firstName("James")
                        .build())
                    .build())
                .build(),
            Element.<Child>builder()
                .id(randomUUID())
                .value(Child.builder()
                    .party(ChildParty.builder()
                        .firstName("Lucy")
                        .build())
                    .build())
                .build()
        );

        CaseData caseData = CaseData.builder()
            .children1(children)
            .build();
        List<Element<Child>> updatedChildren = service.modifyHiddenValues(caseData.getAllChildren());

        assertThat(getParty(updatedChildren, 0).firstName).isEqualTo("James");
        assertThat(getParty(updatedChildren, 0).partyType).isEqualTo(PartyType.INDIVIDUAL);
        assertThat(getParty(updatedChildren, 0).partyId).isNotNull();

        assertThat(getParty(updatedChildren, 1).firstName).isEqualTo("Lucy");
        assertThat(getParty(updatedChildren, 1).partyType).isEqualTo(PartyType.INDIVIDUAL);
        assertThat(getParty(updatedChildren, 1).partyId).isNotNull();
    }

    @Test
    void shouldKeepExistingPartyID() {
        List<Element<Child>> children = ImmutableList.of(
            Element.<Child>builder()
                .id(randomUUID())
                .value(Child.builder()
                    .party(ChildParty.builder()
                        .firstName("James")
                        .partyId("123")
                        .build())
                    .build())
                .build());

        CaseData caseData = CaseData.builder()
            .children1(children)
            .build();

        List<Element<Child>> updatedChildren = service.modifyHiddenValues(caseData.getAllChildren());

        assertThat(getParty(updatedChildren, 0).partyId).isEqualTo("123");
    }

    @Test
    void shouldKeepExistingPartyIdAndContinueAddingNewPartyId() {
        List<Element<Child>> children = ImmutableList.of(
            Element.<Child>builder()
                .id(randomUUID())
                .value(Child.builder()
                    .party(ChildParty.builder()
                        .firstName("James")
                        .partyId("123")
                        .build())
                    .build())
                .build(),
            Element.<Child>builder()
                .id(randomUUID())
                .value(Child.builder()
                    .party(ChildParty.builder()
                        .firstName("Lucy")
                        .build())
                    .build())
                .build());

        CaseData caseData = CaseData.builder()
            .children1(children)
            .build();

        List<Element<Child>> updatedChildren = service.modifyHiddenValues(caseData.getAllChildren());

        assertThat(getParty(updatedChildren, 0).firstName).isEqualTo("James");
        assertThat(getParty(updatedChildren, 0).partyId).isEqualTo("123");
        assertThat(getParty(updatedChildren, 1).firstName).isEqualTo("Lucy");
        assertThat(getParty(updatedChildren, 1).partyId).isNotNull();
    }

    @Test
    void shouldHideChildAddressDetailsWhenConfidentialitySelected() {
        List<Element<Child>> children = childElementWithDetailsHiddenValue("Yes");

        CaseData caseData = CaseData.builder()
            .children1(children)
            .build();

        List<Element<Child>> updatedChildren = service.modifyHiddenValues(caseData.getAllChildren());

        assertThat(getParty(updatedChildren, 0).address).isNull();
        assertThat(getParty(updatedChildren, 0).email).isNull();
        assertThat(getParty(updatedChildren, 0).telephoneNumber).isNull();
    }

    @Test
    void shouldNotHideChildAddressDetailsWhenConfidentialitySelected() {
        List<Element<Child>> children = childElementWithDetailsHiddenValue("No");

        CaseData caseData = CaseData.builder()
            .children1(children)
            .build();

        List<Element<Child>> updatedChildren = service.modifyHiddenValues(caseData.getAllChildren());

        assertThat(getParty(updatedChildren, 0).address).isNotNull();
        assertThat(getParty(updatedChildren, 0).email).isNotNull();
        assertThat(getParty(updatedChildren, 0).telephoneNumber).isNotNull();
    }

    private Element<Child> childWithDetailsHiddenNo(UUID id) {
        return Element.<Child>builder()
            .id(id)
            .value(Child.builder()
                .party(ChildParty.builder()
                    .firstName("James")
                    .detailsHidden("No")
                    .email(EmailAddress.builder().email("email@email.com").build())
                    .address(Address.builder()
                        .addressLine1("Address Line 1")
                        .build())
                    .telephoneNumber(Telephone.builder().telephoneNumber("01227 831393").build())
                    .build())
                .build())
            .build();
    }

    private Element<Child> childWithRemovedConfidentialFields(UUID id) {
        return Element.<Child>builder()
            .id(id)
            .value(Child.builder()
                .party(ChildParty.builder()
                    .firstName("James")
                    .detailsHidden("Yes")
                    .build())
                .build())
            .build();
    }

    private Element<Child> childWithConfidentialFields(UUID id) {
        return Element.<Child>builder()
            .id(id)
            .value(Child.builder()
                .party(ChildParty.builder()
                    .firstName("James")
                    .detailsHidden("Yes")
                    .email(EmailAddress.builder().email("email@email.com").build())
                    .address(Address.builder()
                        .addressLine1("Address Line 1")
                        .build())
                    .telephoneNumber(Telephone.builder().telephoneNumber("01227 831393").build())
                    .build())
                .build())
            .build();
    }

    private List<Element<Child>> childElementWithDetailsHiddenValue(String hidden) {
        return ImmutableList.of(Element.<Child>builder()
            .id(randomUUID())
            .value(Child.builder()
                .party(ChildParty.builder()
                    .firstName("James")
                    .detailsHidden(hidden)
                    .email(EmailAddress.builder().email("email@email.com").build())
                    .address(Address.builder()
                        .addressLine1("Address Line 1")
                        .build())
                    .telephoneNumber(Telephone.builder().telephoneNumber("01227 831393").build())
                    .build())
                .build())
            .build());
    }

    private ChildParty getParty(List<Element<Child>> updatedChildren, int i) {
        return updatedChildren.get(i).getValue().getParty();
    }
}
