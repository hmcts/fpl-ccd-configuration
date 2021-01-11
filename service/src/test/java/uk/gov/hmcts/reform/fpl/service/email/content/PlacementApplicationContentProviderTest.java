package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.model.notify.BaseCaseNotifyData;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.TabLabel.PLACEMENT;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.caseData;

@ContextConfiguration(classes = {PlacementApplicationContentProvider.class})
class PlacementApplicationContentProviderTest extends AbstractEmailContentProviderTest {

    @Autowired
    private PlacementApplicationContentProvider placementApplicationContentProvider;

    @Test
    void shouldBuildPlacementNotificationWithExpectedParameters() {
        BaseCaseNotifyData expected = BaseCaseNotifyData.builder()
            .respondentLastName("Smith")
            .caseUrl(caseUrl(CASE_REFERENCE, PLACEMENT))
            .build();

        BaseCaseNotifyData actual = placementApplicationContentProvider
            .buildPlacementApplicationNotificationParameters(caseData());

        assertThat(actual).isEqualTo(expected);
    }
}
