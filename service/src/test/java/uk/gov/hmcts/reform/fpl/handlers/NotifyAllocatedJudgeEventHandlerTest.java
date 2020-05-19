package uk.gov.hmcts.reform.fpl.handlers;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.events.NotifyAllocatedJudgeEvent;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.AllocatedJudgeContentProvider;

import java.util.Map;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ALLOCATED_JUDGE_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.ALLOCATED_JUDGE_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {NotifyAllocatedJudgeEventHandler.class, JacksonAutoConfiguration.class})
public class NotifyAllocatedJudgeEventHandlerTest {
    @MockBean
    private RequestData requestData;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private AllocatedJudgeContentProvider allocatedJudgeContentProvider;

    @Autowired
    private NotifyAllocatedJudgeEventHandler notifyAllocatedJudgeEventHandler;

    @Test
    void shouldNotifyAllocatedJudgeWhenAssignedToCase() {
        CallbackRequest callbackRequest = callbackRequest();
        CaseDetails caseDetails = callbackRequest().getCaseDetails();

        final Map<String, Object> expectedParameters = getExpectedAllocatedJudgeNotificationParameters();

        given(allocatedJudgeContentProvider.buildAllocatedJudgeNotificationParameters(caseDetails))
            .willReturn(expectedParameters);

        notifyAllocatedJudgeEventHandler.notifyAllocatedJudge(new NotifyAllocatedJudgeEvent(callbackRequest,
            requestData));

        verify(notificationService).sendEmail(
            ALLOCATED_JUDGE_TEMPLATE,
            ALLOCATED_JUDGE_EMAIL_ADDRESS,
            expectedParameters,
            "12345");
    }

    private Map<String, Object> getExpectedAllocatedJudgeNotificationParameters() {
        return ImmutableMap.of(
            "judgeTitle", "Her Honour Judge",
            "judgeName", "McVerry",
            "caseName", "Test Application",
            "caseUrl", "null/case/" + JURISDICTION + "/" + CASE_TYPE + "/12345"
        );
    }
}
