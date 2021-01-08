package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
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
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseData;

@ContextConfiguration(classes = {HmctsEmailContentProvider.class, LookupTestConfig.class})
class HmctsEmailContentProviderTest extends AbstractEmailContentProviderTest {

    @Autowired
    private HmctsEmailContentProvider hmctsEmailContentProvider;

    private static final byte[] APPLICATION_BINARY = TestDataHelper.DOCUMENT_CONTENT;

    private static DocumentReference applicationDocument = DocumentReference.builder()
        .binaryUrl("applicationBinaryUrl")
        .build();

    @BeforeEach
    void init() {
        when(documentDownloadService.downloadDocument(applicationDocument.getBinaryUrl()))
            .thenReturn(APPLICATION_BINARY);
    }

    @Test
    void shouldReturnExpectedMapWithValidCaseDetails() {
        List<String> ordersAndDirections = List.of("Emergency protection order", "Contact with any named person");

        SubmitCaseHmctsTemplate hmctsSubmissionTemplate = SubmitCaseHmctsTemplate.builder()
            .court(COURT_NAME)
            .localAuthority(LOCAL_AUTHORITY_NAME)
            .dataPresent(YES.getValue())
            .fullStop(NO.getValue())
            .ordersAndDirections(ordersAndDirections)
            .timeFramePresent(YES.getValue())
            .timeFrameValue("same day")
            .urgentHearing(YES.getValue())
            .nonUrgentHearing(NO.getValue())
            .firstRespondentName("Smith")
            .reference(CASE_REFERENCE)
            .caseUrl(caseUrl(CASE_REFERENCE))
            .documentLink("applicationBinaryUrl")
            .build();

        CaseData caseData = populatedCaseData(Map.of("applicationBinaryUrl", applicationDocument.getBinaryUrl()));

        assertThat(hmctsEmailContentProvider.buildHmctsSubmissionNotification(caseData))
            .usingRecursiveComparison().isEqualTo(hmctsSubmissionTemplate);
    }

    @Test
    void shouldReturnSuccessfullyWithIncompleteCaseDetails() {
        SubmitCaseHmctsTemplate hmctsSubmissionTemplate = SubmitCaseHmctsTemplate.builder()
            .court(COURT_NAME)
            .localAuthority(LOCAL_AUTHORITY_NAME)
            .dataPresent(YES.getValue())
            .fullStop(NO.getValue())
            .ordersAndDirections(List.of("Care order"))
            .timeFramePresent(NO.getValue())
            .timeFrameValue("")
            .urgentHearing(NO.getValue())
            .nonUrgentHearing(NO.getValue())
            .firstRespondentName("")
            .reference(CASE_REFERENCE)
            .caseUrl(caseUrl(CASE_REFERENCE))
            .documentLink("applicationBinaryUrl")
            .build();

        assertThat(hmctsEmailContentProvider.buildHmctsSubmissionNotification(buildCaseData(applicationDocument)))
            .usingRecursiveComparison().isEqualTo(hmctsSubmissionTemplate);
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
