package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.events.UpdateGuardianEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.RecipientsRequest;
import uk.gov.hmcts.reform.fpl.model.notify.UpdateGuardianNotifyData;
import uk.gov.hmcts.reform.fpl.service.LocalAuthorityRecipientsService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.RepresentativesInbox;
import uk.gov.hmcts.reform.fpl.service.email.content.UpdateGuardianContentProvider;

import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.GUARDIAN_UPDATED;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;

public class UpdateGuardiansEventHandlerTest {
    private static final CaseData CASE_DATA = CaseData.builder().id(1L).build();
    private static final UpdateGuardianEvent EVENT = UpdateGuardianEvent.builder().caseData(CASE_DATA).build();
    private static final UpdateGuardianNotifyData NOTIFY_DATA = UpdateGuardianNotifyData.builder()
        .firstRespondentLastName("firstRespondentLastName")
        .caseUrl("caseUrl")
        .familyManCaseNumber("familyManCaseNumber")
        .build();
    private static final Set<String> RECIPIENTS = Set.of("recipient@test.com");

    private final UpdateGuardianContentProvider updateGuardianContentProvider =
        mock(UpdateGuardianContentProvider.class);
    private final LocalAuthorityRecipientsService localAuthorityRecipients =
        mock(LocalAuthorityRecipientsService.class);
    private final RepresentativesInbox representativesInbox = mock(RepresentativesInbox.class);
    private final NotificationService notificationService = mock(NotificationService.class);

    private final UpdateGuardiansEventHandler underTest =
        new UpdateGuardiansEventHandler(updateGuardianContentProvider, localAuthorityRecipients, representativesInbox,
            notificationService);

    @BeforeEach
    void setUp() {
        when(updateGuardianContentProvider.getUpdateGuardianNotifyData(CASE_DATA)).thenReturn(NOTIFY_DATA);
    }

    @Test
    void shouldNotifyLocalAuthorities() {
        when(localAuthorityRecipients.getRecipients(RecipientsRequest.builder()
            .caseData(CASE_DATA)
            .secondaryLocalAuthorityExcluded(true)
            .legalRepresentativesExcluded(true)
            .build()))
            .thenReturn(RECIPIENTS);

        underTest.notifyLocalAuthorities(EVENT);

        verify(notificationService).sendEmail(GUARDIAN_UPDATED, RECIPIENTS, NOTIFY_DATA, 1L);
        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void shouldNotifyRespondentSolicitors() {
        when(representativesInbox.getRespondentSolicitorEmails(CASE_DATA, DIGITAL_SERVICE)).thenReturn(RECIPIENTS);

        underTest.notifyRespondentSolicitors(EVENT);

        verify(notificationService).sendEmail(GUARDIAN_UPDATED, RECIPIENTS, NOTIFY_DATA, 1L);
        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void shouldNotifyChildSolicitors() {
        when(representativesInbox.getChildrenSolicitorEmails(CASE_DATA, DIGITAL_SERVICE)).thenReturn(RECIPIENTS);

        underTest.notifyChildSolicitors(EVENT);

        verify(notificationService).sendEmail(GUARDIAN_UPDATED, RECIPIENTS, NOTIFY_DATA, 1L);
        verifyNoMoreInteractions(notificationService);
    }
}
