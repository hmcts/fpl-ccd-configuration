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

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
class ChildrenServiceTest {
    private final ChildrenService service = new ChildrenService();

    @Test
    void shouldReturnAnEmptyListOfChildrenWithAPartyIdIfChildrenIsNull() {
        CaseData caseData = CaseData.builder().build();

        List<Element<Child>> alteredChildrenList = service.expandCollection(caseData.getAllChildren());

        assertThat(getParty(alteredChildrenList, 0).partyId).isNotNull();
    }

    @Test
    void shouldReturnChildrenIfChildrenIsPrePopulated() {
        CaseData caseData = CaseData.builder()
            .children1(
                ImmutableList.of(Element.<Child>builder()
                    .value(
                        Child.builder()
                            .party(ChildParty.builder()
                                .partyId("123")
                                .build())
                            .build())
                    .build()))
            .build();

        List<Element<Child>> childrenList = service.expandCollection(caseData.getAllChildren());

        assertThat(getParty(childrenList, 0).partyId).isEqualTo("123");
    }

    @Test
    void shouldAddPartyIDAndPartyTypeValuesToSingleChild() {
        List<Element<Child>> children = ImmutableList.of(
            Element.<Child>builder()
                .id(UUID.randomUUID())
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
                .id(UUID.randomUUID())
                .value(Child.builder()
                    .party(ChildParty.builder()
                        .firstName("James")
                        .build())
                    .build())
                .build(),
            Element.<Child>builder()
                .id(UUID.randomUUID())
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

    @Valid
    @NotNull(message = "You need to add details to children")
    private ChildParty getParty(List<Element<Child>> updatedChildren, int i) {
        return updatedChildren.get(i).getValue().getParty();
    }

    @Test
    void shouldKeepExistingPartyID() {
        List<Element<Child>> children = ImmutableList.of(
            Element.<Child>builder()
                .id(UUID.randomUUID())
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
                .id(UUID.randomUUID())
                .value(Child.builder()
                    .party(ChildParty.builder()
                        .firstName("James")
                        .partyId("123")
                        .build())
                    .build())
                .build(),
            Element.<Child>builder()
                .id(UUID.randomUUID())
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

    private List<Element<Child>> childElementWithDetailsHiddenValue(String hidden) {
        return ImmutableList.of(Element.<Child>builder()
                .id(UUID.randomUUID())
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
}
