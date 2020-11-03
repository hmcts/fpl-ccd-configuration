package uk.gov.hmcts.reform.fpl.service.email.content;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.caseData;

@ContextConfiguration(classes = {PlacementApplicationContentProvider.class})
class PlacementApplicationContentProviderTest extends AbstractEmailContentProviderTest {

    @Autowired
    private PlacementApplicationContentProvider placementApplicationContentProvider;

    @Test
    void shouldBuildPlacementNotificationWithExpectedParameters() {
        final Map<String, Object> expectedParameters = ImmutableMap.<String, Object>builder()
            .put("respondentLastName", "Smith")
            .put("caseUrl", caseUrl(CASE_REFERENCE, "PlacementTab"))
            .build();

        assertThat(placementApplicationContentProvider.buildPlacementApplicationNotificationParameters(caseData()))
            .isEqualTo(expectedParameters);
    }
}
