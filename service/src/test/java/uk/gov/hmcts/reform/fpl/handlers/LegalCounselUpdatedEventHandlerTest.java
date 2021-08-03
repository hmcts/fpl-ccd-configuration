package uk.gov.hmcts.reform.fpl.handlers;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.events.LegalCounsellorAdded;
import uk.gov.hmcts.reform.fpl.events.LegalCounsellorRemoved;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.LegalCounsellor;
import uk.gov.hmcts.reform.fpl.model.notify.legalcounsel.LegalCounsellorAddedNotifyTemplate;
import uk.gov.hmcts.reform.fpl.model.notify.legalcounsel.LegalCounsellorRemovedNotifyTemplate;
import uk.gov.hmcts.reform.fpl.service.CaseAccessService;
import uk.gov.hmcts.reform.fpl.service.CaseUrlService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import java.time.LocalDate;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.Constants.TEST_CASE_ID_AS_LONG;
import static uk.gov.hmcts.reform.fpl.Constants.TEST_FORMATTED_CASE_ID;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.BARRISTER;
import static uk.gov.hmcts.reform.fpl.enums.ChildGender.GIRL;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testChild;

class LegalCounselUpdatedEventHandlerTest {

    private static final String LEGAL_COUNSELLOR_ADDED_EMAIL_TEMPLATE = "2f5826a5-f5c4-41aa-8d75-2bfee7dade87";
    private static final String LEGAL_COUNSELLOR_REMOVED_EMAIL_TEMPLATE = "85494117-1030-4c57-a1d7-f6ce32a81454";
    private static final Pair<String, LegalCounsellor> TEST_LEGAL_COUNCILLOR = Pair.of(
        "testUserId",
        LegalCounsellor.builder().email("ted.baker@example.com").firstName("Ted").lastName("Baker").build()
    );

    private CaseAccessService caseAccessService = mock(CaseAccessService.class);
    private NotificationService notificationService = mock(NotificationService.class);
    private CaseUrlService caseUrlService = mock(CaseUrlService.class);
    private EmailNotificationHelper helper = new EmailNotificationHelper(null);
    private LegalCounselUpdatedEventHandler legalCounselUpdatedEventHandler = new LegalCounselUpdatedEventHandler(
        caseAccessService,
        notificationService,
        caseUrlService,
        helper);

    @Test
    void shouldGrantAccessToUserAndNotifyThem() {
        when(caseUrlService.getCaseUrl(TEST_CASE_ID_AS_LONG)).thenReturn("myUrl");
        CaseData caseData = CaseData.builder()
            .id(TEST_CASE_ID_AS_LONG)
            .children1(asList(testChild("Beatrice", "Langley", GIRL, LocalDate.now())))
            .build();

        legalCounselUpdatedEventHandler.handleLegalCounsellorAddedEvent(new LegalCounsellorAdded(caseData,
            TEST_LEGAL_COUNCILLOR));

        verify(caseAccessService).grantCaseRoleToUser(TEST_CASE_ID_AS_LONG, "testUserId", BARRISTER);
        verify(notificationService).sendEmail(LEGAL_COUNSELLOR_ADDED_EMAIL_TEMPLATE,
            "ted.baker@example.com",
            LegalCounsellorAddedNotifyTemplate.builder()
                .childLastName("Langley")
                .caseId(TEST_FORMATTED_CASE_ID)
                .caseUrl("myUrl")
                .build(),
            TEST_CASE_ID_AS_LONG);
    }

    @Test
    void shouldRevokeAccessFromUserAndNotifyThem() {
        CaseData caseData = CaseData.builder()
            .id(TEST_CASE_ID_AS_LONG)
            .children1(asList(testChild("Beatrice", "Langley", GIRL, LocalDate.now())))
            .caseName("testCaseName")
            .build();

        legalCounselUpdatedEventHandler.handleLegalCounsellorRemovedEvent(new LegalCounsellorRemoved(caseData,
            "Peter Taylor Solicitors Ltd",
            TEST_LEGAL_COUNCILLOR));

        verify(caseAccessService).revokeCaseRoleFromUser(TEST_CASE_ID_AS_LONG, "testUserId", BARRISTER);
        verify(notificationService).sendEmail(LEGAL_COUNSELLOR_REMOVED_EMAIL_TEMPLATE,
            "ted.baker@example.com",
            LegalCounsellorRemovedNotifyTemplate.builder()
                .caseName("testCaseName")
                .childLastName("Langley")
                .salutation("Dear Ted Baker")
                .clientFullName("Peter Taylor Solicitors Ltd")//Solicitor firm
                .ccdNumber(TEST_FORMATTED_CASE_ID)
                .build(),
            TEST_CASE_ID_AS_LONG);
    }

}
