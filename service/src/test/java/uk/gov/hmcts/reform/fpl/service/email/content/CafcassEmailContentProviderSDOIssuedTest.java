package uk.gov.hmcts.reform.fpl.service.email.content;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseData;

@ContextConfiguration(classes = {
    CafcassEmailContentProviderSDOIssued.class, LookupTestConfig.class, FixedTimeConfiguration.class
})
@TestPropertySource(properties = {"manage-case.ui.base.url=http://fake-url"})
class CafcassEmailContentProviderSDOIssuedTest extends AbstractEmailContentProviderTest {

    @Autowired
    private CafcassEmailContentProviderSDOIssued contentProviderSDOIssued;

    @Test
    void shouldReturnExpectedMapWithValidSDODetails() {
        Map<String, Object> expectedMap = getStandardDirectionTemplateParameters();

        assertThat(contentProviderSDOIssued.buildCafcassStandardDirectionOrderIssuedNotification(populatedCaseData()))
            .isEqualTo(expectedMap);
    }

    private Map<String, Object> getStandardDirectionTemplateParameters() {

        return ImmutableMap.<String, Object>builder()
            .put("title", CAFCASS_NAME)
            .put("familyManCaseNumber", "12345,")
            .put("leadRespondentsName", "Smith")
            .put("hearingDate", "1 January 2020")
            .put("reference", CASE_REFERENCE)
            .put("caseUrl", caseUrl(CASE_REFERENCE))
            .put("documentLink", "http://fake-url/documents/be17a76e-38ed-4448-8b83-45de1aa93f55/binary")
            .put("callout", "^Smith, 12345, hearing 1 Jan 2020")
            .build();
    }
}
