package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.getExpectedAllocatedJudgeNotificationParameters;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;

@ContextConfiguration(classes = {AllocatedJudgeContentProvider.class})
class AllocatedJudgeContentProviderTest extends AbstractEmailContentProviderTest {

    @Autowired
    private AllocatedJudgeContentProvider allocatedJudgeContentProvider;

    @Test
    void shouldBuildAllocatedJudgeNotificationWithExpectedParameters() {
        final Map<String, Object> expectedParameters = getExpectedAllocatedJudgeNotificationParameters();

        assertThat(allocatedJudgeContentProvider.buildAllocatedJudgeNotificationParameters(
            callbackRequest().getCaseDetails())).isEqualTo(expectedParameters);
    }
}
