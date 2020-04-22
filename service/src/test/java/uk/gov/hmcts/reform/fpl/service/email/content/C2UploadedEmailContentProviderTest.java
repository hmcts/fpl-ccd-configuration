package uk.gov.hmcts.reform.fpl.service.email.content;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.service.email.content.AbstractEmailContentProviderTest.BASE_URL;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseDetails;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.formatCaseUrl;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { JacksonAutoConfiguration.class, C2UploadedEmailContentProvider.class })
@TestPropertySource(properties = {"ccd.ui.base.url=" + BASE_URL})
class C2UploadedEmailContentProviderTest extends AbstractEmailContentProviderTest {

    private static final Long CASE_ID = 12345L;

    @Autowired
    private C2UploadedEmailContentProvider c2UploadedEmailContentProvider;

    @Test
    void shouldReturnExpectedMapWithGivenCaseDetails() {
        Map<String, Object> expectedMap = ImmutableMap.<String, Object>builder()
            .put("caseUrl", buildCaseUrl())
            .put("subjectLine","Smith, 12345")
            .put("hearingDetailsCallout","Smith, 12345")
            .put("reference","12345")
            .build();

        assertThat(c2UploadedEmailContentProvider.buildC2UploadNotification(populatedCaseDetails()))
            .isEqualTo(expectedMap);
    }

    @Test
    void shouldReturnExpectedPbaPaymentNotTakenNotification() {
        Map<String, Object> expectedMap = ImmutableMap.<String, Object>builder()
            .put("caseUrl", buildCaseUrl())
            .build();

        assertThat(c2UploadedEmailContentProvider.buildC2UploadPbaPaymentNotTakenNotification(createCase()))
            .isEqualTo(expectedMap);
    }

    String buildCaseUrl() {
        return formatCaseUrl(BASE_URL, C2UploadedEmailContentProviderTest.CASE_ID);
    }

    private static CaseDetails createCase() {
        return CaseDetails.builder()
            .id(CASE_ID)
            .build();
    }
}
