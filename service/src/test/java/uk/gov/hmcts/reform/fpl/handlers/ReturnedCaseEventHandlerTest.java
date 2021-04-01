package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.events.ReturnedCaseEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.returnedcase.ReturnedCaseTemplate;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.ReturnedCaseContentProvider;

import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.APPLICATION_RETURNED_TO_THE_LA;
import static uk.gov.hmcts.reform.fpl.model.notify.LocalAuthorityInboxRecipientsRequest.builder;

@ExtendWith(MockitoExtension.class)
class ReturnedCaseEventHandlerTest {

    private static final long CASE_ID = 12335L;
    private static final CaseData CASE_DATA = CaseData.builder().id(CASE_ID).build();
    private static final Set<String> RECIPIENTS = Set.of("email1","email2");
    private static final ReturnedCaseTemplate PARAMETERS = mock(ReturnedCaseTemplate.class);

    @Mock
    private NotificationService notificationService;
    @Mock
    private InboxLookupService inboxLookupService;
    @Mock
    private ReturnedCaseContentProvider returnedCaseContentProvider;

    @InjectMocks
    private ReturnedCaseEventHandler underTest;

    @Test
    void testNotifyLocalAuthority() {

        when(returnedCaseContentProvider.parametersWithCaseUrl(CASE_DATA)).thenReturn(PARAMETERS);
        when(inboxLookupService.getRecipients(
            builder().caseData(CASE_DATA)
                .excludeLegalRepresentatives(true)
                .build())
        ).thenReturn(RECIPIENTS);

        underTest.notifyLocalAuthority(new ReturnedCaseEvent(CASE_DATA));

        verify(notificationService).sendEmail(APPLICATION_RETURNED_TO_THE_LA,
            RECIPIENTS,
            PARAMETERS,
            CASE_DATA.getId().toString());
    }
}
