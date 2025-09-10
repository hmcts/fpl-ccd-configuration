package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.events.ApplicantsDetailsUpdatedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.ApplicantsDetailsUpdatedNotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.RecipientsRequest;
import uk.gov.hmcts.reform.fpl.service.LocalAuthorityRecipientsService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.ApplicantsDetailsUpdatedContentProvider;

import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.APPLICANTS_DETAILS_UPDATED;

class ApplicantsDetailsUpdatedEventHandlerTest {
    private static final CaseData CASE_DATA = CaseData.builder().id(1L).build();
    private static final ApplicantsDetailsUpdatedEvent EVENT = new ApplicantsDetailsUpdatedEvent(CASE_DATA);
    private static final ApplicantsDetailsUpdatedNotifyData NOTIFY_DATA = ApplicantsDetailsUpdatedNotifyData.builder()
        .firstRespondentLastNameOrLaName("firstRespondentLastName")
        .caseUrl("caseUrl")
        .familyManCaseNumber("familyManCaseNumber")
        .build();
    private static final Set<String> RECIPIENTS = Set.of("recipient@test.com");

    private final ApplicantsDetailsUpdatedContentProvider contentProvider =
        mock(ApplicantsDetailsUpdatedContentProvider.class);
    private final LocalAuthorityRecipientsService localAuthorityRecipients =
        mock(LocalAuthorityRecipientsService.class);
    private final NotificationService notificationService = mock(NotificationService.class);

    private final ApplicantsDetailsUpdatedEventHandler underTest =
        new ApplicantsDetailsUpdatedEventHandler(contentProvider, localAuthorityRecipients, notificationService);

    @BeforeEach
    void setUp() {
        when(contentProvider.getApplicantsDetailsUpdatedNotifyData(CASE_DATA)).thenReturn(NOTIFY_DATA);
    }

    @Test
    void shouldNotifyLocalAuthorities() {
        when(localAuthorityRecipients.getRecipients(RecipientsRequest.builder()
            .caseData(CASE_DATA)
            .build()))
            .thenReturn(RECIPIENTS);

        underTest.notifyLocalAuthorities(EVENT);

        verify(notificationService).sendEmail(APPLICANTS_DETAILS_UPDATED, RECIPIENTS, NOTIFY_DATA, 1L);
        verifyNoMoreInteractions(notificationService);
    }
}
