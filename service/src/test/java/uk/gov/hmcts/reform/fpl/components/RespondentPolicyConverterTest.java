package uk.gov.hmcts.reform.fpl.components;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.Telephone;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.SolicitorRole.SOLICITORA;
import static uk.gov.hmcts.reform.fpl.enums.SolicitorRole.SOLICITORB;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { RespondentPolicyConverter.class })
class RespondentPolicyConverterTest {
    @Autowired
    private RespondentPolicyConverter respondentPolicyConverter;

    private static final UUID ELEMENT_ID = UUID.randomUUID();
    private static final LocalDate RESPONDENT_DOB = LocalDate.now().minusDays(5);
    private static final Organisation EMPTY_ORG = Organisation.builder().build();

    @Test
    void shouldReturnRespondentPolicyWithOrganisationSetWhenRespondentHasOrganisation() {
        RespondentParty respondentParty = buildRespondentParty();

        Organisation solicitorOrganisation = Organisation.builder()
            .organisationName("Summers Inc")
            .organisationID("12345")
            .build();

        Respondent respondent = Respondent.builder()
            .party(respondentParty)
            .legalRepresentation("Yes")
            .solicitor(RespondentSolicitor.builder()
                .firstName("Ben")
                .lastName("Summers")
                .email("bensummers@gmail.com")
                .organisation(solicitorOrganisation)
                .build())
            .build();

        Element<Respondent> respondentElement = element(ELEMENT_ID, respondent);

        OrganisationPolicy expectedOrganisationPolicy = OrganisationPolicy.builder()
                .organisation(solicitorOrganisation)
                .orgPolicyCaseAssignedRole(SOLICITORA.getCaseRoleLabel())
                .build();

        OrganisationPolicy actualOrganisationPolicy
            = respondentPolicyConverter.generateForSubmission(SOLICITORA, respondentElement);

        assertThat(actualOrganisationPolicy).isEqualTo(expectedOrganisationPolicy);
    }

    @Test
    void shouldReturnRespondentPolicyWithoutOrganisationSetWhenRespondentHasNoOrganisation() {
        RespondentParty respondentParty = buildRespondentParty();

        Respondent respondent = Respondent.builder()
            .party(respondentParty)
            .legalRepresentation("Yes")
            .solicitor(RespondentSolicitor.builder()
                .firstName("Ben")
                .lastName("Summers")
                .email("bensummers@gmail.com")
                .build())
            .build();

        Element<Respondent> respondentElement = element(ELEMENT_ID, respondent);

        OrganisationPolicy expectedOrganisationPolicy = OrganisationPolicy.builder()
            .organisation(EMPTY_ORG)
            .orgPolicyCaseAssignedRole(SOLICITORA.getCaseRoleLabel())
            .build();

        OrganisationPolicy actualOrganisationPolicy
            = respondentPolicyConverter.generateForSubmission(SOLICITORA, respondentElement);

        assertThat(actualOrganisationPolicy).isEqualTo(expectedOrganisationPolicy);
    }

    @Test
    void shouldReturnOrganisationPolicyWithAssignedRole() {
        OrganisationPolicy expectedOrganisationPolicy = OrganisationPolicy.builder()
            .organisation(EMPTY_ORG)
            .orgPolicyCaseAssignedRole(SOLICITORB.getCaseRoleLabel())
            .build();

        OrganisationPolicy actualOrganisationPolicy = respondentPolicyConverter.generateForSubmission(SOLICITORB);

        assertThat(actualOrganisationPolicy).isEqualTo(expectedOrganisationPolicy);
    }

    private RespondentParty buildRespondentParty() {
        return RespondentParty.builder()
            .firstName("Joe")
            .lastName("Bloggs")
            .relationshipToChild("Father")
            .dateOfBirth(RESPONDENT_DOB)
            .telephoneNumber(Telephone.builder()
                .contactDirection("By telephone")
                .telephoneNumber("02838882333")
                .telephoneUsageType("Personal home number")
                .build())
            .gender("Male")
            .placeOfBirth("Newry")
            .build();
    }
}
