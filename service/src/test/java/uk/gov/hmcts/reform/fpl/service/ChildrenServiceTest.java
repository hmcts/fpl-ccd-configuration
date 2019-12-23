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

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
class ChildrenServiceTest {
    private final ChildrenService service = new ChildrenService();

    @Test
    void shouldReturnAnEmptyListOfChildrenWithAPartyIdIfChildrenIsNull() {
        CaseData caseData = CaseData.builder().build();

        List<Element<Child>> alteredChildrenList = service.expandCollection(caseData.getAllChildren());

        assertThat(alteredChildrenList.get(0).getValue().getParty().partyId).isNotNull();
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

        assertThat(childrenList.get(0).getValue().getParty().partyId).isEqualTo("123");
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

        assertThat(updatedChildren.get(0).getValue().getParty().firstName).isEqualTo("James");
        assertThat(updatedChildren.get(0).getValue().getParty().partyType).isEqualTo(PartyType.INDIVIDUAL);
        assertThat(updatedChildren.get(0).getValue().getParty().partyId).isNotNull();
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

        assertThat(updatedChildren.get(0).getValue().getParty().firstName).isEqualTo("James");
        assertThat(updatedChildren.get(0).getValue().getParty().partyType).isEqualTo(PartyType.INDIVIDUAL);
        assertThat(updatedChildren.get(0).getValue().getParty().partyId).isNotNull();

        assertThat(updatedChildren.get(1).getValue().getParty().firstName).isEqualTo("Lucy");
        assertThat(updatedChildren.get(1).getValue().getParty().partyType).isEqualTo(PartyType.INDIVIDUAL);
        assertThat(updatedChildren.get(1).getValue().getParty().partyId).isNotNull();
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

        assertThat(updatedChildren.get(0).getValue().getParty().partyId).isEqualTo("123");
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

        assertThat(updatedChildren.get(0).getValue().getParty().firstName).isEqualTo("James");
        assertThat(updatedChildren.get(0).getValue().getParty().partyId).isEqualTo("123");

        assertThat(updatedChildren.get(1).getValue().getParty().firstName).isEqualTo("Lucy");
        assertThat(updatedChildren.get(1).getValue().getParty().partyId).isNotNull();
    }

    @Test
    void shouldHideChildAddressDetailsWhenConfidentialitySelected() {
        List<Element<Child>> children = childWithAddress("Address Line 1");

        CaseData caseData = CaseData.builder()
            .children1(children)
            .build();

        List<Element<Child>> updatedChildren = service.modifyHiddenValues(caseData.getAllChildren());

        assertThat(updatedChildren.get(0).getValue().getParty().getAddress()).isNull();
    }

    private List<Element<Child>> childWithAddress(String address) {
        return ImmutableList.of(Element.<Child>builder()
                .id(UUID.randomUUID())
                .value(Child.builder()
                    .party(ChildParty.builder()
                        .firstName("James")
                        .detailsHidden("Yes")
                        .address(Address.builder()
                            .addressLine1(address)
                            .build())
                        .build())
                    .build())
                .build());
    }
}
