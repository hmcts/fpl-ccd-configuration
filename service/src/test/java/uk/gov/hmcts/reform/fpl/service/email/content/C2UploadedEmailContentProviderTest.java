package uk.gov.hmcts.reform.fpl.service.email.content;

import com.google.common.collect.ImmutableMap;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.notify.allocatedjudge.AllocatedJudgeTemplateForC2;
import uk.gov.hmcts.reform.fpl.model.notify.c2uploaded.C2UploadedTemplate;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

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

    private static final byte[] C2_DOCUMENT_BINARY = {5};
    private static DocumentReference applicationDocument;
    private DocumentReference uploadedC2 = testDocumentReference();

    @BeforeEach
    void init() {
        applicationDocument = testDocumentReference();
        when(documentDownloadService.downloadDocument(uploadedC2.getBinaryUrl()))
            .thenReturn(C2_DOCUMENT_BINARY);
    }

    @Test
    void shouldReturnExpectedMapWithGivenCaseDetails() {
        CaseDetails caseDetails = populatedCaseDetails(
            Map.of("applicationBinaryUrl", applicationDocument.getBinaryUrl()));

        C2UploadedTemplate c2UploadedTemplateParameters = getC2UploadedTemplateParameters();

        assertThat(c2UploadedEmailContentProvider.buildC2UploadNotificationTemplate(caseDetails, uploadedC2))
            .isEqualToComparingFieldByField(c2UploadedTemplateParameters);
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

    private C2UploadedTemplate getC2UploadedTemplateParameters() {
        C2UploadedTemplate c2UploadedTemplate = new C2UploadedTemplate();

        c2UploadedTemplate.setCallout(format("Smith, %s", CASE_REFERENCE));
        c2UploadedTemplate.setRespondentLastName("Smith");
        c2UploadedTemplate.setCaseUrl("http://fake-url/cases/case-details/12345");
        c2UploadedTemplate.setDocumentLink(generateAttachedDocumentLink(C2_DOCUMENT_BINARY)
            .map(JSONObject::toMap)
            .orElse(null));

        return c2UploadedTemplate;
    }
}
