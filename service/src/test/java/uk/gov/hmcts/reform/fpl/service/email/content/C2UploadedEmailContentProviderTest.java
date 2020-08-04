package uk.gov.hmcts.reform.fpl.service.email.content;

import com.google.common.collect.ImmutableMap;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.notify.allocatedjudge.UploadC2Template;
import uk.gov.hmcts.reform.fpl.model.notify.allocatedjudge.AllocatedJudgeTemplateForC2;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;
import uk.gov.hmcts.reform.fpl.utils.TestDataHelper;

import java.util.Map;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseDetails;
import static uk.gov.hmcts.reform.fpl.utils.NotifyAttachedDocumentLinkHelper.generateAttachedDocumentLink;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@ContextConfiguration(classes = {C2UploadedEmailContentProvider.class, EmailNotificationHelper.class,
    FixedTimeConfiguration.class})
class C2UploadedEmailContentProviderTest extends AbstractEmailContentProviderTest {

    @Autowired
    private C2UploadedEmailContentProvider c2UploadedEmailContentProvider;

    private static final byte[] APPLICATION_BINARY = TestDataHelper.DOCUMENT_CONTENT;

    private static DocumentReference applicationDocument;

    @BeforeEach
    void init() {
        applicationDocument = testDocumentReference();
        when(documentDownloadService.downloadDocument(applicationDocument.getBinaryUrl()))
            .thenReturn(APPLICATION_BINARY);
    }

    @Test
    void shouldReturnExpectedMapWithGivenCaseDetails() {
        CaseDetails caseDetails = populatedCaseDetails(
            Map.of("applicationBinaryUrl", applicationDocument.getBinaryUrl()));

        UploadC2Template adminTemplateForC2 = getAdminParametersForC2();

        assertThat(c2UploadedEmailContentProvider.buildC2UploadNotification(caseDetails))
            .isEqualToComparingFieldByField(adminTemplateForC2);
    }

    @Test
    void shouldReturnExpectedPbaPaymentNotTakenNotification() {
        Map<String, Object> expectedMap = ImmutableMap.<String, Object>builder()
            .put("caseUrl", caseUrl(CASE_REFERENCE))
            .build();

        assertThat(c2UploadedEmailContentProvider.buildC2UploadPbaPaymentNotTakenNotification(createCase()))
            .isEqualTo(expectedMap);
    }

    @Test
    void shouldReturnExpectedMapWithAllocatedJudgeDetails() {
        AllocatedJudgeTemplateForC2 expectedMap = getAllocatedJudgeParametersForC2();

        assertThat(c2UploadedEmailContentProvider.buildC2UploadNotificationForAllocatedJudge(populatedCaseDetails()))
            .isEqualToComparingFieldByField(expectedMap);
    }

    private AllocatedJudgeTemplateForC2 getAllocatedJudgeParametersForC2() {
        AllocatedJudgeTemplateForC2 allocatedJudgeTemplateForC2 = new AllocatedJudgeTemplateForC2();

        allocatedJudgeTemplateForC2.setCaseUrl("http://fake-url/cases/case-details/12345");
        allocatedJudgeTemplateForC2.setCallout(format("Smith, %s", CASE_REFERENCE));
        allocatedJudgeTemplateForC2.setJudgeTitle("Her Honour Judge");
        allocatedJudgeTemplateForC2.setJudgeName("Moley");
        allocatedJudgeTemplateForC2.setRespondentLastName("Smith");

        return allocatedJudgeTemplateForC2;
    }

    private static CaseDetails createCase() {
        return CaseDetails.builder()
            .id(Long.valueOf(CASE_REFERENCE))
            .build();
    }

    private UploadC2Template getAdminParametersForC2() {
        UploadC2Template adminTemplateForC2 = new UploadC2Template();

        adminTemplateForC2.setCallout(format("Smith, %s", CASE_REFERENCE));
        adminTemplateForC2.setRespondentLastName("Smith");
        adminTemplateForC2.setCaseUrl("http://fake-url/cases/case-details/12345");
        adminTemplateForC2.setDocumentLink(generateAttachedDocumentLink(APPLICATION_BINARY)
            .map(JSONObject::toMap)
            .orElse(null));

        return adminTemplateForC2;
    }
}
