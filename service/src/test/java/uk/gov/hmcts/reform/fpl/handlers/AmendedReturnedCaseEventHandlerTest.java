package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.fpl.events.AmendedReturnedCaseEvent;
import uk.gov.hmcts.reform.fpl.model.event.EventData;
import uk.gov.hmcts.reform.fpl.model.notify.returnedcase.ReturnedCaseTemplate;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.ReturnedCaseContentProvider;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.AMENDED_APPLICATION_RETURNED_TO_THE_ADMIN;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_CODE;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;
import static uk.gov.hmcts.reform.fpl.utils.assertions.AnnotationAssertion.assertClass;

@ExtendWith(SpringExtension.class)
class AmendedReturnedCaseEventHandlerTest {

    @Mock
    private RequestData requestData;

    @Mock
    private NotificationService notificationService;

    @Mock
    private ReturnedCaseContentProvider returnedCaseContentProvider;

    @Mock
    private HmctsAdminNotificationHandler adminNotificationHandler;

    @InjectMocks
    private AmendedReturnedCaseEventHandler amendedReturnedCaseEventHandler;

    @Test
    void shouldExecuteAsynchronously() {
        assertClass(AmendedReturnedCaseEventHandler.class).hasAsyncMethods("notifyAdmin");
    }

    @Test
    void shouldSendEmailToCourtAdmin() {
        final String expectedEmail = "test@test.com";
        final CallbackRequest request = callbackRequest();
        final ReturnedCaseTemplate expectedTemplate = new ReturnedCaseTemplate();
        final AmendedReturnedCaseEvent amendedReturnedCaseEvent = new AmendedReturnedCaseEvent(request, requestData);

        when(adminNotificationHandler.getHmctsAdminEmail(new EventData(amendedReturnedCaseEvent)))
            .thenReturn(expectedEmail);
        when(returnedCaseContentProvider.buildNotificationParameters(request.getCaseDetails(), LOCAL_AUTHORITY_CODE))
            .thenReturn(expectedTemplate);

        amendedReturnedCaseEventHandler.notifyAdmin(amendedReturnedCaseEvent);

        verify(notificationService).sendEmail(
            AMENDED_APPLICATION_RETURNED_TO_THE_ADMIN,
            expectedEmail,
            expectedTemplate,
            request.getCaseDetails().getId().toString());
    }
}
