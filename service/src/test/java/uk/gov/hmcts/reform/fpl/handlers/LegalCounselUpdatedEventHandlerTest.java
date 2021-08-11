package uk.gov.hmcts.reform.fpl.handlers;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.events.LegalCounsellorAdded;
import uk.gov.hmcts.reform.fpl.events.LegalCounsellorRemoved;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.LegalCounsellor;
import uk.gov.hmcts.reform.fpl.model.notify.legalcounsel.LegalCounsellorAddedNotifyTemplate;
import uk.gov.hmcts.reform.fpl.model.notify.legalcounsel.LegalCounsellorRemovedNotifyTemplate;
import uk.gov.hmcts.reform.fpl.service.CaseAccessService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.LegalCounsellorEmailContentProvider;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.Constants.TEST_CASE_ID_AS_LONG;
import static uk.gov.hmcts.reform.fpl.Constants.TEST_FORMATTED_CASE_ID;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.BARRISTER;

@ExtendWith(MockitoExtension.class)
class LegalCounselUpdatedEventHandlerTest {

    private static final String LEGAL_COUNSELLOR_ADDED_EMAIL_TEMPLATE = "2f5826a5-f5c4-41aa-8d75-2bfee7dade87";
    private static final String LEGAL_COUNSELLOR_REMOVED_EMAIL_TEMPLATE = "85494117-1030-4c57-a1d7-f6ce32a81454";

    private static final String TEST_COUNSELLOR_EMAIL_ADDRESS = "ted.baker@example.com";
    private static final String TEST_COUNCILLOR_USER_ID = "testUserId";
    private static final Pair<String, LegalCounsellor> TEST_LEGAL_COUNCILLOR = Pair.of(
        TEST_COUNCILLOR_USER_ID,
        LegalCounsellor.builder().email(TEST_COUNSELLOR_EMAIL_ADDRESS).firstName("Ted").lastName("Baker").build()
    );

    @Mock
    private CaseAccessService caseAccessService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private LegalCounsellorEmailContentProvider legalCounsellorEmailContentProvider;

    @InjectMocks
    private LegalCounselUpdatedEventHandler legalCounselUpdatedEventHandler;

    private CaseData caseData;

    @BeforeEach
    void setUp() {
        caseData = CaseData.builder()
            .id(TEST_CASE_ID_AS_LONG)
            .build();
    }

    @Test
    void shouldGrantAccessToUserAndNotifyThem() {
        LegalCounsellorAddedNotifyTemplate expectedTemplate = LegalCounsellorAddedNotifyTemplate.builder()
            .caseId(TEST_FORMATTED_CASE_ID)
            .build();
        when(legalCounsellorEmailContentProvider.buildLegalCounsellorAddedNotificationTemplate(caseData))
            .thenReturn(expectedTemplate);

        legalCounselUpdatedEventHandler.handleLegalCounsellorAddedEvent(new LegalCounsellorAdded(caseData,
            TEST_LEGAL_COUNCILLOR));

        verify(caseAccessService).grantCaseRoleToUser(TEST_CASE_ID_AS_LONG, TEST_LEGAL_COUNCILLOR.getKey(), BARRISTER);
        verify(notificationService).sendEmail(LEGAL_COUNSELLOR_ADDED_EMAIL_TEMPLATE,
            TEST_COUNSELLOR_EMAIL_ADDRESS,
            expectedTemplate,
            TEST_CASE_ID_AS_LONG);
    }

    @Test
    void shouldRevokeAccessFromUserAndNotifyThem() {
        LegalCounsellorRemovedNotifyTemplate expectedTemplate = LegalCounsellorRemovedNotifyTemplate.builder()
            .ccdNumber(TEST_FORMATTED_CASE_ID)
            .build();
        LegalCounsellorRemoved event = new LegalCounsellorRemoved(caseData, "Test Solicitors", TEST_LEGAL_COUNCILLOR);
        when(legalCounsellorEmailContentProvider.buildLegalCounsellorRemovedNotificationTemplate(caseData, event))
            .thenReturn(expectedTemplate);

        legalCounselUpdatedEventHandler.handleLegalCounsellorRemovedEvent(event);

        verify(caseAccessService).revokeCaseRoleFromUser(TEST_CASE_ID_AS_LONG, TEST_COUNCILLOR_USER_ID, BARRISTER);
        verify(notificationService).sendEmail(LEGAL_COUNSELLOR_REMOVED_EMAIL_TEMPLATE,
            TEST_COUNSELLOR_EMAIL_ADDRESS,
            expectedTemplate,
            TEST_CASE_ID_AS_LONG);
    }

}
