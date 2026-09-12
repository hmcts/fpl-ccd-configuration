package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.events.NoticeOfChangeThirdPartyEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.notify.noticeofchange.NoticeOfChangeRespondentSolicitorTemplate;
import uk.gov.hmcts.reform.fpl.service.OrganisationService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.NoticeOfChangeContentProvider;
import uk.gov.hmcts.reform.rd.model.Organisation;
import uk.gov.hmcts.reform.rd.model.SuperUser;

import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.NOTICE_OF_CHANGE_FORMER_REPRESENTATIVE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.NOTICE_OF_CHANGE_NEW_REPRESENTATIVE;

@ExtendWith(MockitoExtension.class)
public class NoticeOfChangeThirdPartyEventHandlerTest {

    private static final Long CASE_ID = 1L;
    private final CaseData caseData = mock(CaseData.class);

    private static final NoticeOfChangeRespondentSolicitorTemplate EXPECTED_TEMPLATE =
        mock(NoticeOfChangeRespondentSolicitorTemplate.class);

    private static final String NEW_EMAIL = "new@test.com";
    private static final String OLD_EMAIL = "old@test.com";

    private final LocalAuthority oldThirdPartyOrgWithEmail = LocalAuthority.builder().id("ABC123")
        .email(OLD_EMAIL).build();
    private final LocalAuthority oldThirdPartyOrgNoEmail = LocalAuthority.builder().id("ABC123").build();
    private final LocalAuthority newThirdPartyOrgWithEmail = LocalAuthority.builder().id("DEF456")
        .email(NEW_EMAIL).build();
    private final LocalAuthority newThirdPartyOrgNoEmail = LocalAuthority.builder().id("DEF456").build();

    @Mock
    private NotificationService notificationService;

    @Mock
    private NoticeOfChangeContentProvider noticeOfChangeContentProvider;

    @Mock
    private OrganisationService organisationService;

    @InjectMocks
    private NoticeOfChangeThirdPartyEventHandler underTest;

    @Test
    void shouldSendEmailToSolicitorAccessGranted() {
        given(caseData.getId()).willReturn(CASE_ID);
        given(noticeOfChangeContentProvider.buildNoticeOfChangeThirdPartySolicitorTemplate(caseData))
            .willReturn(EXPECTED_TEMPLATE);

        underTest.notifyThirdPartySolicitorAccessGranted(new NoticeOfChangeThirdPartyEvent(oldThirdPartyOrgWithEmail,
            newThirdPartyOrgWithEmail, caseData));

        verify(notificationService).sendEmail(
            NOTICE_OF_CHANGE_NEW_REPRESENTATIVE,
            NEW_EMAIL,
            EXPECTED_TEMPLATE,
            CASE_ID
        );
    }

    @Test
    void shouldSendEmailToSolicitorAccessRemoved() {
        given(caseData.getId()).willReturn(CASE_ID);
        given(noticeOfChangeContentProvider.buildNoticeOfChangeThirdPartySolicitorTemplate(caseData))
            .willReturn(EXPECTED_TEMPLATE);

        underTest.notifyThirdPartySolicitorAccessRemoved(new NoticeOfChangeThirdPartyEvent(oldThirdPartyOrgWithEmail,
            newThirdPartyOrgWithEmail, caseData));

        verify(notificationService).sendEmail(
            NOTICE_OF_CHANGE_FORMER_REPRESENTATIVE,
            OLD_EMAIL,
            EXPECTED_TEMPLATE,
            CASE_ID
        );
    }

    @Test
    void shouldSendEmailToSolicitorAccessGrantedNoEmail() {
        given(caseData.getId()).willReturn(CASE_ID);
        given(noticeOfChangeContentProvider.buildNoticeOfChangeThirdPartySolicitorTemplate(caseData))
            .willReturn(EXPECTED_TEMPLATE);
        given(organisationService.findOrganisation(newThirdPartyOrgNoEmail.getId())).willReturn(
            Optional.ofNullable(Organisation.builder()
                .superUser(SuperUser.builder()
                    .email(NEW_EMAIL)
                    .build()).build()));

        underTest.notifyThirdPartySolicitorAccessGranted(new NoticeOfChangeThirdPartyEvent(oldThirdPartyOrgWithEmail,
            newThirdPartyOrgNoEmail, caseData));

        verify(notificationService).sendEmail(
            NOTICE_OF_CHANGE_NEW_REPRESENTATIVE,
            NEW_EMAIL,
            EXPECTED_TEMPLATE,
            CASE_ID
        );
    }

    @Test
    void shouldSendEmailToSolicitorAccessRemovedNoEmail() {
        given(caseData.getId()).willReturn(CASE_ID);
        given(noticeOfChangeContentProvider.buildNoticeOfChangeThirdPartySolicitorTemplate(caseData))
            .willReturn(EXPECTED_TEMPLATE);
        given(organisationService.findOrganisation(oldThirdPartyOrgNoEmail.getId())).willReturn(
            Optional.ofNullable(Organisation.builder()
                .superUser(SuperUser.builder()
                        .email(OLD_EMAIL)
                        .build()).build()));

        underTest.notifyThirdPartySolicitorAccessRemoved(new NoticeOfChangeThirdPartyEvent(oldThirdPartyOrgNoEmail,
            newThirdPartyOrgWithEmail, caseData));

        verify(notificationService).sendEmail(
            NOTICE_OF_CHANGE_FORMER_REPRESENTATIVE,
            OLD_EMAIL,
            EXPECTED_TEMPLATE,
            CASE_ID
        );
    }
}
