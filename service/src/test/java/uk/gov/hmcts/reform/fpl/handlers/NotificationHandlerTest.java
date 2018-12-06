package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.events.SubmittedCaseEvent;
import uk.gov.hmcts.reform.fpl.service.HmctsCourtLookUpService;
import uk.gov.hmcts.reform.fpl.service.LocalAuthorityService;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;

@ExtendWith(SpringExtension.class)
class NotificationHandlerTest {

    private static final String LOCAL_AUTHORITY_CODE = "example";
    private static final String COURT_EMAIL_ADDRESS = "FamilyPublicLaw+test@gmail.com";
    private static final String COURT_NAME = "Test court";
    private static final String AUTH_TOKEN = "Bearer token";
    private static final String USER_ID = "1";

    @Mock
    private HmctsCourtLookUpService hmctsCourtLookUpService;

    @Mock
    private LocalAuthorityService localAuthorityService;

    @Mock
    private NotificationClient notificationClient;

    @InjectMocks
    private NotificationHandler notificationHandler;

    @Test
    void shouldSendEmail() throws IOException, NotificationClientException {
        CallbackRequest request = callbackRequest();

        given(hmctsCourtLookUpService.getCourt(LOCAL_AUTHORITY_CODE))
            .willReturn(new HmctsCourtLookupConfiguration.Court(COURT_NAME, COURT_EMAIL_ADDRESS));

        given(localAuthorityService.getLocalAuthorityName(LOCAL_AUTHORITY_CODE))
            .willReturn("Example Local Authority");

        notificationHandler.sendNotificationToHmctsAdmin(new SubmittedCaseEvent(request, AUTH_TOKEN, USER_ID));

        verify(notificationClient, times(1)).sendEmail(any(), any(), any(), any());
    }
}
