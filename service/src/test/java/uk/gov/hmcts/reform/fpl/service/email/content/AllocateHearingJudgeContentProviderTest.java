package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.notify.hearing.AllocateHearingJudgeTemplate;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.time.LocalDateTime;
import java.time.format.FormatStyle;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HER_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;

@ContextConfiguration(classes = {AllocateHearingJudgeContentProvider.class})
class AllocateHearingJudgeContentProviderTest extends AbstractEmailContentProviderTest {
    private static final LocalDateTime NOW = LocalDateTime.now();

    @Autowired
    private AllocateHearingJudgeContentProvider allocateHearingJudgeContentProvider;

    @Test
    void shouldBuildAllocateHearingJudgeTemplateWithExpectedParameters() {
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .respondents1(List.of(ElementUtils.element(Respondent.builder()
                .party(RespondentParty.builder()
                    .firstName("Mark")
                    .lastName("Watson")
                    .build())
                .build())))
            .familyManCaseNumber("123")
            .build();

        HearingBooking hearingBooking = HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .startDate(NOW)
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .judgeTitle(HER_HONOUR_JUDGE)
                .judgeLastName("Davidson")
                .build())
            .build();

        assertThat(allocateHearingJudgeContentProvider.buildNotificationParameters(caseData, hearingBooking))
            .isEqualToComparingFieldByField(getExpectedNotificationParameters());
    }

    @Test
    void shouldBuildAllocateHearingJudgeTemplateCalloutWithPartialParameters() {
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .familyManCaseNumber("123")
            .build();

        HearingBooking hearingBooking = HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .startDate(NOW)
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .judgeTitle(HER_HONOUR_JUDGE)
                .judgeLastName("Davidson")
                .build())
            .build();

        AllocateHearingJudgeTemplate partiallyCompleteTemplate =
            allocateHearingJudgeContentProvider.buildNotificationParameters(caseData, hearingBooking);

        String hearingStartDate = formatLocalDateToString(NOW.toLocalDate(), FormatStyle.MEDIUM);

        assertThat(partiallyCompleteTemplate.getCallout()).isEqualTo(
            String.format("123, hearing %s", hearingStartDate)
        );
    }

    private AllocateHearingJudgeTemplate getExpectedNotificationParameters() {
        AllocateHearingJudgeTemplate allocatedJudgeTemplate = new AllocateHearingJudgeTemplate();
        String hearingStartDate = formatLocalDateToString(NOW.toLocalDate(), FormatStyle.MEDIUM);

        allocatedJudgeTemplate.setJudgeTitle("Her Honour Judge");
        allocatedJudgeTemplate.setJudgeName("Davidson");
        allocatedJudgeTemplate.setCaseUrl("http://fake-url/cases/case-details/12345");
        allocatedJudgeTemplate.setCallout(String.format("Watson, 123, hearing %s", hearingStartDate));
        allocatedJudgeTemplate.setHearingType(CASE_MANAGEMENT.getLabel());

        return allocatedJudgeTemplate;
    }
}
