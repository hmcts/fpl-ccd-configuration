package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.enums.PartyType;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.OldApplicant;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {ApplicantMigrationService.class, ObjectMapper.class})
class ApplicantMigrationServiceTest {

    @Autowired
    private ApplicantMigrationService service;

    @Test
    void shouldAddMigratedApplicantYesWhenNoApplicantData() {
        String response = service.setMigratedValue(CaseData.builder().build());

        assertThat(response).isEqualTo("Yes");
    }

    @Test
    void shouldAddMigratedApplicantYesWhenNewApplicantExists() {
        CaseData caseData = CaseData.builder()
            .applicants(ImmutableList.of(Element.<Applicant>builder()
                .value(Applicant.builder()
                    .party(ApplicantParty.builder().build())
                    .build())
                .build()))
            .build();

        String response = service.setMigratedValue(caseData);

        assertThat(response).isEqualTo("Yes");
    }

    @Test
    void shouldAddMigratedApplicantNoWhenOldApplicantExists() {
        CaseData caseData = CaseData.builder()
            .applicant(OldApplicant.builder().build())
            .build();

        String response = service.setMigratedValue(caseData);

        assertThat(response).isEqualTo("No");
    }

    @Test
    void shouldExpandApplicantCollectionWhenNoApplicants() {
        CaseData caseData = CaseData.builder().build();

        assertThat(caseData.getApplicants()).isNull();
        assertThat(service.expandApplicantCollection(caseData)).hasSize(1);
    }

    @Test
    void shouldNotExpandApplicantCollectionWhenApplicantsAlreadyHasSize1() {
        CaseData caseData = CaseData.builder().applicants(ImmutableList.of(
            Element.<Applicant>builder().build()))
            .build();

        assertThat(caseData.getApplicants()).hasSize(1);
        assertThat(service.expandApplicantCollection(caseData)).hasSize(1);
    }


    @Test
    void shouldAddPartyIdAndPartyTypeValuesToMigratedApplicant() {
        List<Element<Applicant>> applicants = ImmutableList.of(
            Element.<Applicant>builder()
                .id(UUID.randomUUID())
                .value(Applicant.builder()
                    .party(ApplicantParty.builder().build())
                    .build())
                .build());

        CaseData caseData = CaseData.builder()
            .applicants(applicants)
            .build();

        List<Element<Applicant>> migratedApplicant = service.addHiddenValues(caseData);

        assertThat(migratedApplicant.get(0).getValue().getParty().getPartyId()).isNotNull();
        assertThat(migratedApplicant.get(0).getValue().getParty().getPartyType()).isEqualTo(PartyType.ORGANISATION);
    }

    @Test
    void shouldAddPartyIDAndPartyTypeValuesToManyMigratedApplicants() {
        List<Element<Applicant>> applicants = ImmutableList.of(
            Element.<Applicant>builder()
                .id(UUID.randomUUID())
                .value(Applicant.builder()
                    .party(ApplicantParty.builder()
                        .organisationName("Organisation 1")
                        .build())
                    .build())
                .build(),
            Element.<Applicant>builder()
                .id(UUID.randomUUID())
                .value(Applicant.builder()
                    .party(ApplicantParty.builder()
                        .organisationName("Organisation 2")
                        .build())
                    .build())
                .build()
        );

        CaseData caseData = CaseData.builder()
            .applicants(applicants)
            .build();

        Applicant firstApplicant = service.addHiddenValues(caseData).get(0).getValue();
        Applicant secondApplicant = service.addHiddenValues(caseData).get(1).getValue();

        assertThat(firstApplicant.getParty().getPartyId()).isNotNull();
        assertThat(firstApplicant.getParty().getPartyType()).isEqualTo(PartyType.ORGANISATION);
        assertThat(firstApplicant.getParty().getOrganisationName()).isEqualTo("Organisation 1");

        assertThat(secondApplicant.getParty().getPartyId()).isNotNull();
        assertThat(secondApplicant.getParty().getPartyType()).isEqualTo(PartyType.ORGANISATION);
        assertThat(secondApplicant.getParty().getOrganisationName()).isEqualTo("Organisation 2");
    }

    @Test
    void shouldNotAddNewPartyIdWhenApplicantsAlreadyHasPartyIdValue() {
        String uuid = UUID.randomUUID().toString();

        List<Element<Applicant>> applicants = ImmutableList.of(
            Element.<Applicant>builder()
                .id(UUID.randomUUID())
                .value(Applicant.builder()
                    .party(ApplicantParty.builder()
                        .partyId(uuid)
                        .build())
                    .build())
                .build());

        CaseData caseData = CaseData.builder()
            .applicants(applicants)
            .build();

        Applicant migratedApplicant = service.addHiddenValues(caseData).get(0).getValue();

        assertThat(migratedApplicant.getParty().partyId).isEqualTo(uuid);
    }


}
