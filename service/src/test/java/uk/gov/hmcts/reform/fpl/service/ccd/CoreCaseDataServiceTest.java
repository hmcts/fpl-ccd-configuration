package uk.gov.hmcts.reform.fpl.service.ccd;

import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.fpl.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyMap;
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

    @Mock
    private RequestData requestData;

    private CoreCaseDataService service;

    @BeforeEach
    void setup() {
        service = new CoreCaseDataService(userConfig, authTokenGenerator, idamClient, coreCaseDataApi, requestData);

        when(authTokenGenerator.generate()).thenReturn(serviceAuthToken);
    }

    @Nested
    class StartAndSubmitEvent {
        String eventId = "sample-event";
        String eventToken = "t-xyz";
        String userId = "u1-xyz";

        @BeforeEach
        void setUp() {
            when(idamClient.getUserInfo(userAuthToken))
                .thenReturn(UserInfo.builder().uid(userId).build());
            when(idamClient.getAccessToken(userConfig.getUserName(), userConfig.getPassword()))
                .thenReturn(userAuthToken);

            when(coreCaseDataApi.startEventForCaseWorker(userAuthToken, serviceAuthToken, userId, JURISDICTION,
                CASE_TYPE, Long.toString(caseId), eventId))
                .thenReturn(buildStartEventResponse(eventId, eventToken));
        }

        @Test
        void shouldStartAndSubmitEventWithoutEventData() {
            service.triggerEvent(JURISDICTION, CASE_TYPE, caseId, eventId);

            verify(coreCaseDataApi).startEventForCaseWorker(userAuthToken, serviceAuthToken, userId,
                JURISDICTION, CASE_TYPE, Long.toString(caseId), eventId);
            verify(coreCaseDataApi).submitEventForCaseWorker(userAuthToken, serviceAuthToken, userId, JURISDICTION,
                CASE_TYPE, Long.toString(caseId), true,
                buildCaseDataContent(eventId, eventToken, emptyMap()));
        }

        @Test
        void shouldStartAndSubmitEventWithEventData() {
            Map<String, Object> eventData = Map.of("A", "B");
            service.triggerEvent(JURISDICTION, CASE_TYPE, caseId, eventId, eventData);

            verify(coreCaseDataApi).startEventForCaseWorker(userAuthToken, serviceAuthToken, userId,
                JURISDICTION, CASE_TYPE, Long.toString(caseId), eventId);
            verify(coreCaseDataApi).submitEventForCaseWorker(userAuthToken, serviceAuthToken, userId, JURISDICTION,
                CASE_TYPE, Long.toString(caseId), true,
                buildCaseDataContent(eventId, eventToken, eventData));
        }
    }

    @Test
    void shouldReturnMatchingCaseDetailsIdWhenSearchedByExistingCaseId() {
        CaseDetails expectedCaseDetails = populatedCaseDetails();

        when(requestData.authorisation()).thenReturn(userAuthToken);

        when(coreCaseDataApi.getCase(userAuthToken, serviceAuthToken, Long.toString(caseId)))
            .thenReturn(expectedCaseDetails);

        CaseDetails returnedCaseDetails = service.findCaseDetailsById(Long.toString(caseId));

        assertThat(expectedCaseDetails)
            .isEqualTo(returnedCaseDetails);
    }

    @Test
    void shouldReturnNullCaseDetailsIdWhenSearchedByNonExistingCaseId() {
        when(requestData.authorisation()).thenReturn(userAuthToken);

        CaseDetails returnedCaseDetails = service.findCaseDetailsById("111111111111");

        assertThat(returnedCaseDetails).isNull();
    }

    @Test
    void shouldSearchCasesAsSystemUpdateUser() {
        String query = "query";
        String caseType = "caseType";

        List<CaseDetails> cases = List.of(CaseDetails.builder().id(RandomUtils.nextLong()).build());
        SearchResult searchResult = SearchResult.builder().cases(cases).build();

        when(idamClient.getAccessToken(userConfig.getUserName(), userConfig.getPassword())).thenReturn(userAuthToken);
        when(coreCaseDataApi.searchCases(userAuthToken, serviceAuthToken, caseType, query)).thenReturn(searchResult);

        List<CaseDetails> casesFound = service.searchCases(caseType, query);

        assertThat(casesFound).isEqualTo(cases);
        verify(coreCaseDataApi).searchCases(userAuthToken, serviceAuthToken, caseType, query);
        verify(idamClient).getAccessToken(userConfig.getUserName(), userConfig.getPassword());
    }

    private StartEventResponse buildStartEventResponse(String eventId, String eventToken) {
        return StartEventResponse.builder().eventId(eventId).token(eventToken).build();
    }

    private CaseDataContent buildCaseDataContent(String eventId, String eventToken, Object eventData) {
        return CaseDataContent.builder()
            .eventToken(eventToken)
            .event(Event.builder()
                .id(eventId)
                .build())
            .data(eventData)
            .build();
    }
}
