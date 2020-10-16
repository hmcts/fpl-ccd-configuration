package uk.gov.hmcts.reform.fpl.service.email.content;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;
import uk.gov.hmcts.reform.fpl.utils.TestDataHelper;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseData;
import static uk.gov.hmcts.reform.fpl.utils.NotifyAttachedDocumentLinkHelper.generateAttachedDocumentLink;

@ContextConfiguration(classes = {CafcassEmailContentProviderSDOIssued.class, LookupTestConfig.class,
    FixedTimeConfiguration.class})
class CafcassEmailContentProviderSDOIssuedTest extends AbstractEmailContentProviderTest {

    @Autowired
    private CafcassEmailContentProviderSDOIssued contentProviderSDOIssued;

    private static final byte[] APPLICATION_BINARY = TestDataHelper.DOCUMENT_CONTENT;

    @Test
    void shouldReturnExpectedMapWithValidSDODetails() {
        Map<String, Object> expectedMap = getStandardDirectionTemplateParameters();

        when(documentDownloadService.downloadDocument(any()))
            .thenReturn(APPLICATION_BINARY);

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
            .put("documentLink", generateAttachedDocumentLink(APPLICATION_BINARY).get().toMap())
            .put("callout", "^Smith, 12345, hearing 1 Jan 2020")
            .build();
    }
}
