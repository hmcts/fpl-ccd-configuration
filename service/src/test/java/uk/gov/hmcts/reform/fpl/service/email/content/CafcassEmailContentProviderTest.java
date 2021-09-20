package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.Hearing;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.notify.submittedcase.SubmitCaseCafcassTemplate;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;
import uk.gov.hmcts.reform.fpl.utils.TestDataHelper;

import java.util.Base64;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.config.utils.EmergencyProtectionOrderDirectionsType.CONTACT_WITH_NAMED_PERSON;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.EMERGENCY_PROTECTION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ContextConfiguration(classes = {CafcassEmailContentProvider.class, LookupTestConfig.class})
class CafcassEmailContentProviderTest extends AbstractEmailContentProviderTest {

    private static final byte[] APPLICATION_BINARY = TestDataHelper.DOCUMENT_CONTENT;
    private static final String ENCODED_BINARY = Base64.getEncoder().encodeToString(APPLICATION_BINARY);
    private static final String BINARY_URL = "/documents/applicationBinaryUrl";
    private static final DocumentReference C110A = mock(DocumentReference.class);
    private static final String CHILD_LAST_NAME = "Holmes";

    @Autowired
    private CafcassEmailContentProvider underTest;

    @MockBean
    private EmailNotificationHelper helper;

    @BeforeEach
    void init() {
        when(C110A.getBinaryUrl()).thenReturn(BINARY_URL);
        when(documentDownloadService.downloadDocument(BINARY_URL)).thenReturn(APPLICATION_BINARY);

        when(helper.getEldestChildLastName(anyList())).thenReturn(CHILD_LAST_NAME);
    }

    @Test
    void shouldReturnCompletedNotifyData() {
        SubmitCaseCafcassTemplate cafcassSubmissionTemplate = SubmitCaseCafcassTemplate.builder()
            .cafcass(CAFCASS_NAME)
            .localAuthority(LOCAL_AUTHORITY_NAME)
            .dataPresent(YES.getValue())
            .fullStop(NO.getValue())
            .ordersAndDirections(List.of("Emergency protection order", "Contact with any named person"))
            .timeFramePresent(YES.getValue())
            .timeFrameValue("same day")
            .urgentHearing(YES.getValue())
            .nonUrgentHearing(NO.getValue())
            .firstRespondentName("Smith")
            .reference(CASE_REFERENCE)
            .caseUrl(caseUrl(CASE_REFERENCE))
            .documentLink(Map.of(
                "file", ENCODED_BINARY,
                "is_csv", false
            ))
            .childLastName(CHILD_LAST_NAME)
            .build();

        CaseData caseData = CaseData.builder()
            .id(Long.valueOf(CASE_REFERENCE))
            .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
            .c110A(uk.gov.hmcts.reform.fpl.model.group.C110A.builder()
                .submittedForm(C110A)
                .build())
            .children1(wrapElements(mock(Child.class)))
            .respondents1(wrapElements(Respondent.builder()
                .party(RespondentParty.builder().lastName("Smith").build())
                .build()))
            .orders(Orders.builder()
                .orderType(List.of(EMERGENCY_PROTECTION_ORDER))
                .directions(YES.getValue())
                .emergencyProtectionOrderDirections(List.of(CONTACT_WITH_NAMED_PERSON))
                .build())
            .hearing(Hearing.builder()
                .timeFrame("Same day")
                .build())
            .build();

        assertThat(underTest.buildCafcassSubmissionNotification(caseData)).isEqualTo(cafcassSubmissionTemplate);
    }
}
