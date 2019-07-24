package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.enums.PartyType;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.Respondents;
import uk.gov.hmcts.reform.fpl.model.common.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.migration.MigratedRespondent;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {RespondentService.class, ObjectMapper.class})
class RespondentServiceTest {

    @Autowired
    private RespondentService service;

    @Test
    void shouldAddMigratedRespondentYesWhenNoRespondentData() {
        CaseData caseData = CaseData.builder().respondents(null).build();

        String migratedValue = service.setMigratedValue(caseData);

        assertThat(migratedValue).isEqualTo("Yes");
    }

    @Test
    void shouldAddMigratedRespondentYesWhenRespondents1Exists() {
        CaseData caseData = CaseData.builder()
            .respondents1(
                ImmutableList.of(Element.<MigratedRespondent>builder()
                    .value(
                        MigratedRespondent.builder()
                            .build())
                    .build()))
            .build();

        String migratedValue = service.setMigratedValue(caseData);

        assertThat(migratedValue).isEqualTo("Yes");
    }

    @Test
    void shouldAddMigratedRespondentNoWhenOldRespondentsExists() {
        CaseData caseData = CaseData.builder()
            .respondents(Respondents.builder().build())
            .build();

        String migratedValue = service.setMigratedValue(caseData);

        assertThat(migratedValue).isEqualTo("No");
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldAddPartyIdAndPartyTypeValuesToSingleRespondent() {
        List<Element<MigratedRespondent>> respondents = ImmutableList.of(
            Element.<MigratedRespondent>builder()
                .id(UUID.randomUUID())
                .value(MigratedRespondent.builder()
                    .party(RespondentParty.builder()
                        .firstName("James")
                        .build())
                    .build())
                .build());

        CaseData caseData = CaseData.builder()
            .respondents1(respondents)
            .build();

        CaseData newData = service.addHiddenValues(caseData);

        assertThat(newData.getRespondents1().get(0).getValue().getParty().firstName).isEqualTo("James");
        assertThat(newData.getRespondents1().get(0).getValue().getParty().partyType).isEqualTo(PartyType.INDIVIDUAL);
        assertThat(newData.getRespondents1().get(0).getValue().getParty().partyId).isNotNull();
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldAddPartyIdAndPartyTypeValuesToMultipleRespondents() {
        List<Element<MigratedRespondent>> respondents = ImmutableList.of(
            Element.<MigratedRespondent>builder()
                .id(UUID.randomUUID())
                .value(MigratedRespondent.builder()
                    .party(RespondentParty.builder()
                        .firstName("James")
                        .build())
                    .build())
                .build(),
            Element.<MigratedRespondent>builder()
                .id(UUID.randomUUID())
                .value(MigratedRespondent.builder()
                    .party(RespondentParty.builder()
                        .firstName("Lucy")
                        .build())
                    .build())
                .build()
        );

        CaseData caseData = CaseData.builder()
            .respondents1(respondents)
            .build();

        CaseData newData = service.addHiddenValues(caseData);

        assertThat(newData.getRespondents1().get(0).getValue().getParty().firstName).isEqualTo("James");
        assertThat(newData.getRespondents1().get(0).getValue().getParty().partyType).isEqualTo(PartyType.INDIVIDUAL);
        assertThat(newData.getRespondents1().get(0).getValue().getParty().partyId).isNotNull();

        assertThat(newData.getRespondents1().get(1).getValue().getParty().firstName).isEqualTo("Lucy");
        assertThat(newData.getRespondents1().get(1).getValue().getParty().partyType).isEqualTo(PartyType.INDIVIDUAL);
        assertThat(newData.getRespondents1().get(1).getValue().getParty().partyId).isNotNull();
    }

    @Test
    void shouldNotAddPartyIdAndPartyTypeValuesToDataStructureIfRespondents1IsNotPresent() {
        CaseData caseData = CaseData.builder().build();

        CaseData alteredData = service.addHiddenValues(caseData);

        assertThat(alteredData).isEqualTo(caseData);
    }

    @Test
    void shouldKeepExistingPartyIdWhenAlreadyExists() {
        List<Element<MigratedRespondent>> respondents = ImmutableList.of(
            Element.<MigratedRespondent>builder()
                .id(UUID.randomUUID())
                .value(MigratedRespondent.builder()
                    .party(RespondentParty.builder()
                        .firstName("James")
                        .partyId("123")
                        .build())
                    .build())
                .build());

        CaseData caseData = CaseData.builder()
            .respondents1(respondents)
            .build();

        CaseData alteredData = service.addHiddenValues(caseData);

        assertThat(alteredData.getRespondents1().get(0).getValue().getParty().partyId).isEqualTo("123");
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldKeepExistingPartyIdAndContinueAddingNewPartyId() {
        List<Element<MigratedRespondent>> respondents = ImmutableList.of(
            Element.<MigratedRespondent>builder()
                .id(UUID.randomUUID())
                .value(MigratedRespondent.builder()
                    .party(RespondentParty.builder()
                        .firstName("James")
                        .partyId("123")
                        .build())
                    .build())
                .build(),
            Element.<MigratedRespondent>builder()
                .id(UUID.randomUUID())
                .value(MigratedRespondent.builder()
                    .party(RespondentParty.builder()
                        .firstName("Lucy")
                        .build())
                    .build())
                .build());

        CaseData caseData = CaseData.builder()
            .respondents1(respondents)
            .build();

        CaseData alteredData = service.addHiddenValues(caseData);

        assertThat(alteredData.getRespondents1().get(0).getValue().getParty().firstName).isEqualTo("James");
        assertThat(alteredData.getRespondents1().get(0).getValue().getParty().partyId).isEqualTo("123");

        assertThat(alteredData.getRespondents1().get(1).getValue().getParty().firstName).isEqualTo("Lucy");
        assertThat(alteredData.getRespondents1().get(1).getValue().getParty().partyId).isNotNull();
    }
}
