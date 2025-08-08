package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.events.legalcounsel.LegalCounsellorAdded;
import uk.gov.hmcts.reform.fpl.events.legalcounsel.LegalCounsellorRemoved;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.LegalCounsellor;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.notify.legalcounsel.LegalCounsellorAddedNotifyTemplate;
import uk.gov.hmcts.reform.fpl.model.notify.legalcounsel.LegalCounsellorRemovedNotifyTemplate;
import uk.gov.hmcts.reform.fpl.service.CaseAccessService;
import uk.gov.hmcts.reform.fpl.service.UserService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.LegalCounsellorEmailContentProvider;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.Constants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.LEGAL_COUNSELLOR_REMOVED_THEMSELVES;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.LEGAL_COUNSELLOR_SELF_REMOVED_EMAIL_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.BARRISTER;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
class LegalCounselUpdatedEventHandlerTest {

    private static final String LEGAL_COUNSELLOR_ADDED_EMAIL_TEMPLATE = "2f5826a5-f5c4-41aa-8d75-2bfee7dade87";
    private static final String LEGAL_COUNSELLOR_REMOVED_EMAIL_TEMPLATE = "85494117-1030-4c57-a1d7-f6ce32a81454";

    private static final String TEST_COUNSELLOR_EMAIL_ADDRESS = "ted.baker@example.com";
    private static final String TEST_COUNCILLOR_USER_ID = "testUserId";
    private static final LegalCounsellor TEST_LEGAL_COUNCILLOR = LegalCounsellor.builder()
        .email(TEST_COUNSELLOR_EMAIL_ADDRESS)
        .firstName("Ted")
        .lastName("Baker")
        .userId(TEST_COUNCILLOR_USER_ID)
        .build();

    @Mock
    private CaseAccessService caseAccessService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private LegalCounsellorEmailContentProvider contentProvider;

    @Mock
    private UserService userService;

    @InjectMocks
    private LegalCounselUpdatedEventHandler underTest;

    private CaseData caseData;

    @BeforeEach
    void setUp() {
        caseData = CaseData.builder().id(TEST_CASE_ID).build();
    }

    @Test
    void shouldGrantAccessToUserAndNotifyThem() {
        LegalCounsellorAddedNotifyTemplate expectedTemplate = mock(LegalCounsellorAddedNotifyTemplate.class);
        when(contentProvider.buildLegalCounsellorAddedNotificationTemplate(caseData))
            .thenReturn(expectedTemplate);

        underTest.handleLegalCounsellorAddedEvent(new LegalCounsellorAdded(caseData, TEST_LEGAL_COUNCILLOR));

        verify(caseAccessService).grantCaseRoleToUser(TEST_CASE_ID, TEST_COUNCILLOR_USER_ID, BARRISTER);
        verify(notificationService).sendEmail(
            LEGAL_COUNSELLOR_ADDED_EMAIL_TEMPLATE, TEST_COUNSELLOR_EMAIL_ADDRESS,
            expectedTemplate, TEST_CASE_ID
        );
    }

    @Test
    void shouldRevokeAccessFromUserAndNotifyThem() {
        LegalCounsellorRemovedNotifyTemplate expectedTemplate = mock(LegalCounsellorRemovedNotifyTemplate.class);
        LegalCounsellorRemoved event = new LegalCounsellorRemoved(caseData, "Test Solicitors", TEST_LEGAL_COUNCILLOR);

        when(userService.getUserEmail()).thenReturn("solicitor@test.com");
        when(contentProvider.buildLegalCounsellorRemovedNotificationTemplate(caseData, event))
            .thenReturn(expectedTemplate);

        underTest.handleLegalCounsellorRemovedEvent(event);

        verify(caseAccessService).revokeCaseRoleFromUser(TEST_CASE_ID, TEST_COUNCILLOR_USER_ID, BARRISTER);
        verify(notificationService).sendEmail(
            LEGAL_COUNSELLOR_REMOVED_EMAIL_TEMPLATE, TEST_COUNSELLOR_EMAIL_ADDRESS,
            expectedTemplate, TEST_CASE_ID
        );
    }

    @Test
    void shouldRevokeAccessFromUserAndNotifyThemIfBarrister() {
        LegalCounsellorRemovedNotifyTemplate expectedTemplate = mock(LegalCounsellorRemovedNotifyTemplate.class);
        LegalCounsellorRemoved event = new LegalCounsellorRemoved(caseData, "Test Solicitors", TEST_LEGAL_COUNCILLOR);

        when(userService.getUserEmail()).thenReturn(TEST_COUNSELLOR_EMAIL_ADDRESS);
        when(contentProvider.buildLegalCounsellorRemovedNotificationTemplate(caseData, event))
            .thenReturn(expectedTemplate);

        underTest.handleLegalCounsellorRemovedEvent(event);

        verify(caseAccessService).revokeCaseRoleFromUser(TEST_CASE_ID, TEST_COUNCILLOR_USER_ID, BARRISTER);
        verify(notificationService).sendEmail(
            LEGAL_COUNSELLOR_SELF_REMOVED_EMAIL_TEMPLATE, TEST_COUNSELLOR_EMAIL_ADDRESS,
            expectedTemplate, TEST_CASE_ID
        );
    }

    @Test
    void shouldNotifyAllRespondentSolicitorTheyAreActingForIfBarristerRemovedThemselves() {
        CaseData caseData = CaseData.builder()
            .id(TEST_CASE_ID)
            .respondents1(wrapElements(
                Respondent.builder()
                    .legalCounsellors(wrapElements(TEST_LEGAL_COUNCILLOR))
                    .solicitor(RespondentSolicitor.builder().email("respondent.solicitor@test.com").build())
                    .build(),
                Respondent.builder()
                    .legalCounsellors(wrapElements(TEST_LEGAL_COUNCILLOR))
                    .solicitor(RespondentSolicitor.builder().email("respondent.solicitor2@test.com").build())
                    .build()))
            .build();
        LegalCounsellorRemoved event = new LegalCounsellorRemoved(caseData, "Test Solicitors", TEST_LEGAL_COUNCILLOR);

        LegalCounsellorRemovedNotifyTemplate expectedTemplateToBarrister =
            mock(LegalCounsellorRemovedNotifyTemplate.class);
        LegalCounsellorRemovedNotifyTemplate expectedTemplateToSolicitor =
            mock(LegalCounsellorRemovedNotifyTemplate.class);

        when(userService.getUserEmail()).thenReturn(TEST_COUNSELLOR_EMAIL_ADDRESS);
        when(contentProvider.buildLegalCounsellorRemovedNotificationTemplate(caseData, event))
            .thenReturn(expectedTemplateToBarrister);
        when(contentProvider.buildLegalCounsellorRemovedThemselvesNotificationTemplate(caseData,
            TEST_LEGAL_COUNCILLOR.getFullName())).thenReturn(expectedTemplateToSolicitor);

        underTest.handleLegalCounsellorRemovedEvent(event);

        verify(caseAccessService).revokeCaseRoleFromUser(TEST_CASE_ID, TEST_COUNCILLOR_USER_ID, BARRISTER);
        verify(notificationService).sendEmail(
            LEGAL_COUNSELLOR_SELF_REMOVED_EMAIL_TEMPLATE, TEST_COUNSELLOR_EMAIL_ADDRESS,
            expectedTemplateToBarrister, TEST_CASE_ID
        );

        verify(notificationService).sendEmail(
            LEGAL_COUNSELLOR_REMOVED_THEMSELVES, "respondent.solicitor@test.com",
            expectedTemplateToSolicitor, TEST_CASE_ID
        );
        verify(notificationService).sendEmail(
            LEGAL_COUNSELLOR_REMOVED_THEMSELVES, "respondent.solicitor2@test.com",
            expectedTemplateToSolicitor, TEST_CASE_ID
        );
    }

    @Test
    void shouldNotifyAllChildSolicitorTheyAreActingForIfBarristerRemovedThemselves() {
        CaseData caseData = CaseData.builder()
            .id(TEST_CASE_ID)
            .children1(wrapElements(
                Child.builder()
                    .legalCounsellors(wrapElements(TEST_LEGAL_COUNCILLOR))
                    .solicitor(RespondentSolicitor.builder().email("child.solicitor@test.com").build())
                    .build(),
                Child.builder()
                    .legalCounsellors(wrapElements(TEST_LEGAL_COUNCILLOR))
                    .solicitor(RespondentSolicitor.builder().email("child.solicitor2@test.com").build())
                    .build()))
            .build();
        LegalCounsellorRemoved event = new LegalCounsellorRemoved(caseData, "Test Solicitors", TEST_LEGAL_COUNCILLOR);

        LegalCounsellorRemovedNotifyTemplate expectedTemplateToBarrister =
            mock(LegalCounsellorRemovedNotifyTemplate.class);
        LegalCounsellorRemovedNotifyTemplate expectedTemplateToSolicitor =
            mock(LegalCounsellorRemovedNotifyTemplate.class);

        when(userService.getUserEmail()).thenReturn(TEST_COUNSELLOR_EMAIL_ADDRESS);
        when(contentProvider.buildLegalCounsellorRemovedNotificationTemplate(caseData, event))
            .thenReturn(expectedTemplateToBarrister);
        when(contentProvider.buildLegalCounsellorRemovedThemselvesNotificationTemplate(caseData,
            TEST_LEGAL_COUNCILLOR.getFullName())).thenReturn(expectedTemplateToSolicitor);

        underTest.handleLegalCounsellorRemovedEvent(event);

        verify(caseAccessService).revokeCaseRoleFromUser(TEST_CASE_ID, TEST_COUNCILLOR_USER_ID, BARRISTER);
        verify(notificationService).sendEmail(
            LEGAL_COUNSELLOR_SELF_REMOVED_EMAIL_TEMPLATE, TEST_COUNSELLOR_EMAIL_ADDRESS,
            expectedTemplateToBarrister, TEST_CASE_ID
        );

        verify(notificationService).sendEmail(
            LEGAL_COUNSELLOR_REMOVED_THEMSELVES, "child.solicitor@test.com",
            expectedTemplateToSolicitor, TEST_CASE_ID
        );
        verify(notificationService).sendEmail(
            LEGAL_COUNSELLOR_REMOVED_THEMSELVES, "child.solicitor2@test.com",
            expectedTemplateToSolicitor, TEST_CASE_ID
        );
    }

}
