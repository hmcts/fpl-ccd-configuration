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
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.rd.model.Organisation;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {ApplicantService.class, ObjectMapper.class})
class ApplicantServiceTest {

    @Autowired
    private ApplicantService service;

    @Test
    void shouldExpandApplicantCollectionWhenNoApplicants() {
        CaseData caseData = CaseData.builder().build();
        Organisation organisation = Organisation.builder().build();

        assertThat(caseData.getApplicants()).isNull();
        assertThat(service.expandApplicantCollection(caseData, organisation)).hasSize(1);
    }

    @Test
    void shouldNotExpandApplicantCollectionWhenApplicantsAlreadyExists() {
        String uuid = UUID.randomUUID().toString();

        Organisation organisation = Organisation.builder().build();

        CaseData caseData = CaseData.builder().applicants(ImmutableList.of(
            Element.<Applicant>builder()
                .value(Applicant.builder()
                    .party(ApplicantParty.builder()
                        .partyId(uuid)
                        .build())
                    .build())
                .build()))
            .build();

        assertThat(service.expandApplicantCollection(caseData, organisation)).hasSize(1);
        assertThat(service.expandApplicantCollection(caseData, organisation)
            .get(0).getValue().getParty().partyId).isEqualTo(uuid);
    }

    @Test
    void shouldAddPartyIdAndPartyTypeValuesToApplicant() {
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

        List<Element<Applicant>> applicant = service.addHiddenValues(caseData);

        assertThat(applicant.get(0).getValue().getParty().getPartyId()).isNotNull();
        assertThat(applicant.get(0).getValue().getParty().getPartyType()).isEqualTo(PartyType.ORGANISATION);
    }

    @Test
    void shouldAddPartyIDAndPartyTypeValuesToManyApplicants() {
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

        Applicant applicant = service.addHiddenValues(caseData).get(0).getValue();

        assertThat(applicant.getParty().partyId).isEqualTo(uuid);
    }

    @Test
    void shouldReturnApplicantCollectionWithOrganisationDetailsWhenOrganisationExists(){
        CaseData caseData = CaseData.builder().build();

        List<Element<Applicant>> applicants = service.expandApplicantCollection(caseData, buildOrganisation());
        assertThat(applicants.get(0).getValue().getParty().getOrganisationName()).isEqualTo(buildOrganisation().getName());
    }

    @Test
    void shouldReturnApplicantCollectionWithoutOrganisationDetailsWhenNoOrganisationExists(){
        CaseData caseData = CaseData.builder().build();

        List<Element<Applicant>> applicants = service.expandApplicantCollection(caseData, Organisation.builder().build());
        assertThat(applicants.get(0).getValue().getParty().getOrganisationName()).isNull();
    }

    private Organisation buildOrganisation(){
        return Organisation.builder().name("Organisation").build();
    }
}
