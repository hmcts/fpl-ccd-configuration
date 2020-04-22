package uk.gov.hmcts.reform.fpl.service.email.content;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Map;
import javax.annotation.PostConstruct;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseDetails;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.formatCaseUrl;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { JacksonAutoConfiguration.class, C2UploadedEmailContentProvider.class })
class C2UploadedEmailContentProviderTest extends AbstractEmailContentProviderTest {

    private static final Long CASE_ID = 12345L;

    @Autowired
    private C2UploadedEmailContentProvider c2UploadedEmailContentProvider;

    @PostConstruct
    void setField() {
        ReflectionTestUtils.setField(c2UploadedEmailContentProvider, "uiBaseUrl", BASE_URL);
    }

    @Test
    void shouldReturnExpectedMapWithGivenCaseDetails() {
        Map<String, Object> expectedMap = ImmutableMap.<String, Object>builder()
            .put("caseUrl", buildCaseUrl("12345"))
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
            .put("caseUrl", buildCaseUrl("12345"))
            .build();

        assertThat(c2UploadedEmailContentProvider.buildC2UploadPbaPaymentNotTakenNotification(createCase()))
            .isEqualTo(expectedMap);
    }

    String buildCaseUrl(String caseId) {
        return formatCaseUrl(BASE_URL, Long.parseLong(caseId));
    }

    private CaseDetails createCase() {
        return CaseDetails.builder()
            .id(CASE_ID)
            .build();
    }
}
