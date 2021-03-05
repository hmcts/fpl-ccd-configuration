package uk.gov.hmcts.reform.fpl.service.email.content.cmo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.exceptions.NoHearingBookingException;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.CMOReadyToSealTemplate;
import uk.gov.hmcts.reform.fpl.service.email.content.AbstractEmailContentProviderTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.TabUrlAnchor.DRAFT_ORDERS;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ContextConfiguration(classes = {AgreedCMOUploadedContentProvider.class})
class AgreedCMOUploadedContentProviderTest extends AbstractEmailContentProviderTest {

    @Autowired
    private AgreedCMOUploadedContentProvider contentProvider;

    private static final LocalDate SOME_DATE = LocalDate.of(2020, 2, 20);
    private static final Long CASE_NUMBER = 12345L;

    @Test
    void shouldCreateTemplateWithExpectedParameters() {
        List<Element<Respondent>> respondents = buildRespondents();
        String familyManCaseNumber = "123456";

        JudgeAndLegalAdvisor judge = JudgeAndLegalAdvisor.builder()
            .judgeTitle(JudgeOrMagistrateTitle.HER_HONOUR_JUDGE)
            .judgeLastName("Simmons")
            .build();

        HearingBooking hearing = buildHearing(judge);

        CMOReadyToSealTemplate template = contentProvider.buildTemplate(hearing, CASE_NUMBER, judge,
            respondents, familyManCaseNumber);

        CMOReadyToSealTemplate expected = CMOReadyToSealTemplate.builder()
            .judgeName("Simmons")
            .judgeTitle("Her Honour Judge")
            .respondentLastName("Vlad")
            .subjectLineWithHearingDate("Vlad, 123456, case management hearing, 20 February 2020")
            .caseUrl(caseUrl(CASE_NUMBER.toString(), DRAFT_ORDERS))
            .build();

        assertThat(template).isEqualTo(expected);
    }

    @Test
    void shouldSetJudgeNameCorrectlyWhenMagistrateJudgeIncludesFullName() {
        List<Element<Respondent>> respondents = buildRespondents();
        String familyManCaseNumber = "123456";

        JudgeAndLegalAdvisor judge = JudgeAndLegalAdvisor.builder()
            .judgeFullName("Mark Simmons")
            .judgeTitle(JudgeOrMagistrateTitle.MAGISTRATES)
            .build();

        HearingBooking hearing = buildHearing(judge);

        CMOReadyToSealTemplate template = contentProvider.buildTemplate(hearing, CASE_NUMBER, judge,
            respondents, familyManCaseNumber);

        CMOReadyToSealTemplate expected = CMOReadyToSealTemplate.builder()
            .judgeName("Mark Simmons (JP)")
            .judgeTitle("")
            .respondentLastName("Vlad")
            .subjectLineWithHearingDate("Vlad, 123456, case management hearing, 20 February 2020")
            .caseUrl(caseUrl(CASE_NUMBER.toString(), DRAFT_ORDERS))
            .build();

        assertThat(template).isEqualTo(expected);
    }

    @Test
    void shouldSetJudgeTitleCorrectlyWhenMagistrateJudgeDoesNotIncludeName() {
        List<Element<Respondent>> respondents = buildRespondents();
        String familyManCaseNumber = "123456";

        JudgeAndLegalAdvisor judge = JudgeAndLegalAdvisor.builder()
            .judgeTitle(JudgeOrMagistrateTitle.MAGISTRATES)
            .build();

        HearingBooking hearing = buildHearing(judge);

        CMOReadyToSealTemplate template = contentProvider.buildTemplate(hearing, CASE_NUMBER, judge,
            respondents, familyManCaseNumber);

        CMOReadyToSealTemplate expected = CMOReadyToSealTemplate.builder()
            .judgeName("")
            .judgeTitle("Justice of the Peace")
            .respondentLastName("Vlad")
            .subjectLineWithHearingDate("Vlad, 123456, case management hearing, 20 February 2020")
            .caseUrl(caseUrl(CASE_NUMBER.toString(), DRAFT_ORDERS))
            .build();

        assertThat(template).isEqualTo(expected);
    }

    @Test
    void shouldThrowNoHearingBookingExceptionWhenNoHearing() {
        List<Element<Respondent>> respondents = buildRespondents();
        String familyManCaseNumber = "123456";

        JudgeAndLegalAdvisor judge = JudgeAndLegalAdvisor.builder()
            .judgeTitle(JudgeOrMagistrateTitle.MAGISTRATES)
            .build();

        assertThatThrownBy(() -> contentProvider.buildTemplate(null, CASE_NUMBER, judge,
            respondents, familyManCaseNumber))
            .isInstanceOf(NoHearingBookingException.class);
    }

    private List<Element<Respondent>> buildRespondents() {
        return wrapElements(
            Respondent.builder()
                .party(RespondentParty.builder()
                    .lastName("Vlad")
                    .build())
                .build());
    }

    private HearingBooking buildHearing(JudgeAndLegalAdvisor judge) {
        return HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .startDate(LocalDateTime.of(SOME_DATE, LocalTime.of(0, 0)))
            .judgeAndLegalAdvisor(judge)
            .build();
    }
}
