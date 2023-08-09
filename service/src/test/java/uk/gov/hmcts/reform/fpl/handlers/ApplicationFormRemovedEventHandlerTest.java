package uk.gov.hmcts.reform.fpl.handlers;

import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.config.CtscTeamLeadLookupConfiguration;
import uk.gov.hmcts.reform.fpl.events.ApplicationFormRemovedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.ApplicationFormRemovedNotifyData;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.ApplicationFormRemovedEmailContentProvider;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.APPLICATION_FORM_REMOVED_CTSC_LEAD_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_CODE;

@ExtendWith(SpringExtension.class)
class ApplicationFormRemovedEventHandlerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private ApplicationFormRemovedEmailContentProvider contentProvider;

    @Mock
    private CtscTeamLeadLookupConfiguration ctscTeamLeadLookupConfiguration;

    @InjectMocks
    private ApplicationFormRemovedEventHandler applicationFormRemovedEventHandler;

    @Test
    void shouldSendEmailToCTSCTeamLead() {
        final String expectedEmail = "test@test.com";
        final CaseData caseData = CaseData.builder()
            .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
            .id(RandomUtils.nextLong())
            .build();
        final ApplicationFormRemovedNotifyData notifyData = ApplicationFormRemovedNotifyData.builder().build();
        final ApplicationFormRemovedEvent event = new ApplicationFormRemovedEvent(caseData);

        when(ctscTeamLeadLookupConfiguration.getEmail())
            .thenReturn(expectedEmail);
        when(contentProvider.getNotifyData(caseData))
            .thenReturn(notifyData);

        applicationFormRemovedEventHandler.notifyTeamLead(event);

        verify(notificationService).sendEmail(
            APPLICATION_FORM_REMOVED_CTSC_LEAD_TEMPLATE,
            expectedEmail,
            notifyData,
            caseData.getId());

        verify(ctscTeamLeadLookupConfiguration).getEmail();
        verifyNoMoreInteractions(notificationService);
    }
}
