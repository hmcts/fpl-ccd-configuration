package uk.gov.hmcts.reform.fpl.service.email.content;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.notify.allocatedjudge.AllocatedJudgeTemplateForC2;
import uk.gov.hmcts.reform.fpl.model.notify.c2uploaded.C2UploadedTemplate;
import uk.gov.hmcts.reform.fpl.service.CaseConverter;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.util.Map;

import static java.lang.String.format;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseDetails;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentBinaries;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@ContextConfiguration(classes = {C2UploadedEmailContentProvider.class, FixedTimeConfiguration.class,
    CaseConverter.class, JacksonAutoConfiguration.class})
@TestPropertySource(properties = {"manage-case.ui.base.url=http://fake-url"})
class C2UploadedEmailContentProviderTest extends AbstractEmailContentProviderTest {

    @Autowired
    private CaseConverter caseConverter;

    @Autowired
    private C2UploadedEmailContentProvider c2UploadedEmailContentProvider;

    private static final byte[] C2_DOCUMENT_BINARY = testDocumentBinaries();
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
        DocumentReference uploadedC2 = DocumentReference.builder()
            .filename(randomAlphanumeric(10))
            .url(randomAlphanumeric(10))
            .binaryUrl("http://dm-store:8080/documents/b28f859b-7521-4c84-9057-47e56afd773f/binary")
            .build();

        CaseData caseData = caseConverter.convert(populatedCaseDetails(
            Map.of("applicationBinaryUrl", applicationDocument.getBinaryUrl())));

        C2UploadedTemplate c2UploadedTemplateParameters = getC2UploadedTemplateParameters();

        assertThat(c2UploadedEmailContentProvider.buildC2UploadNotificationTemplate(caseData, uploadedC2))
            .isEqualToComparingFieldByField(c2UploadedTemplateParameters);
    }

    @Test
    void shouldReturnExpectedPbaPaymentNotTakenNotification() {
        Map<String, Object> expectedMap = ImmutableMap.<String, Object>builder()
            .put("caseUrl", caseUrl(CASE_REFERENCE, "C2Tab"))
            .build();

        CaseData caseData = CaseData.builder()
            .id(Long.valueOf(CASE_REFERENCE))
            .build();

        assertThat(c2UploadedEmailContentProvider.buildC2UploadPbaPaymentNotTakenNotification(caseData))
            .isEqualTo(expectedMap);
    }

    @Test
    void shouldReturnExpectedMapWithAllocatedJudgeDetails() {
        CaseData caseData = caseConverter.convert(populatedCaseDetails());

        AllocatedJudgeTemplateForC2 expectedData = getAllocatedJudgeParametersForC2();
        AllocatedJudgeTemplateForC2 actualData = c2UploadedEmailContentProvider
            .buildC2UploadNotificationForAllocatedJudge(caseData);

        assertThat(actualData).isEqualToComparingFieldByField(expectedData);
    }

    private AllocatedJudgeTemplateForC2 getAllocatedJudgeParametersForC2() {
        AllocatedJudgeTemplateForC2 allocatedJudgeTemplateForC2 = new AllocatedJudgeTemplateForC2();

        allocatedJudgeTemplateForC2.setCaseUrl("http://fake-url/cases/case-details/12345#C2Tab");
        allocatedJudgeTemplateForC2.setCallout(format("Smith, %s", CASE_REFERENCE));
        allocatedJudgeTemplateForC2.setJudgeTitle("Her Honour Judge");
        allocatedJudgeTemplateForC2.setJudgeName("Moley");
        allocatedJudgeTemplateForC2.setRespondentLastName("Smith");

        return allocatedJudgeTemplateForC2;
    }

    private C2UploadedTemplate getC2UploadedTemplateParameters() {
        C2UploadedTemplate c2UploadedTemplate = new C2UploadedTemplate();

        c2UploadedTemplate.setCallout(format("Smith, %s", CASE_REFERENCE));
        c2UploadedTemplate.setRespondentLastName("Smith");
        c2UploadedTemplate.setCaseUrl("http://fake-url/cases/case-details/12345#C2Tab");
        c2UploadedTemplate.setDocumentUrl("http://fake-url/documents/b28f859b-7521-4c84-9057-47e56afd773f/binary");

        return c2UploadedTemplate;
    }
}
