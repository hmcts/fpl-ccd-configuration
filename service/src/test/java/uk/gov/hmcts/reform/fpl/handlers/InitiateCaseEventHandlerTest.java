package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.fpl.events.InitiatedCaseEvent;
import uk.gov.hmcts.reform.fpl.service.CaseRepository;
import uk.gov.hmcts.reform.fpl.service.UserService;

import java.io.IOException;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;

@ExtendWith(SpringExtension.class)
class InitiateCaseEventHandlerTest {

    private static final String AUTH_TOKEN = "Bearer token";
    private static final String USER_ID = "1";

    @Mock
    private UserService userService;
    @Mock
    private CaseRepository caseRepository;

    @InjectMocks
    private InitiatedCaseEventHandler initiateCaseEventHandler;

    @Test
    void shouldUpdateCaseWithLocalAuthority() throws IOException {
        CallbackRequest request = callbackRequest();
        String domain = "example";
        String caseId = "12345";

        given(userService.extractUserDomainName(AUTH_TOKEN)).willReturn(domain);

        initiateCaseEventHandler.handleCaseInitiation(new InitiatedCaseEvent(request, AUTH_TOKEN, USER_ID));

        verify(caseRepository).setCaseLocalAuthority(AUTH_TOKEN, USER_ID, caseId, domain);
    }
}
