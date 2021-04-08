package uk.gov.hmcts.reform.fpl.components;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.Telephone;
import uk.gov.hmcts.reform.fpl.model.noticeofchange.NoticeOfChangeAnswers;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { NoticeOfChangeAnswersConverter.class })
class NoticeOfChangeAnswersConverterTest {
    @Autowired
    private NoticeOfChangeAnswersConverter noticeOfChangeAnswersConverter;

    @Test
    void shouldConvertRespondentAndApplicantToNoticeOfChangeAnswers() {
        UUID elementId = UUID.randomUUID();
        LocalDate respondentDOB = LocalDate.now().minusDays(5);

        Applicant applicant = Applicant.builder()
            .party(ApplicantParty.builder()
                .organisationName("Test organisation")
                .build())
            .build();

        Organisation solicitorOrganisation = Organisation.builder()
            .organisationName("Summers Inc")
            .organisationID("12345")
            .build();

        Respondent respondent = Respondent.builder()
            .policyReference(1)
            .party(RespondentParty.builder()
                .firstName("Joe")
                .lastName("Bloggs")
                .relationshipToChild("Father")
                .dateOfBirth(respondentDOB)
                .telephoneNumber(Telephone.builder()
                    .contactDirection("By telephone")
                    .telephoneNumber("02838882333")
                    .telephoneUsageType("Personal home number")
                    .build())
                .gender("Male")
                .placeOfBirth("Newry")
                .build())
            .legalRepresentation("Yes")
            .solicitor(RespondentSolicitor.builder()
                .firstName("Ben")
                .lastName("Summers")
                .email("bensummers@gmail.com")
                .organisation(solicitorOrganisation)
                .build())
            .build();

        Element<Respondent> respondentElement = element(elementId, respondent);

        NoticeOfChangeAnswers expectedNoticeOfChangeAnswers = NoticeOfChangeAnswers.builder()
                .respondentFirstName("Joe")
                .respondentLastName("Bloggs")
                .respondentDOB(respondentDOB)
                .applicantName("Test organisation")
                .policyReference(1)
                .build();

        NoticeOfChangeAnswers actualNoticeOfChangeAnswer
            = noticeOfChangeAnswersConverter.convert(respondentElement, applicant);

        assertThat(actualNoticeOfChangeAnswer).isEqualTo(expectedNoticeOfChangeAnswers);
    }
}
