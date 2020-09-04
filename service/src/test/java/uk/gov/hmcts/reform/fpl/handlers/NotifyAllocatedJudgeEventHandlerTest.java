package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.events.NotifyAllocatedJudgeEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.allocatedjudge.AllocatedJudgeTemplate;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.AllocatedJudgeContentProvider;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ALLOCATED_JUDGE_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.getExpectedAllocatedJudgeNotificationParameters;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.caseData;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {NotifyAllocatedJudgeEventHandler.class})
class NotifyAllocatedJudgeEventHandlerTest {
    private static final String ALLOCATED_JUDGE_EMAIL_ADDRESS = "judge@gmail.com";

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private AllocatedJudgeContentProvider allocatedJudgeContentProvider;

    @Autowired
    private NotifyAllocatedJudgeEventHandler notifyAllocatedJudgeEventHandler;

    @Test
    void shouldNotifyAllocatedJudgeWhenAssignedToCase() {
        CaseData caseData = caseData();

        final AllocatedJudgeTemplate expectedParameters = getExpectedAllocatedJudgeNotificationParameters();

        given(allocatedJudgeContentProvider.buildNotificationParameters(caseData))
            .willReturn(expectedParameters);

        notifyAllocatedJudgeEventHandler.notifyAllocatedJudge(new NotifyAllocatedJudgeEvent(caseData));

        verify(notificationService).sendEmail(
            ALLOCATED_JUDGE_TEMPLATE,
            ALLOCATED_JUDGE_EMAIL_ADDRESS,
            expectedParameters,
            caseData.getId());
    }
}
