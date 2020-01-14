package uk.gov.hmcts.reform.fpl.service.ccd;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.fpl.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseDetails;

@ExtendWith(MockitoExtension.class)
class CoreCaseDataServiceTest {
    private String userAuthToken = "Bearer user-xyz";
    private String serviceAuthToken = "Bearer service-xyz";
    private long caseId = 1L;

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

    @BeforeEach
    void setup() {
        when(idamClient.authenticateUser(userConfig.getUserName(), userConfig.getPassword())).thenReturn(userAuthToken);
        when(authTokenGenerator.generate()).thenReturn(serviceAuthToken);
    }

    @Test
    void shouldMakeAppropriateApiCalls() {
        String userId = "u1-xyz";
        String eventId = "sample-event";
        String eventToken = "t-xyz";

        when(idamClient.getUserDetails(userAuthToken)).thenReturn(UserDetails.builder().id(userId).build());

        when(coreCaseDataApi.startEventForCaseWorker(userAuthToken, serviceAuthToken, userId, JURISDICTION,
            CASE_TYPE, Long.toString(caseId), eventId)).thenReturn(buildStartEventResponse(eventId, eventToken));

        service.triggerEvent(JURISDICTION, CASE_TYPE, caseId, eventId);

        verify(coreCaseDataApi).startEventForCaseWorker(userAuthToken, serviceAuthToken, userId,
            JURISDICTION, CASE_TYPE, Long.toString(caseId), eventId);
        verify(coreCaseDataApi).submitEventForCaseWorker(userAuthToken, serviceAuthToken, userId, JURISDICTION,
            CASE_TYPE, Long.toString(caseId), true, buildCaseDataContent(eventId, eventToken));
    }

    @Test
    void shouldReturnMatchingCaseDetailsIdWhenSearchedByExistingCaseId() throws IOException {
        CaseDetails expectedCaseDetails = populatedCaseDetails();

        when(coreCaseDataApi.getCase(userAuthToken, serviceAuthToken, Long.toString(caseId)))
            .thenReturn(expectedCaseDetails);

        CaseDetails returnedCaseDetails = service.findCaseDetailsById(Long.toString(caseId));

        assertThat(expectedCaseDetails.getId())
            .isEqualTo(returnedCaseDetails.getId());
    }

    @Test
    void shouldReturnNullCaseDetailsIdWhenSearchedByNonExistingCaseId() {
        CaseDetails returnedCaseDetails = service.findCaseDetailsById("111111111111");

        assertThat(returnedCaseDetails).isNull();
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
