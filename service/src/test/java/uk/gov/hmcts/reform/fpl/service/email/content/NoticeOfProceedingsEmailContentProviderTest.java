package uk.gov.hmcts.reform.fpl.service.email.content;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseDetails;

@ContextConfiguration(classes = {NoticeOfProceedingsEmailContentProvider.class, EmailNotificationHelper.class,
    HearingBookingService.class, FixedTimeConfiguration.class})
class NoticeOfProceedingsEmailContentProviderTest extends AbstractEmailContentProviderTest {

    @Autowired
    private NoticeOfProceedingsEmailContentProvider noticeOfProceedingsEmailContentProvider;

    @Test
    void shouldBuildNoticeOfProceedingsParametersForAllocatedJudge() {
        Map<String, Object> expectedMap = ImmutableMap.<String, Object>builder()
            .put("caseUrl", caseUrl(CASE_REFERENCE))
            .put("familyManCaseNumber", "12345,")
            .put("hearingDate", "1 January 2020")
            .put("judgeName", "Moley")
            .put("judgeTitle", "Her Honour Judge")
            .put("leadRespondentsName", "Smith")
            .build();

        assertThat(noticeOfProceedingsEmailContentProvider.buildAllocatedJudgeNotification(populatedCaseDetails()))
            .isEqualTo(expectedMap);
    }
}
