package uk.gov.hmcts.reform.fpl.service.email.content;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import javax.annotation.PostConstruct;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class, PlacementApplicationContentProvider.class})
class PlacementApplicationContentProviderTest extends AbstractEmailContentProviderTest {

    @Autowired
    private PlacementApplicationContentProvider placementApplicationContentProvider;

    @PostConstruct
    void setField() {
        ReflectionTestUtils.setField(placementApplicationContentProvider, "uiBaseUrl", BASE_URL);
    }

    @Test
    void shouldBuildPlacementNotificationWithExpectedParameters() {
        final Map<String, Object> expectedParameters = ImmutableMap.<String, Object>builder()
            .put("respondentLastName", "Smith")
            .put("caseUrl", buildCaseUrl(CASE_REFERENCE))
            .build();

        assertThat(placementApplicationContentProvider.buildPlacementApplicationNotificationParameters(
            callbackRequest().getCaseDetails())).isEqualTo(expectedParameters);
    }
}
