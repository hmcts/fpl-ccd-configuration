package uk.gov.hmcts.reform.fpl.service.email.content;

import com.google.common.collect.ImmutableMap;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.notify.submittedcase.SubmitCaseHmctsTemplate;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.utils.TestDataHelper;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseDetails;
import static uk.gov.hmcts.reform.fpl.utils.NotifyAttachedDocumentLinkHelper.generateAttachedDocumentLink;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@ContextConfiguration(classes = {HmctsEmailContentProvider.class, LookupTestConfig.class})
class HmctsEmailContentProviderTest extends AbstractEmailContentProviderTest {

    @Autowired
    private HmctsEmailContentProvider hmctsEmailContentProvider;

    private static final byte[] APPLICATION_BINARY = TestDataHelper.DOCUMENT_CONTENT;

    private static DocumentReference applicationDocument;

    @BeforeEach
    void init() {
        applicationDocument = testDocumentReference();
        when(documentDownloadService.downloadDocument(applicationDocument.getBinaryUrl()))
            .thenReturn(APPLICATION_BINARY);
    }

    @Test
    void shouldReturnExpectedMapWithValidCaseDetails() {
        List<String> ordersAndDirections = List.of("Emergency protection order", "Contact with any named person");

        SubmitCaseHmctsTemplate hmctsSubmissionTemplate = new SubmitCaseHmctsTemplate();
        hmctsSubmissionTemplate.setCourt(COURT_NAME);
        hmctsSubmissionTemplate.setLocalAuthority(LOCAL_AUTHORITY_NAME);
        hmctsSubmissionTemplate.setDataPresent(YES.getValue());
        hmctsSubmissionTemplate.setFullStop(NO.getValue());
        hmctsSubmissionTemplate.setOrdersAndDirections(ordersAndDirections);
        hmctsSubmissionTemplate.setTimeFramePresent(YES.getValue());
        hmctsSubmissionTemplate.setTimeFrameValue("same day");
        hmctsSubmissionTemplate.setUrgentHearing(YES.getValue());
        hmctsSubmissionTemplate.setNonUrgentHearing(NO.getValue());
        hmctsSubmissionTemplate.setFirstRespondentName("Smith");
        hmctsSubmissionTemplate.setReference(CASE_REFERENCE);
        hmctsSubmissionTemplate.setCaseUrl(caseUrl(CASE_REFERENCE));
        hmctsSubmissionTemplate.setDocumentLink(generateAttachedDocumentLink(APPLICATION_BINARY)
            .map(JSONObject::toMap)
            .orElse(null));

        CaseDetails caseDetails = populatedCaseDetails(
            Map.of("applicationBinaryUrl", applicationDocument.getBinaryUrl()));

        assertThat(hmctsEmailContentProvider.buildHmctsSubmissionNotification(caseDetails, LOCAL_AUTHORITY_CODE))
            .isEqualToComparingFieldByField(hmctsSubmissionTemplate);
    }

    @Test
    void shouldReturnSuccessfullyWithIncompleteCaseDetails() {
        SubmitCaseHmctsTemplate hmctsSubmissionTemplate = new SubmitCaseHmctsTemplate();

        hmctsSubmissionTemplate.setCourt(COURT_NAME);
        hmctsSubmissionTemplate.setLocalAuthority(LOCAL_AUTHORITY_NAME);
        hmctsSubmissionTemplate.setDataPresent(YES.getValue());
        hmctsSubmissionTemplate.setFullStop(NO.getValue());
        hmctsSubmissionTemplate.setOrdersAndDirections(List.of("Care order"));
        hmctsSubmissionTemplate.setTimeFramePresent(NO.getValue());
        hmctsSubmissionTemplate.setTimeFrameValue("");
        hmctsSubmissionTemplate.setUrgentHearing(NO.getValue());
        hmctsSubmissionTemplate.setNonUrgentHearing(NO.getValue());
        hmctsSubmissionTemplate.setFirstRespondentName("");
        hmctsSubmissionTemplate.setReference("123");
        hmctsSubmissionTemplate.setCaseUrl(caseUrl("123"));
        hmctsSubmissionTemplate.setDocumentLink(generateAttachedDocumentLink(APPLICATION_BINARY)
            .map(JSONObject::toMap).orElse(null));

        assertThat(hmctsEmailContentProvider.buildHmctsSubmissionNotification(buildCaseDetails(applicationDocument),
            LOCAL_AUTHORITY_CODE)).isEqualToComparingFieldByField(hmctsSubmissionTemplate);
    }

    private CaseDetails buildCaseDetails(DocumentReference applicationDocument) {
        return CaseDetails.builder()
            .id(123L)
            .data(ImmutableMap.of(
                "submittedForm", applicationDocument,
                "orders", Orders.builder()
                    .orderType(List.of(CARE_ORDER))
                    .build()))
            .build();
    }
}
