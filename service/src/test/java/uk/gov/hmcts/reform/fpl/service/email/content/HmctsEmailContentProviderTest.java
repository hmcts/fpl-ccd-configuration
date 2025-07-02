package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.config.HighCourtAdminEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.hearing.HearingUrgencyType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.Court;
import uk.gov.hmcts.reform.fpl.model.Hearing;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.notify.submittedcase.SubmitCaseHmctsTemplate;
import uk.gov.hmcts.reform.fpl.service.CourtService;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;
import uk.gov.hmcts.reform.fpl.utils.TestDataHelper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.config.utils.EmergencyProtectionOrderDirectionsType.CONTACT_WITH_NAMED_PERSON;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.EMERGENCY_PROTECTION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ContextConfiguration(classes = {HmctsEmailContentProvider.class, LookupTestConfig.class, CourtService.class,
    HighCourtAdminEmailLookupConfiguration.class})
class HmctsEmailContentProviderTest extends AbstractEmailContentProviderTest {

    private static final byte[] APPLICATION_BINARY = TestDataHelper.DOCUMENT_CONTENT;
    private static final String BINARY_URL = "/documents/applicationBinaryUrl";
    private static final DocumentReference C110A = mock(DocumentReference.class);
    private static final String CHILD_LAST_NAME = "Holmes";

    @Autowired
    private HmctsEmailContentProvider underTest;

    @MockBean
    private EmailNotificationHelper helper;

    @BeforeEach
    void init() {
        when(C110A.getBinaryUrl()).thenReturn(BINARY_URL);
        when(documentDownloadService.downloadDocument(BINARY_URL)).thenReturn(APPLICATION_BINARY);

        when(helper.getEldestChildLastName(anyList())).thenReturn(CHILD_LAST_NAME);
    }

    @Test
    void shouldReturnExpectedMapWithValidCaseDetails() {
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
                .hearingUrgencyType(HearingUrgencyType.SAME_DAY)
                .build())
            .build();

        SubmitCaseHmctsTemplate expectedTempalte = SubmitCaseHmctsTemplate.builder()
            .court(COURT_NAME)
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
            .documentLink("http://fake-url" + BINARY_URL)
            .childLastName(CHILD_LAST_NAME)
            .build();

        assertThat(underTest.buildHmctsSubmissionNotification(caseData)).isEqualTo(expectedTempalte);
    }

    @Test
    void shouldReturnExpectedMapWithValidCaseDetailsEvenWhenCaseLocalAuthorityDoesntExist() {
        CaseData caseData = CaseData.builder()
            .id(Long.valueOf(CASE_REFERENCE))
            .localAuthorities(wrapElements(LocalAuthority.builder()
                .name(LOCAL_AUTHORITY_NAME)
                .build()))
            .court(Court.builder()
                .name(COURT_NAME)
                .build())
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
                .hearingUrgencyType(HearingUrgencyType.SAME_DAY)
                .build())
            .build();

        SubmitCaseHmctsTemplate expectedTemplate = SubmitCaseHmctsTemplate.builder()
            .court(COURT_NAME)
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
            .documentLink("http://fake-url" + BINARY_URL)
            .childLastName(CHILD_LAST_NAME)
            .build();

        assertThat(underTest.buildHmctsSubmissionNotification(caseData)).isEqualTo(expectedTemplate);
    }
}
