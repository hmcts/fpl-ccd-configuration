package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.notify.hearing.TemporaryHearingJudgeTemplate;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.time.LocalDateTime;
import java.time.format.FormatStyle;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HER_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.MAGISTRATES;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;

@ContextConfiguration(classes = {TemporaryHearingJudgeContentProvider.class})
class TemporaryHearingJudgeContentProviderTest extends AbstractEmailContentProviderTest {
    private static final LocalDateTime NOW = LocalDateTime.now();

    @Autowired
    private TemporaryHearingJudgeContentProvider temporaryHearingJudgeContentProvider;

    @Test
    void shouldBuildTemporaryHearingJudgeTemplateWithExpectedParameters() {
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .respondents1(List.of(ElementUtils.element(Respondent.builder()
                .party(RespondentParty.builder()
                    .firstName("Mark")
                    .lastName("Watson")
                    .build())
                .build())))
            .familyManCaseNumber("123")
            .allocatedJudge(Judge.builder()
                .judgeLastName("Watson")
                .judgeTitle(HIS_HONOUR_JUDGE)
                .build())
            .build();

        HearingBooking hearingBooking = HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .startDate(NOW)
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .judgeTitle(HER_HONOUR_JUDGE)
                .judgeLastName("Davidson")
                .build())
            .build();

        assertThat(temporaryHearingJudgeContentProvider.buildNotificationParameters(caseData, hearingBooking))
            .usingRecursiveComparison().isEqualTo(getExpectedNotificationParameters());
    }

    @Test
    void shouldBuildPartialTemporaryHearingJudgeTemplateWhenMissingOptionalCaseData() {
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

        TemporaryHearingJudgeTemplate partiallyCompleteTemplate =
            temporaryHearingJudgeContentProvider.buildNotificationParameters(caseData, hearingBooking);

        String hearingStartDate = formatLocalDateToString(NOW.toLocalDate(), FormatStyle.MEDIUM);

        assertThat(partiallyCompleteTemplate.getCallout()).isEqualTo(
            String.format("123, hearing %s", hearingStartDate)
        );

        assertThat(partiallyCompleteTemplate.getHasAllocatedJudge()).isEqualTo(NO.getValue());
        assertThat(partiallyCompleteTemplate.getAllocatedJudgeName()).isEmpty();
        assertThat(partiallyCompleteTemplate.getAllocatedJudgeTitle()).isEmpty();
    }

    @Test
    void shouldBuildHearingJudgeTemplateWhenMagistrateSelectedAsHearingJudgeAndNoNameProvided() {
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .familyManCaseNumber("123")
            .build();

        HearingBooking hearingBooking = HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .startDate(NOW)
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .judgeTitle(MAGISTRATES)
                .build())
            .build();

        TemporaryHearingJudgeTemplate template =
            temporaryHearingJudgeContentProvider.buildNotificationParameters(caseData, hearingBooking);

        assertThat(template.getJudgeTitle()).isEqualTo("Justice of the Peace");
        assertThat(template.getJudgeName()).isEqualTo("");
    }

    @Test
    void shouldBuildHearingJudgeTemplateWhenMagistrateSelectedAsHearingJudgeAndNameIsProvided() {
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .familyManCaseNumber("123")
            .build();

        HearingBooking hearingBooking = HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .startDate(NOW)
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .judgeTitle(MAGISTRATES)
                .judgeFullName("Paul Hastings")
                .build())
            .build();

        TemporaryHearingJudgeTemplate template =
            temporaryHearingJudgeContentProvider.buildNotificationParameters(caseData, hearingBooking);

        assertThat(template.getJudgeTitle()).isEqualTo("");
        assertThat(template.getJudgeName()).isEqualTo("Paul Hastings (JP)");
    }

    private TemporaryHearingJudgeTemplate getExpectedNotificationParameters() {
        String hearingStartDate = formatLocalDateToString(NOW.toLocalDate(), FormatStyle.MEDIUM);
        return TemporaryHearingJudgeTemplate.builder()
            .judgeTitle(HER_HONOUR_JUDGE.getLabel())
            .judgeName("Davidson")
            .caseUrl("http://fake-url/cases/case-details/12345")
            .callout(String.format("Watson, 123, hearing %s", hearingStartDate))
            .hearingType(CASE_MANAGEMENT.getLabel())
            .allocatedJudgeTitle(HIS_HONOUR_JUDGE.getLabel())
            .allocatedJudgeName("Watson")
            .hasAllocatedJudge(YES.getValue())
            .build();
    }
}
