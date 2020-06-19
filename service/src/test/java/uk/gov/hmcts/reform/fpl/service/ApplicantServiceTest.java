package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.enums.PartyType;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.rd.model.ContactInformation;
import uk.gov.hmcts.reform.rd.model.Organisation;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ApplicantService.class})
class ApplicantServiceTest {

    private static final Organisation EMPTY_ORGANISATION = Organisation.builder().build();

    @Autowired
    private ApplicantService service;

    @Test
    void shouldExpandApplicantCollectionWhenNoApplicants() {
        CaseData caseData = CaseData.builder().build();

        assertThat(caseData.getApplicants()).isNull();
        assertThat(service.expandApplicantCollection(caseData, EMPTY_ORGANISATION)).hasSize(1);
    }

    @Test
    void shouldNotExpandApplicantCollectionWhenApplicantsAlreadyExists() {
        String applicantPartyId = UUID.randomUUID().toString();

        Organisation organisation = EMPTY_ORGANISATION;

        CaseData caseData = CaseData.builder().applicants(wrapElements(
            Applicant.builder()
                .party(ApplicantParty.builder()
                    .partyId(applicantPartyId)
                    .build())
                .build()))
            .build();

        assertThat(service.expandApplicantCollection(caseData, organisation)).hasSize(1);
        assertThat(unwrapElements(service.expandApplicantCollection(caseData, organisation))
            .get(0).getParty().getPartyId()).isEqualTo(applicantPartyId);
    }

    @Test
    void shouldPassThroughWhenNoApplicants() {
        assertThat(service.addHiddenValues(CaseData.builder().build())).isEmpty();
    }

    @Test
    void shouldAddPartyIdAndPartyTypeValuesToApplicant() {
        List<Element<Applicant>> applicants = wrapElements(Applicant.builder()
            .party(ApplicantParty.builder().build())
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
        List<Element<Applicant>> applicants = wrapElements(Applicant.builder()
                .party(ApplicantParty.builder()
                    .organisationName("Organisation 1")
                    .build())
                .build(),
            Applicant.builder()
                .party(ApplicantParty.builder()
                    .organisationName("Organisation 2")
                    .build())
                .build());

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
        String applicantPartyId = UUID.randomUUID().toString();

        List<Element<Applicant>> applicants = wrapElements(Applicant.builder()
            .party(ApplicantParty.builder()
                .partyId(applicantPartyId)
                .build())
            .build());

        CaseData caseData = CaseData.builder()
            .applicants(applicants)
            .build();

        Applicant applicant = service.addHiddenValues(caseData).get(0).getValue();

        assertThat(applicant.getParty().getPartyId()).isEqualTo(applicantPartyId);
    }

    @Test
    void shouldReturnApplicantCollectionWithOrganisationDetailsWhenOrganisationExists() {
        CaseData caseData = CaseData.builder().build();
        Organisation organisation = buildOrganisation();

        List<Applicant> applicants = unwrapElements(service.expandApplicantCollection(
            caseData, organisation));

        assertThat(applicants.get(0).getParty().getOrganisationName())
            .isEqualTo(organisation.getName());
        assertThat(applicants.get(0).getParty().getAddress()).isEqualTo(organisation
            .getContactInformation().get(0).toAddress());
    }

    @Test
    void shouldReturnApplicantCollectionWithoutOrganisationDetailsWhenNoOrganisationExists() {
        CaseData caseData = CaseData.builder().build();

        List<Applicant> applicants = unwrapElements(service.expandApplicantCollection(
            caseData, EMPTY_ORGANISATION));

        assertThat(applicants.get(0).getParty().getOrganisationName()).isNull();
    }

    private Organisation buildOrganisation() {
        return Organisation.builder()
            .name("Organisation")
            .contactInformation(buildOrganisationAddress())
            .build();
    }

    private List<ContactInformation> buildOrganisationAddress() {
        return List.of(ContactInformation.builder()
            .addressLine1("Flat 12, Pinnacle Apartments")
            .addressLine2("Saffron Central")
            .addressLine3("Square 11")
            .townCity("London")
            .county("County")
            .country("United Kingdom")
            .postCode("CR0 2GE")
            .build());
    }
}
