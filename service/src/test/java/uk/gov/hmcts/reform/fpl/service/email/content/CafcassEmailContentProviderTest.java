package uk.gov.hmcts.reform.fpl.service.email.content;

import com.google.common.collect.ImmutableList;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.notify.submittedcase.SubmitCaseCafcassTemplate;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.utils.TestDataHelper;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseData;
import static uk.gov.hmcts.reform.fpl.utils.NotifyAttachedDocumentLinkHelper.generateAttachedDocumentLink;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@ContextConfiguration(classes = {CafcassEmailContentProvider.class, LookupTestConfig.class})
class CafcassEmailContentProviderTest extends AbstractEmailContentProviderTest {

    @Autowired
    private CafcassEmailContentProvider cafcassEmailContentProvider;

    private static final byte[] APPLICATION_BINARY = TestDataHelper.DOCUMENT_CONTENT;

    private static DocumentReference applicationDocument = testDocumentReference();

    @BeforeEach
    void init() {
        when(documentDownloadService.downloadDocument(applicationDocument.getBinaryUrl()))
            .thenReturn(APPLICATION_BINARY);
    }

    @Test
    void shouldReturnCompletedNotifyData() {
        List<String> ordersAndDirections = ImmutableList.of("Emergency protection order",
            "Contact with any named person");

        SubmitCaseCafcassTemplate cafcassSubmissionTemplate = new SubmitCaseCafcassTemplate();
        cafcassSubmissionTemplate.setCafcass(CAFCASS_NAME);
        cafcassSubmissionTemplate.setLocalAuthority(LOCAL_AUTHORITY_NAME);
        cafcassSubmissionTemplate.setDataPresent(YES.getValue());
        cafcassSubmissionTemplate.setFullStop(NO.getValue());
        cafcassSubmissionTemplate.setOrdersAndDirections(ordersAndDirections);
        cafcassSubmissionTemplate.setTimeFramePresent(YES.getValue());
        cafcassSubmissionTemplate.setTimeFrameValue("same day");
        cafcassSubmissionTemplate.setUrgentHearing(YES.getValue());
        cafcassSubmissionTemplate.setNonUrgentHearing(NO.getValue());
        cafcassSubmissionTemplate.setFirstRespondentName("Smith");
        cafcassSubmissionTemplate.setReference(CASE_REFERENCE);
        cafcassSubmissionTemplate.setCaseUrl(caseUrl(CASE_REFERENCE));
        cafcassSubmissionTemplate.setDocumentLink(generateAttachedDocumentLink(APPLICATION_BINARY)
            .map(JSONObject::toMap)
            .orElse(null));

        CaseData caseData = populatedCaseData(Map.of("applicationBinaryUrl", applicationDocument.getBinaryUrl()));

        assertThat(cafcassEmailContentProvider.buildCafcassSubmissionNotification(caseData))
            .usingRecursiveComparison().isEqualTo(cafcassSubmissionTemplate);
    }

    @Test
    void shouldReturnIncompletedNotifyData() {
        SubmitCaseCafcassTemplate cafcassSubmissionTemplate = new SubmitCaseCafcassTemplate();
        cafcassSubmissionTemplate.setCafcass(CAFCASS_NAME);
        cafcassSubmissionTemplate.setLocalAuthority(LOCAL_AUTHORITY_NAME);
        cafcassSubmissionTemplate.setDataPresent(YES.getValue());
        cafcassSubmissionTemplate.setFullStop(NO.getValue());
        cafcassSubmissionTemplate.setOrdersAndDirections(List.of("Care order"));
        cafcassSubmissionTemplate.setTimeFramePresent(NO.getValue());
        cafcassSubmissionTemplate.setTimeFrameValue("");
        cafcassSubmissionTemplate.setUrgentHearing(NO.getValue());
        cafcassSubmissionTemplate.setNonUrgentHearing(NO.getValue());
        cafcassSubmissionTemplate.setFirstRespondentName("");
        cafcassSubmissionTemplate.setReference(CASE_REFERENCE);
        cafcassSubmissionTemplate.setCaseUrl(caseUrl(CASE_REFERENCE));
        cafcassSubmissionTemplate.setDocumentLink(generateAttachedDocumentLink(APPLICATION_BINARY)
            .map(JSONObject::toMap).orElse(null));

        assertThat(cafcassEmailContentProvider.buildCafcassSubmissionNotification(buildCaseData(applicationDocument)))
            .usingRecursiveComparison().isEqualTo(cafcassSubmissionTemplate);
    }

    private CaseData buildCaseData(DocumentReference applicationDocument) {
        return CaseData.builder()
            .id(Long.valueOf(CASE_REFERENCE))
            .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
            .submittedForm(applicationDocument)
            .orders(Orders.builder()
                .orderType(List.of(CARE_ORDER))
                .build())
            .build();
    }
}
