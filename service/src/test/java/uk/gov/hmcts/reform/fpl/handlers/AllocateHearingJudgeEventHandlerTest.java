package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.events.AllocateHearingJudgeEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.notify.hearing.AllocateHearingJudgeTemplate;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.AllocateHearingJudgeContentProvider;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.JUDGE_ALLOCATED_TO_HEARING_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HER_HONOUR_JUDGE;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {AllocateHearingJudgeEventHandler.class})
class AllocateHearingJudgeEventHandlerTest {
    private static final String JUDGE_EMAIL = "test@test.com";

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private AllocateHearingJudgeContentProvider allocateHearingJudgeContentProvider;

    @Autowired
    private AllocateHearingJudgeEventHandler allocateHearingJudgeEventHandler;

    @Test
    void shouldSendEmailToJudge() {
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .familyManCaseNumber("123")
            .build();

        HearingBooking hearingBooking = HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .judgeEmailAddress(JUDGE_EMAIL)
                .judgeTitle(HER_HONOUR_JUDGE)
                .judgeLastName("Davidson")
                .build())
            .build();

        AllocateHearingJudgeTemplate expectedParameters = getExpectedNotificationParameters();

        given(allocateHearingJudgeContentProvider.buildNotificationParameters(caseData, hearingBooking))
            .willReturn(expectedParameters);

        AllocateHearingJudgeEvent allocateHearingJudgeEvent = new AllocateHearingJudgeEvent(caseData, hearingBooking);

        allocateHearingJudgeEventHandler.notifyAllocatedHearingJudge(allocateHearingJudgeEvent);

        verify(notificationService).sendEmail(
            JUDGE_ALLOCATED_TO_HEARING_TEMPLATE,
            JUDGE_EMAIL,
            expectedParameters,
            caseData.getId().toString());
    }

    private AllocateHearingJudgeTemplate getExpectedNotificationParameters() {
        AllocateHearingJudgeTemplate allocatedJudgeTemplate = new AllocateHearingJudgeTemplate();

        allocatedJudgeTemplate.setJudgeTitle("Her Honour Judge");
        allocatedJudgeTemplate.setJudgeName("Davidson");
        allocatedJudgeTemplate.setCaseUrl("http://fake-url/cases/case-details/12345");
        allocatedJudgeTemplate.setCallout("Watson, 123, hearing 1st Jan 2020");
        allocatedJudgeTemplate.setHearingType(CASE_MANAGEMENT.getLabel());

        return allocatedJudgeTemplate;
    }
}
