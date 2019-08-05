package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.enums.PartyType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.OldChildren;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
class ChildrenMigrationServiceTest {

    private final ChildrenMigrationService service = new ChildrenMigrationService();

    @Test
    void shouldSetMigratedChildrenToYesWhenNoChildrenDataPresent() {
        CaseData caseData = CaseData.builder().children(null).build();
        String migratedValue = service.setMigratedValue(caseData);

        assertThat(migratedValue).isEqualTo("Yes");
    }

    @Test
    void shouldSetMigratedChildrenToYesWhenChildren1Exists() {
        CaseData caseData = CaseData.builder()
            .children1(
                ImmutableList.of(Element.<Child>builder()
                    .value(
                        Child.builder()
                            .build())
                    .build()))
            .build();

        String migratedValue = service.setMigratedValue(caseData);

        assertThat(migratedValue).isEqualTo("Yes");
    }

    @Test
    void shouldSetMigratedChildrenToNoWhenOldChildrenExists() {
        CaseData caseData = CaseData.builder()
            .children(OldChildren.builder().build())
            .build();

        String migratedValue = service.setMigratedValue(caseData);

        assertThat(migratedValue).isEqualTo("No");
    }

    @Test
    void shouldReturnAnEmptyListOfChildren1WithAPartyIdIfChildren1IsNull() {
        CaseData caseData = CaseData.builder()
            .children(OldChildren.builder().build())
            .build();

        List<Element<Child>> alteredChildrenList = service.expandChildrenCollection(caseData);

        assertThat(alteredChildrenList.get(0).getValue().getParty().partyId).isNotNull();
    }

    @Test
    void shouldReturnChildren1IfChildren1IsPrePopulated() {
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

        List<Element<Child>> migratedChildrenList = service.expandChildrenCollection(caseData);

        assertThat(migratedChildrenList.get(0).getValue().getParty().partyId).isEqualTo("123");
    }

    @SuppressWarnings("unchecked")
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
        List<Element<Child>> updatedChildren1 = service.addHiddenValues(caseData);

        assertThat(updatedChildren1.get(0).getValue().getParty().firstName).isEqualTo("James");
        assertThat(updatedChildren1.get(0).getValue().getParty().partyType).isEqualTo(PartyType.INDIVIDUAL);
        assertThat(updatedChildren1.get(0).getValue().getParty().partyId).isNotNull();
    }

    @SuppressWarnings("unchecked")
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
        List<Element<Child>> updatedChildren1 = service.addHiddenValues(caseData);

        assertThat(updatedChildren1.get(0).getValue().getParty().firstName).isEqualTo("James");
        assertThat(updatedChildren1.get(0).getValue().getParty().partyType).isEqualTo(PartyType.INDIVIDUAL);
        assertThat(updatedChildren1.get(0).getValue().getParty().partyId).isNotNull();

        assertThat(updatedChildren1.get(1).getValue().getParty().firstName).isEqualTo("Lucy");
        assertThat(updatedChildren1.get(1).getValue().getParty().partyType).isEqualTo(PartyType.INDIVIDUAL);
        assertThat(updatedChildren1.get(1).getValue().getParty().partyId).isNotNull();
    }

    @SuppressWarnings("unchecked")
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
        List<Element<Child>> updatedChildren1 = service.addHiddenValues(caseData);

        assertThat(updatedChildren1.get(0).getValue().getParty().partyId).isEqualTo("123");
    }

    @SuppressWarnings("unchecked")
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
        List<Element<Child>> updatedChildren1 = service.addHiddenValues(caseData);

        assertThat(updatedChildren1.get(0).getValue().getParty().firstName).isEqualTo("James");
        assertThat(updatedChildren1.get(0).getValue().getParty().partyId).isEqualTo("123");

        assertThat(updatedChildren1.get(1).getValue().getParty().firstName).isEqualTo("Lucy");
        assertThat(updatedChildren1.get(1).getValue().getParty().partyId).isNotNull();
    }
}
