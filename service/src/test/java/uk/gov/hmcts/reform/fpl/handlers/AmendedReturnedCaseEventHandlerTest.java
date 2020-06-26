package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.events.AmendedReturnedCaseEvent;
import uk.gov.hmcts.reform.fpl.model.event.EventData;
import uk.gov.hmcts.reform.fpl.model.notify.returnedcase.ReturnedCaseTemplate;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.ReturnedCaseContentProvider;

import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.AMENDED_APPLICATION_RETURNED_ADMIN_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.AMENDED_APPLICATION_RETURNED_CAFCASS_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration.Cafcass;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;
import static uk.gov.hmcts.reform.fpl.utils.assertions.AnnotationAssertion.assertClass;

@ExtendWith(SpringExtension.class)
class AmendedReturnedCaseEventHandlerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private ReturnedCaseContentProvider returnedCaseContentProvider;

    @Mock
    private HmctsAdminNotificationHandler adminNotificationHandler;

    @Mock
    private CafcassLookupConfiguration cafcassLookupConfiguration;

    @InjectMocks
    private AmendedReturnedCaseEventHandler amendedReturnedCaseEventHandler;

    @Test
    void shouldExecuteAsynchronously() {
        assertClass(AmendedReturnedCaseEventHandler.class).hasAsyncMethods("notifyAdmin", "notifyCafcass");
    }

    @Test
    void shouldSendEmailToCourtAdmin() {
        final String expectedEmail = "test@test.com";
        final CallbackRequest request = callbackRequest();
        final ReturnedCaseTemplate expectedTemplate = ReturnedCaseTemplate.builder().build();
        final AmendedReturnedCaseEvent amendedReturnedCaseEvent = new AmendedReturnedCaseEvent(request);

        when(adminNotificationHandler.getHmctsAdminEmail(new EventData(amendedReturnedCaseEvent)))
            .thenReturn(expectedEmail);
        when(returnedCaseContentProvider.parametersWithCaseUrl(request.getCaseDetails()))
            .thenReturn(expectedTemplate);

        amendedReturnedCaseEventHandler.notifyAdmin(amendedReturnedCaseEvent);

        verify(notificationService).sendEmail(
            AMENDED_APPLICATION_RETURNED_ADMIN_TEMPLATE,
            expectedEmail,
            expectedTemplate,
            request.getCaseDetails().getId().toString());

        verify(returnedCaseContentProvider).parametersWithCaseUrl(request.getCaseDetails());
        verify(adminNotificationHandler).getHmctsAdminEmail(new EventData(amendedReturnedCaseEvent));
    }

    @Test
    void shouldSendEmailToCafcass() {
        final String expectedEmail = "test@test.com";
        final String localAuthority = "LA1";
        final CallbackRequest request = callbackRequest(Map.of("localAuthority", localAuthority));
        final ReturnedCaseTemplate expectedTemplate = ReturnedCaseTemplate.builder().build();
        final AmendedReturnedCaseEvent amendedReturnedCaseEvent = new AmendedReturnedCaseEvent(request);

        when(cafcassLookupConfiguration.getCafcass(localAuthority))
            .thenReturn(new Cafcass("Swansea", expectedEmail));
        when(returnedCaseContentProvider.parametersWithApplicationLink(request.getCaseDetails()))
            .thenReturn(expectedTemplate);

        amendedReturnedCaseEventHandler.notifyCafcass(amendedReturnedCaseEvent);

        verify(notificationService).sendEmail(
            AMENDED_APPLICATION_RETURNED_CAFCASS_TEMPLATE,
            expectedEmail,
            expectedTemplate,
            request.getCaseDetails().getId().toString());

        verify(returnedCaseContentProvider).parametersWithApplicationLink(request.getCaseDetails());
        verify(cafcassLookupConfiguration).getCafcass(localAuthority);
    }

    @AfterEach
    void verifyNoUnwantedInteractions() {
        verifyNoMoreInteractions(notificationService, returnedCaseContentProvider);
    }
}
