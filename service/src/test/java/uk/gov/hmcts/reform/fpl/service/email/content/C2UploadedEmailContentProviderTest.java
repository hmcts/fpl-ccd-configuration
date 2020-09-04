package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
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
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseData;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@ContextConfiguration(classes = {C2UploadedEmailContentProvider.class, FixedTimeConfiguration.class})
@TestPropertySource(properties = {"manage-case.ui.base.url=http://fake-url"})
class C2UploadedEmailContentProviderTest extends AbstractEmailContentProviderTest {

    @Autowired
    private C2UploadedEmailContentProvider c2UploadedEmailContentProvider;

    private static DocumentReference applicationDocument = testDocumentReference();

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

        assertThat(actualParameters).isEqualToComparingFieldByField(expectedParameters);
    }

    @Test
    void shouldReturnExpectedPbaPaymentNotTakenNotification() {
        CaseData caseData = populatedCaseData();

        BaseCaseNotifyData expectedParameters = BaseCaseNotifyData.builder()
            .caseUrl(getCaseUrl(caseData))
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

        assertThat(actualData).isEqualToComparingFieldByField(expectedData);
    }

    private AllocatedJudgeTemplateForC2 getAllocatedJudgeParametersForC2() {
        return AllocatedJudgeTemplateForC2.builder()
            .caseUrl("http://fake-url/cases/case-details/12345")
            .callout(format("Smith, %s", CASE_REFERENCE))
            .judgeTitle("Her Honour Judge")
            .judgeName("Moley")
            .respondentLastName("Smith")
            .build();
    }

    private C2UploadedTemplate getC2UploadedTemplateParameters() {
        C2UploadedTemplate c2UploadedTemplate = new C2UploadedTemplate();

        c2UploadedTemplate.setCallout(format("Smith, %s", CASE_REFERENCE));
        c2UploadedTemplate.setRespondentLastName("Smith");
        c2UploadedTemplate.setCaseUrl("http://fake-url/cases/case-details/12345");
        c2UploadedTemplate.setDocumentUrl("http://fake-url/documents/b28f859b-7521-4c84-9057-47e56afd773f/binary");

        return c2UploadedTemplate;
    }
}
