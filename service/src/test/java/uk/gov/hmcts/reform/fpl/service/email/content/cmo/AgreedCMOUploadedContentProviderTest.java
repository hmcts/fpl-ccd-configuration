package uk.gov.hmcts.reform.fpl.service.email.content.cmo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.exceptions.NoHearingBookingException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.CMOReadyToSealTemplate;
import uk.gov.hmcts.reform.fpl.service.email.content.AbstractEmailContentProviderTest;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HER_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.MAGISTRATES;
import static uk.gov.hmcts.reform.fpl.enums.TabUrlAnchor.DRAFT_ORDERS;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ContextConfiguration(classes = {AgreedCMOUploadedContentProvider.class})
class AgreedCMOUploadedContentProviderTest extends AbstractEmailContentProviderTest {

    private static final LocalDate SOME_DATE = LocalDate.of(2020, 2, 20);
    private static final Long CASE_NUMBER = 12345L;
    private static final String FAMILY_MAN_CASE_NUMBER = "123456";

    private static CaseData caseData = CaseData.builder()
        .id(CASE_NUMBER)
        .familyManCaseNumber(FAMILY_MAN_CASE_NUMBER)
        .children1(wrapElements(mock(Child.class)))
        .respondents1(wrapElements(Respondent.builder()
            .party(RespondentParty.builder().lastName("Vlad").build())
            .build()
        ))
        .build();

    @MockBean
    private EmailNotificationHelper helper;

    @Autowired
    private AgreedCMOUploadedContentProvider contentProvider;

    @BeforeEach
    void setUp() {
        when(helper.getEldestChildLastName(caseData.getAllChildren())).thenReturn("Vlad");
    }

    @Test
    void shouldCreateTemplateWithExpectedParameters() {
        JudgeAndLegalAdvisor judge = JudgeAndLegalAdvisor.builder()
            .judgeTitle(HER_HONOUR_JUDGE)
            .judgeLastName("Simmons")
            .build();

        HearingBooking hearing = buildHearing(judge);

        CMOReadyToSealTemplate template = contentProvider.buildTemplate(hearing, judge, caseData);

        CMOReadyToSealTemplate expected = CMOReadyToSealTemplate.builder()
            .judgeName("Simmons")
            .judgeTitle("Her Honour Judge")
            .lastName("Vlad")
            .subjectLineWithHearingDate("Vlad, 123456, case management hearing, 20 February 2020")
            .caseUrl(caseUrl(CASE_NUMBER.toString(), DRAFT_ORDERS))
            .build();

        assertThat(template).isEqualTo(expected);
    }

    @Test
    void shouldSetJudgeNameCorrectlyWhenMagistrateJudgeIncludesFullName() {
        JudgeAndLegalAdvisor judge = JudgeAndLegalAdvisor.builder()
            .judgeFullName("Mark Simmons")
            .judgeTitle(MAGISTRATES)
            .build();

        HearingBooking hearing = buildHearing(judge);

        CMOReadyToSealTemplate template = contentProvider.buildTemplate(hearing, judge, caseData);

        CMOReadyToSealTemplate expected = CMOReadyToSealTemplate.builder()
            .judgeName("Mark Simmons (JP)")
            .judgeTitle("")
            .lastName("Vlad")
            .subjectLineWithHearingDate("Vlad, 123456, case management hearing, 20 February 2020")
            .caseUrl(caseUrl(CASE_NUMBER.toString(), DRAFT_ORDERS))
            .build();

        assertThat(template).isEqualTo(expected);
    }

    @Test
    void shouldSetJudgeTitleCorrectlyWhenMagistrateJudgeDoesNotIncludeName() {
        JudgeAndLegalAdvisor judge = JudgeAndLegalAdvisor.builder()
            .judgeTitle(MAGISTRATES)
            .build();

        HearingBooking hearing = buildHearing(judge);

        CMOReadyToSealTemplate template = contentProvider.buildTemplate(hearing, judge, caseData);

        CMOReadyToSealTemplate expected = CMOReadyToSealTemplate.builder()
            .judgeName("")
            .judgeTitle("Justice of the Peace")
            .lastName("Vlad")
            .subjectLineWithHearingDate("Vlad, 123456, case management hearing, 20 February 2020")
            .caseUrl(caseUrl(CASE_NUMBER.toString(), DRAFT_ORDERS))
            .build();

        assertThat(template).isEqualTo(expected);
    }

    @Test
    void shouldThrowNoHearingBookingExceptionWhenNoHearing() {
        JudgeAndLegalAdvisor judge = JudgeAndLegalAdvisor.builder()
            .judgeTitle(MAGISTRATES)
            .build();

        assertThatThrownBy(() -> contentProvider.buildTemplate(null, judge, caseData))
            .isInstanceOf(NoHearingBookingException.class);
    }

    private HearingBooking buildHearing(JudgeAndLegalAdvisor judge) {
        return HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .startDate(LocalDateTime.of(SOME_DATE, LocalTime.of(0, 0)))
            .judgeAndLegalAdvisor(judge)
            .build();
    }
}
