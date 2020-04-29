package uk.gov.hmcts.reform.fpl.service.email.content;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.emptyCaseDetails;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseDetails;

@ContextConfiguration(classes = {HmctsEmailContentProvider.class, LookupTestConfig.class})
class HmctsEmailContentProviderTest extends AbstractEmailContentProviderTest {

    @Autowired
    private HmctsEmailContentProvider hmctsEmailContentProvider;

    @Test
    void shouldReturnExpectedMapWithValidCaseDetails() {
        List<String> ordersAndDirections = List.of("Emergency protection order", "Contact with any named person");

        Map<String, Object> expectedParameters = ImmutableMap.<String, Object>builder()
            .put("court", COURT_NAME)
            .put("localAuthority", LOCAL_AUTHORITY_NAME)
            .put("dataPresent", "Yes")
            .put("fullStop", "No")
            .put("ordersAndDirections", ordersAndDirections)
            .put("timeFramePresent", "Yes")
            .put("timeFrameValue", "same day")
            .put("urgentHearing", "Yes")
            .put("nonUrgentHearing", "No")
            .put("firstRespondentName", "Smith")
            .put("reference", CASE_REFERENCE)
            .put("caseUrl", buildCaseUrl(CASE_REFERENCE))
            .build();

        Map<String, Object> actualParameters = hmctsEmailContentProvider
            .buildHmctsSubmissionNotification(populatedCaseDetails(), LOCAL_AUTHORITY_CODE);

        assertThat(actualParameters).isEqualTo(expectedParameters);
    }

    @Test
    void shouldReturnSuccessfullyWithEmptyCaseDetails() {
        Map<String, Object> expectedParameters = ImmutableMap.<String, Object>builder()
            .put("court", COURT_NAME)
            .put("localAuthority", LOCAL_AUTHORITY_NAME)
            .put("dataPresent", "No")
            .put("fullStop", "Yes")
            .put("ordersAndDirections", "")
            .put("timeFramePresent", "No")
            .put("timeFrameValue", "")
            .put("urgentHearing", "No")
            .put("nonUrgentHearing", "No")
            .put("firstRespondentName", "")
            .put("reference", "123")
            .put("caseUrl", buildCaseUrl("123"))
            .build();

        Map<String, Object> actualParameters = hmctsEmailContentProvider
            .buildHmctsSubmissionNotification(emptyCaseDetails(), LOCAL_AUTHORITY_CODE);

        assertThat(actualParameters).isEqualTo(expectedParameters);
    }
}
