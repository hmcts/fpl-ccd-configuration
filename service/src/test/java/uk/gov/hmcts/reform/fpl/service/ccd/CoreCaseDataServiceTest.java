package uk.gov.hmcts.reform.fpl.service.ccd;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
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

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CoreCaseDataService.class})
class CoreCaseDataServiceTest {
    @MockBean
    private SystemUpdateUserConfiguration userConfig;
    @MockBean
    private AuthTokenGenerator authTokenGenerator;
    @MockBean
    private IdamClient idamClient;
    @MockBean
    private CoreCaseDataApi coreCaseDataApi;

    @Autowired
    private CoreCaseDataService service;

    @Test
    void shouldMakeAppropriateApiCalls() {
        String userId = "u1-xyz";
        String userAuthenticationToken = "Bearer user-xyz";
        String serviceAuthenticationToken = "Bearer service-xyz";
        long caseId = 1L;
        String eventId = "sample-event";
        String eventToken = "t-xyz";

        when(idamClient.authenticateUser(userConfig.getUserName(), userConfig.getPassword())).thenReturn(userAuthenticationToken);
        when(idamClient.getUserDetails(userAuthenticationToken)).thenReturn(UserDetails.builder().id(userId).build());
        when(authTokenGenerator.generate()).thenReturn(serviceAuthenticationToken);
        when(coreCaseDataApi.startEventForCaseWorker(userAuthenticationToken, serviceAuthenticationToken, userId,
            JURISDICTION, CASE_TYPE, Long.toString(caseId), eventId)).thenReturn(buildStartEventResponse(eventId, eventToken));

        service.triggerEvent(JURISDICTION, CASE_TYPE, caseId, eventId);

        verify(coreCaseDataApi).startEventForCaseWorker(userAuthenticationToken, serviceAuthenticationToken, userId,
            JURISDICTION, CASE_TYPE, Long.toString(caseId), eventId);
        verify(coreCaseDataApi).submitEventForCaseWorker(userAuthenticationToken, serviceAuthenticationToken, userId,
            JURISDICTION, CASE_TYPE, Long.toString(caseId), true, buildCaseDataContent(eventId, eventToken));
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