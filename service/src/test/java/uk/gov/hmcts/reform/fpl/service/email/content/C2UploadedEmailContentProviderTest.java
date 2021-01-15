package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.notify.BaseCaseNotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.allocatedjudge.AllocatedJudgeTemplateForC2;
import uk.gov.hmcts.reform.fpl.model.notify.c2uploaded.C2UploadedTemplate;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.util.Map;

import static java.lang.String.format;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.TabUrlAnchor.C2;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseData;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentBinaries;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@ContextConfiguration(classes = {C2UploadedEmailContentProvider.class, FixedTimeConfiguration.class})
class C2UploadedEmailContentProviderTest extends AbstractEmailContentProviderTest {

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

        CaseData caseData = populatedCaseData(Map.of("applicationBinaryUrl", applicationDocument.getBinaryUrl()));

        C2UploadedTemplate expectedParameters = getC2UploadedTemplateParameters();
        C2UploadedTemplate actualParameters = c2UploadedEmailContentProvider
            .getNotifyData(caseData, uploadedC2);

        assertThat(actualParameters).usingRecursiveComparison().isEqualTo(expectedParameters);
    }

    @Test
    void shouldReturnExpectedPbaPaymentNotTakenNotification() {
        CaseData caseData = populatedCaseData();

        BaseCaseNotifyData expectedParameters = BaseCaseNotifyData.builder()
            .caseUrl(caseUrl(CASE_REFERENCE, C2))
            .build();

        BaseCaseNotifyData actualParameters = c2UploadedEmailContentProvider
            .getPbaPaymentNotTakenNotifyData(caseData);

        assertThat(actualParameters).isEqualTo(expectedParameters);
    }

    @Test
    void shouldReturnExpectedMapWithAllocatedJudgeDetails() {
        CaseData caseData = populatedCaseData();

        AllocatedJudgeTemplateForC2 expectedData = getAllocatedJudgeParametersForC2();
        AllocatedJudgeTemplateForC2 actualData = c2UploadedEmailContentProvider
            .getNotifyDataForAllocatedJudge(caseData);

        assertThat(actualData).usingRecursiveComparison().isEqualTo(expectedData);
    }

    private AllocatedJudgeTemplateForC2 getAllocatedJudgeParametersForC2() {
        return AllocatedJudgeTemplateForC2.builder()
            .caseUrl(caseUrl(CASE_REFERENCE, C2))
            .callout(format("Smith, %s", CASE_REFERENCE))
            .judgeTitle("Her Honour Judge")
            .judgeName("Moley")
            .respondentLastName("Smith")
            .build();
    }

    private C2UploadedTemplate getC2UploadedTemplateParameters() {
        return C2UploadedTemplate.builder()
            .callout(format("Smith, %s", CASE_REFERENCE))
            .respondentLastName("Smith")
            .caseUrl(caseUrl(CASE_REFERENCE, C2))
            .documentUrl("http://fake-url/documents/b28f859b-7521-4c84-9057-47e56afd773f/binary")
            .build();
    }
}
