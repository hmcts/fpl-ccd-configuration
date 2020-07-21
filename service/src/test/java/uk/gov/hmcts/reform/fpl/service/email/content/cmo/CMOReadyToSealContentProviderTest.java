package uk.gov.hmcts.reform.fpl.service.email.content.cmo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.model.CaseData;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ContextConfiguration(classes = {CMOReadyToSealContentProvider.class})
class CMOReadyToSealContentProviderTest extends AbstractEmailContentProviderTest {
    @Autowired
    private CMOReadyToSealContentProvider contentProvider;

    @MockBean
    private EmailNotificationHelper notificationHelper;

    private static final LocalDate SOME_DATE = LocalDate.of(2020, 2, 20);
    private static final Long CASE_NUMBER = 12345L;

    @BeforeEach
    void setUp() {
        when(notificationHelper.buildSubjectLine(any(CaseData.class))).thenReturn("Vlad, 123456");
    }

    @Test
    void shouldCreateTemplateWithExpectedParameters() {
        CaseData data = CaseData.builder()
            .familyManCaseNumber("123456")
            .respondents1(wrapElements(
                Respondent.builder()
                    .party(RespondentParty.builder()
                        .lastName("Vlad")
                        .build())
                    .build()))
            .build();

        JudgeAndLegalAdvisor judge = JudgeAndLegalAdvisor.builder()
            .judgeTitle(JudgeOrMagistrateTitle.HER_HONOUR_JUDGE)
            .judgeLastName("Simmons")
            .build();

        HearingBooking hearing = HearingBooking.builder()
            .startDate(LocalDateTime.of(SOME_DATE, LocalTime.of(0, 0)))
            .judgeAndLegalAdvisor(judge)
            .build();

        CMOReadyToSealTemplate template = contentProvider.buildTemplate(hearing, data, CASE_NUMBER, judge);

        CMOReadyToSealTemplate expected = new CMOReadyToSealTemplate()
            .setJudgeName("Simmons")
            .setJudgeTitle("Her Honour Judge")
            .setRespondentLastName("Vlad")
            .setSubjectLineWithHearingDate("Vlad, 123456 hearing 20 February 2020")
            .setCaseUrl(caseUrl(CASE_NUMBER.toString()));

        assertThat(template).isEqualToComparingFieldByField(expected);
    }
}
