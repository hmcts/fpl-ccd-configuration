package uk.gov.hmcts.reform.fpl.service.email.content;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseDetails;

@ContextConfiguration(classes = {StandardDirectionOrderIssuedEmailContentProvider.class, LookupTestConfig.class,
    HearingBookingService.class, FixedTimeConfiguration.class
})
class StandardDirectionOrderIssuedEmailContentProviderTest extends AbstractEmailContentProviderTest {

    @Autowired
    private StandardDirectionOrderIssuedEmailContentProvider standardDirectionOrderIssuedEmailContentProvider;

    @Test
    void shouldReturnExpectedMapWithValidSDODetails() {
        Map<String, Object> expectedMap = allocatedJudgeSDOTemplateParameters();

        assertThat(standardDirectionOrderIssuedEmailContentProvider
            .buildNotificationParametersForAllocatedJudge(populatedCaseDetails())).isEqualTo(expectedMap);
    }

    private Map<String, Object> allocatedJudgeSDOTemplateParameters() {
        return ImmutableMap.<String, Object>builder()
            .put("familyManCaseNumber", "12345,")
            .put("leadRespondentsName", "Smith")
            .put("hearingDate", "1 January 2020")
            .put("reference", CASE_REFERENCE)
            .put("caseUrl", caseUrl(CASE_REFERENCE))
            .put("judgeTitle", "Her Honour Judge")
            .put("judgeName", "Byrne")
            .build();
    }
}
