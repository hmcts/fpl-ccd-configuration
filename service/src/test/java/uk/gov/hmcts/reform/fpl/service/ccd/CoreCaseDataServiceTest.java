package uk.gov.hmcts.reform.fpl.service.ccd;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.fpl.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;

@ExtendWith(MockitoExtension.class)
class CoreCaseDataServiceTest {
    @Mock
    private SystemUpdateUserConfiguration userConfig;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private IdamClient idamClient;
    @Mock
    private CoreCaseDataApi coreCaseDataApi;

    @InjectMocks
    private CoreCaseDataService service;

    @Test
    void shouldMakeAppropriateApiCalls() {
        String userId = "u1-xyz";
        String userAuthToken = "Bearer user-xyz";
        String serviceAuthToken = "Bearer service-xyz";
        long caseId = 1L;
        String eventId = "sample-event";
        String eventToken = "t-xyz";

        when(idamClient.authenticateUser(userConfig.getUserName(), userConfig.getPassword())).thenReturn(userAuthToken);
        when(idamClient.getUserDetails(userAuthToken)).thenReturn(UserDetails.builder().id(userId).build());
        when(authTokenGenerator.generate()).thenReturn(serviceAuthToken);
        when(coreCaseDataApi.startEventForCaseWorker(userAuthToken, serviceAuthToken, userId, JURISDICTION,
            CASE_TYPE, Long.toString(caseId), eventId)).thenReturn(buildStartEventResponse(eventId, eventToken));

        service.triggerEvent(JURISDICTION, CASE_TYPE, caseId, eventId);

        verify(coreCaseDataApi).startEventForCaseWorker(userAuthToken, serviceAuthToken, userId,
            JURISDICTION, CASE_TYPE, Long.toString(caseId), eventId);
        verify(coreCaseDataApi).submitEventForCaseWorker(userAuthToken, serviceAuthToken, userId, JURISDICTION,
            CASE_TYPE, Long.toString(caseId), true, buildCaseDataContent(eventId, eventToken));
    }

    private StartEventResponse buildStartEventResponse(String eventId, String eventToken) {
        return StartEventResponse.builder().eventId(eventId).token(eventToken).build();
    }

    private CaseDataContent buildCaseDataContent(String eventId, String eventToken) {
        return CaseDataContent.builder()
            .eventToken(eventToken)
            .event(Event.builder()
                .id(eventId)
                .build())
            .build();
    }
}
