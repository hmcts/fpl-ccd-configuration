package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.ApplicationType;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseDetails;

class FailedPBAPaymentContentProviderTest extends AbstractEmailContentProviderTest {

    private final FailedPBAPaymentContentProvider contentProvider = new FailedPBAPaymentContentProvider(BASE_URL);

    @Test
    void shouldReturnExpectedMapWithValidCtscNotificationParameters() {
        Map<String, Object> expectedParameters = getExpectedCtscNotificationParameters();
        Map<String, Object> actualParameters = contentProvider.buildCtscNotificationParameters(populatedCaseDetails(),
            ApplicationType.C2_APPLICATION);

        assertThat(actualParameters).isEqualTo(expectedParameters);
    }

    @Test
    void shouldReturnExpectedMapWithValidLANotificationParameters() {
        Map<String, Object> expectedParameters = Map.of("applicationType", "C110a");
        Map<String, Object> actualParameters = contentProvider.buildLANotificationParameters(
            ApplicationType.C110A_APPLICATION);

        assertThat(actualParameters).isEqualTo(expectedParameters);
    }

    private Map<String, Object> getExpectedCtscNotificationParameters() {
        return Map.of("applicationType", "C2", "caseUrl", buildCaseUrl(CASE_REFERENCE));
    }
}
