package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.events.TemporaryHearingJudgeAllocationEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.notify.hearing.TemporaryHearingJudgeTemplate;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.TemporaryHearingJudgeContentProvider;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.TEMP_JUDGE_ALLOCATED_TO_HEARING_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HER_HONOUR_JUDGE;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {TemporaryHearingJudgeEventHandler.class})
class TemporaryHearingJudgeEventHandlerTest {
    private static final String JUDGE_EMAIL = "test@test.com";
    private static final String JUDGE_NAME = "Davidson";

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private TemporaryHearingJudgeContentProvider temporaryHearingJudgeContentProvider;

    @Autowired
    private TemporaryHearingJudgeEventHandler temporaryHearingJudgeEventHandler;

    @Test
    void shouldSendEmailToJudge() {
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .familyManCaseNumber("123")
            .allocatedJudge(Judge.builder()
                .judgeTitle(HER_HONOUR_JUDGE)
                .judgeLastName(JUDGE_NAME)
                .build())
            .build();

        HearingBooking hearingBooking = HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .judgeEmailAddress(JUDGE_EMAIL)
                .judgeTitle(HER_HONOUR_JUDGE)
                .judgeLastName(JUDGE_NAME)
                .build())
            .build();

        TemporaryHearingJudgeTemplate expectedParameters = getExpectedNotificationParameters();

        given(temporaryHearingJudgeContentProvider.buildNotificationParameters(caseData, hearingBooking))
            .willReturn(expectedParameters);

        TemporaryHearingJudgeAllocationEvent allocateHearingJudgeEvent
            = new TemporaryHearingJudgeAllocationEvent(caseData, hearingBooking);

        temporaryHearingJudgeEventHandler.notifyTemporaryHearingJudge(allocateHearingJudgeEvent);

        verify(notificationService).sendEmail(
            TEMP_JUDGE_ALLOCATED_TO_HEARING_TEMPLATE,
            JUDGE_EMAIL,
            expectedParameters,
            caseData.getId().toString());
    }

    private TemporaryHearingJudgeTemplate getExpectedNotificationParameters() {
        return TemporaryHearingJudgeTemplate.builder()
            .judgeTitle(HER_HONOUR_JUDGE.getLabel())
            .judgeName(JUDGE_NAME)
            .caseUrl("http://fake-url/cases/case-details/12345")
            .callout("Davidson, 123, hearing 1st Jan 2020")
            .hearingType(CASE_MANAGEMENT.getLabel())
            .build();
    }
}
