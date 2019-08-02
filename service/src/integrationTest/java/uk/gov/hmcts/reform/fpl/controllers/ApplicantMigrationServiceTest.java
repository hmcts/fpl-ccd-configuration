package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.OldApplicant;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.ApplicantMigrationService;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {ApplicantMigrationService.class, ObjectMapper.class})
public class ApplicantMigrationServiceTest {

    @Autowired
    private ApplicantMigrationService service;

    @Autowired
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void shouldAddMigratedApplicantYesWhenNoApplicantData() {
        CaseData caseData = CaseData.builder().applicant(null).build();

        String response = service.setMigratedValue(caseData);

        assertThat(response).isEqualTo("Yes");
    }

    @Test
    void shouldAddMigratedApplicantYesWhenNewApplicantExists() {
        CaseData caseData = CaseData.builder()
            .applicants(
                ImmutableList.of(Element.<Applicant>builder()
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

    @SuppressWarnings("unchecked")
    @Test
    void shouldAddPartyIdAndPartyTypeValuesToMigratedApplicant() {
        List<Element<Applicant>> applicants = ImmutableList.of(
            Element.<Applicant>builder()
                .id(UUID.randomUUID())
                .value(Applicant.builder()
                    .party(ApplicantParty.builder()
                        .organisationName("Becky's Organisation")
                        .build())
                    .build())
                .build());

        CaseData caseData = CaseData.builder()
            .applicants(applicants)
            .build();

        List<Element<Applicant>> editedApplicant = service.addHiddenValues(caseData);

        assertThat(editedApplicant.get(0).getValue().getParty().getOrganisationName().contains("Becky's Organisation"));
        assertThat(editedApplicant.get(0).getValue().getParty().getPartyType().equals("ORGANISATION"));
        assertThat(editedApplicant.get(0).getValue().getParty().getPartyId().isEmpty());
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldAddPartyIDAndPartyTypeValuesToMigratedApplicant() {
        List<Element<Applicant>> applicants = ImmutableList.of(
            Element.<Applicant>builder()
                .id(UUID.randomUUID())
                .value(Applicant.builder()
                    .party(ApplicantParty.builder()
                        .organisationName("Becky's Organisation")
                        .build())
                    .build())
                .build(),
            Element.<Applicant>builder()
                .id(UUID.randomUUID())
                .value(Applicant.builder()
                    .party(ApplicantParty.builder()
                        .organisationName("Ben Stokes Bats")
                        .build())
                    .build())
                .build()
        );

        CaseData caseData = CaseData.builder()
            .applicants(applicants)
            .build();

        List<Element<Applicant>> editedApplicant = service.addHiddenValues(caseData);

        assertThat(editedApplicant.get(0).getValue().getParty().getOrganisationName().contains("Becky's Organisation"));
        assertThat(editedApplicant.get(0).getValue().getParty().getPartyType().equals("ORGANISATION"));
        assertThat(editedApplicant.get(0).getValue().getParty().getPartyId().isEmpty());

        assertThat(editedApplicant.get(0).getValue().getParty().getOrganisationName().contains("Ben Stokes Bats"));
        assertThat(editedApplicant.get(0).getValue().getParty().getPartyType().equals("ORGANISATION"));
        assertThat(editedApplicant.get(0).getValue().getParty().getPartyId().isEmpty());
    }
}
