package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.events.RespondQueryEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.RespondQueryNotifyData;
import uk.gov.hmcts.reform.fpl.service.UserService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.RespondQueryContentProvider;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.QUERY_RESPONDED;

public class RespondQueryEventHandlerTest {
    private static final CaseData CASE_DATA = CaseData.builder().id(1L).caseName("test").build();
    private static final String USER_ID = UUID.randomUUID().toString();
    private static final String QUERY_DATE = "2025-06-01";
    private static final RespondQueryEvent EVENT = RespondQueryEvent.builder()
        .caseData(CASE_DATA)
        .userId(USER_ID)
        .queryDate(QUERY_DATE)
        .build();
    private static final RespondQueryNotifyData NOTIFY_DATA = RespondQueryNotifyData.builder()
        .caseId(CASE_DATA.getId().toString())
        .caseName(CASE_DATA.getCaseName())
        .caseUrl("caseUrl")
        .queryDate(QUERY_DATE)
        .build();
    private static final String RECIPIENT = "recipient@test.com";

    private final RespondQueryContentProvider respondQueryContentProvider = mock(RespondQueryContentProvider.class);
    private final NotificationService notificationService = mock(NotificationService.class);
    private final UserService userService = mock(UserService.class);

    private final RespondQueryEventHandler underTest =
        new RespondQueryEventHandler(notificationService, respondQueryContentProvider, userService);

    @Test
    void shouldNotifyUser() {
        when(respondQueryContentProvider.getRespondQueryNotifyData(CASE_DATA, QUERY_DATE)).thenReturn(NOTIFY_DATA);
        when(userService.getUserDetailsById(any())).thenReturn(UserDetails.builder().email(RECIPIENT).build());

        underTest.notifyUser(EVENT);

        verify(notificationService).sendEmail(QUERY_RESPONDED, RECIPIENT, NOTIFY_DATA, CASE_DATA.getId());
        verifyNoMoreInteractions(notificationService);
    }
}
