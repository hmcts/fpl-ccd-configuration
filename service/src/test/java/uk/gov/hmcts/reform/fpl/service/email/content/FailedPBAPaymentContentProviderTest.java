package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.fpl.enums.ApplicationType;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;

import java.util.Map;
import javax.annotation.PostConstruct;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseDetails;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    JacksonAutoConfiguration.class, FailedPBAPaymentContentProvider.class, LookupTestConfig.class
})
class FailedPBAPaymentContentProviderTest extends AbstractEmailContentProviderTest {

    @Autowired
    private FailedPBAPaymentContentProvider failedPBAPaymentContentProvider;

    @PostConstruct
    void setField() {
        ReflectionTestUtils.setField(failedPBAPaymentContentProvider, "uiBaseUrl", BASE_URL);
    }


    @Test
    void shouldReturnExpectedMapWithValidCtscNotificationParameters() {
        Map<String, Object> expectedMap = getExpectedCtscNotificationParameters();

        assertThat(failedPBAPaymentContentProvider.buildCtscNotificationParameters(populatedCaseDetails(),
            ApplicationType.C2_APPLICATION)).isEqualTo(expectedMap);
    }

    @Test
    void shouldReturnExpectedMapWithValidLANotificationParameters() {
        Map<String, Object> expectedMap = Map.of("applicationType", "C110a");

        assertThat(failedPBAPaymentContentProvider.buildLANotificationParameters(
            ApplicationType.C110A_APPLICATION)).isEqualTo(expectedMap);
    }

    private Map<String, Object> getExpectedCtscNotificationParameters() {
        return Map.of("applicationType", "C2", "caseUrl", buildCaseUrl(CASE_REFERENCE));
    }
}
