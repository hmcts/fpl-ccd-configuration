package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.BaseCaseNotifyData;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.TabUrlAnchor.PLACEMENT;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.caseData;

@ContextConfiguration(classes = {PlacementApplicationContentProvider.class})
class PlacementApplicationContentProviderTest extends AbstractEmailContentProviderTest {
    @MockBean
    private EmailNotificationHelper helper;
    @Autowired
    private PlacementApplicationContentProvider underTest;

    @Test
    void shouldBuildPlacementNotificationWithExpectedParameters() {
        BaseCaseNotifyData expected = BaseCaseNotifyData.builder()
            .lastName("Smith")
            .caseUrl(caseUrl(CASE_REFERENCE, PLACEMENT))
            .build();

        CaseData caseData = caseData();

        when(helper.getEldestChildLastName(caseData.getAllChildren())).thenReturn("Smith");

        BaseCaseNotifyData actual = underTest.buildPlacementApplicationNotificationParameters(caseData);

        assertThat(actual).isEqualTo(expected);
    }
}
