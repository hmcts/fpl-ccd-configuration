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

        CaseData newData = service.addHiddenValues(caseData);

        assertThat(newData.getChildren1().get(0).getValue().getParty().firstName).isEqualTo("James");
        assertThat(newData.getChildren1().get(0).getValue().getParty().partyType).isEqualTo(PartyType.INDIVIDUAL);
        assertThat(newData.getChildren1().get(0).getValue().getParty().partyId).isNotNull();
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

        CaseData newData = service.addHiddenValues(caseData);

        assertThat(newData.getChildren1().get(0).getValue().getParty().firstName).isEqualTo("James");
        assertThat(newData.getChildren1().get(0).getValue().getParty().partyType).isEqualTo(PartyType.INDIVIDUAL);
        assertThat(newData.getChildren1().get(0).getValue().getParty().partyId).isNotNull();

        assertThat(newData.getChildren1().get(1).getValue().getParty().firstName).isEqualTo("Lucy");
        assertThat(newData.getChildren1().get(1).getValue().getParty().partyType).isEqualTo(PartyType.INDIVIDUAL);
        assertThat(newData.getChildren1().get(1).getValue().getParty().partyId).isNotNull();
    }

    @Test
    void shouldNotAddPartyIDAndPartyTypeValuesToDataStructureIfChildren1IsNotPresent() {
        CaseData caseData = CaseData.builder().build();

        CaseData alteredData = service.addHiddenValues(caseData);

        assertThat(alteredData).isEqualTo(caseData);
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

        CaseData alteredData = service.addHiddenValues(caseData);

        assertThat(alteredData.getChildren1().get(0).getValue().getParty().partyId).isEqualTo("123");
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

        CaseData alteredData = service.addHiddenValues(caseData);

        assertThat(alteredData.getChildren1().get(0).getValue().getParty().firstName).isEqualTo("James");
        assertThat(alteredData.getChildren1().get(0).getValue().getParty().partyId).isEqualTo("123");

        assertThat(alteredData.getChildren1().get(1).getValue().getParty().firstName).isEqualTo("Lucy");
        assertThat(alteredData.getChildren1().get(1).getValue().getParty().partyId).isNotNull();
    }
}
