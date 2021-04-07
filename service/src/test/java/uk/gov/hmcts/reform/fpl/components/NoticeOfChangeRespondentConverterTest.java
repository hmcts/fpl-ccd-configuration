package uk.gov.hmcts.reform.fpl.components;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.Telephone;
import uk.gov.hmcts.reform.fpl.model.noticeofchange.NoticeOfChangeAnswers;
import uk.gov.hmcts.reform.fpl.model.noticeofchange.NoticeOfChangeRespondent;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.SolicitorRole.SOLICITORA;
import static uk.gov.hmcts.reform.fpl.enums.SolicitorRole.SOLICITORB;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { NoticeOfChangeRespondentConverter.class })
class NoticeOfChangeRespondentConverterTest {
    @Autowired
    private NoticeOfChangeRespondentConverter noticeOfChangeRespondentConverter;

    private static final UUID ELEMENT_ID = UUID.randomUUID();
    private static final LocalDate RESPONDENT_DOB = LocalDate.now().minusDays(5);

    @Test
    void shouldConvertRepresentedRespondentAndApplicant() {
        RespondentParty respondentParty = buildRespondentParty();
        Applicant applicant = buildApplicant();

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

        NoticeOfChangeRespondent expectedRespondent = NoticeOfChangeRespondent.builder()
            .respondentId(ELEMENT_ID)
            .noticeOfChangeAnswers(NoticeOfChangeAnswers.builder()
                .respondentFirstName("Joe")
                .respondentLastName("Bloggs")
                .respondentDOB(RESPONDENT_DOB)
                .applicantName("Test organisation")
                .build())
            .organisationPolicy(OrganisationPolicy.builder()
                .organisation(solicitorOrganisation)
                .orgPolicyCaseAssignedRole(SOLICITORA.getCaseRoleLabel())
                .build())
            .build();

        NoticeOfChangeRespondent actualRespondent
            = noticeOfChangeRespondentConverter.convert(respondentElement, applicant, SOLICITORA);

        assertThat(actualRespondent).isEqualTo(expectedRespondent);
    }

    @Test
    void shouldConvertNonRepresentedRespondentAndApplicant() {
        RespondentParty respondentParty = buildRespondentParty();
        Applicant applicant = buildApplicant();

        Respondent respondent = Respondent.builder()
            .party(respondentParty)
            .legalRepresentation("No")
            .build();

        Element<Respondent> respondentElement = element(ELEMENT_ID, respondent);

        NoticeOfChangeRespondent expectedRespondent = NoticeOfChangeRespondent.builder()
            .respondentId(ELEMENT_ID)
            .noticeOfChangeAnswers(NoticeOfChangeAnswers.builder()
                .respondentFirstName("Joe")
                .respondentLastName("Bloggs")
                .respondentDOB(RESPONDENT_DOB)
                .applicantName("Test organisation")
                .build())
            .organisationPolicy(OrganisationPolicy.builder()
                .orgPolicyCaseAssignedRole(SOLICITORB.getCaseRoleLabel())
                .build())
            .build();

        NoticeOfChangeRespondent actualRespondent
            = noticeOfChangeRespondentConverter.convert(respondentElement, applicant, SOLICITORB);

        assertThat(actualRespondent).isEqualTo(expectedRespondent);
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

    private Applicant buildApplicant() {
        return Applicant.builder()
            .party(ApplicantParty.builder()
                .organisationName("Test organisation")
                .build())
            .build();
    }
}
