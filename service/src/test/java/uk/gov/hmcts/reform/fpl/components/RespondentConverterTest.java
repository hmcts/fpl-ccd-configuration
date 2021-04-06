package uk.gov.hmcts.reform.fpl.components;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitorOrganisation;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.Telephone;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.SolicitorRole.SOLICITORA;
import static uk.gov.hmcts.reform.fpl.enums.SolicitorRole.SOLICITORB;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    JacksonAutoConfiguration.class,
    RespondentConverter.class
})
class RespondentConverterTest {
    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private RespondentConverter respondentConverter;

    private static final UUID ELEMENT_ID = UUID.randomUUID();

    @Test
    void shouldConvertRepresentedRespondent() {
        RespondentParty respondentParty = buildRespondentParty();

        Organisation solicitorOrganisation = Organisation.builder()
            .organisationName("Summers Inc")
            .organisationID("12345")
            .build();

        RespondentSolicitor respondentSolicitor = RespondentSolicitor.builder()
            .firstName("Ben")
            .lastName("Summers")
            .email("bensummers@gmail.com")
            .organisation(solicitorOrganisation)
            .build();

        Respondent respondent = Respondent.builder()
            .party(respondentParty)
            .legalRepresentation("Yes")
            .solicitor(respondentSolicitor)
            .build();

        Element<Respondent> respondentElement = element(ELEMENT_ID, respondent);

        RespondentSolicitorOrganisation expectedRespondent = RespondentSolicitorOrganisation.builder()
            .respondentId(ELEMENT_ID)
            .party(respondentParty)
            .solicitor(respondentSolicitor)
            .organisationPolicy(OrganisationPolicy.builder()
                .organisation(solicitorOrganisation)
                .orgPolicyCaseAssignedRole(SOLICITORA.getCaseRoleLabel())
                .build())
            .build();

        RespondentSolicitorOrganisation actualRespondent = respondentConverter.convert(respondentElement, SOLICITORA);

        assertThat(actualRespondent).isEqualTo(expectedRespondent);
    }

    @Test
    void shouldConvertNonRepresentedRespondent() {
        RespondentParty respondentParty = buildRespondentParty();

        Respondent respondent = Respondent.builder()
            .party(respondentParty)
            .legalRepresentation("No")
            .build();

        Element<Respondent> respondentElement = element(ELEMENT_ID, respondent);

        RespondentSolicitorOrganisation expectedRespondent = RespondentSolicitorOrganisation.builder()
            .respondentId(ELEMENT_ID)
            .party(respondentParty)
            .organisationPolicy(OrganisationPolicy.builder()
                .orgPolicyCaseAssignedRole(SOLICITORB.getCaseRoleLabel())
                .build())
            .build();

        RespondentSolicitorOrganisation actualRespondent = respondentConverter.convert(respondentElement, SOLICITORB);

        assertThat(actualRespondent).isEqualTo(expectedRespondent);
    }

    private RespondentParty buildRespondentParty() {
        return RespondentParty.builder()
            .firstName("Joe")
            .lastName("Bloggs")
            .relationshipToChild("Father")
            .dateOfBirth(LocalDate.now())
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
