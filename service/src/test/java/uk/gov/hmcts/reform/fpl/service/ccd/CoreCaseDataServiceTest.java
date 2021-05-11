package uk.gov.hmcts.reform.fpl.service.ccd;

import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.SystemUserService;

import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.quality.Strictness.LENIENT;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseDetails;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
class CoreCaseDataServiceTest {
    private static final String USER_AUTH_TOKEN = "Bearer user-xyz";
    private static final String SERVICE_AUTH_TOKEN = "Bearer service-xyz";
    private static final long CASE_ID = 1L;

    @Mock
    private SystemUserService systemUserService;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private CoreCaseDataApi coreCaseDataApi;
    @Mock
    private RequestData requestData;

    @Spy
    @InjectMocks
    private CoreCaseDataService service;

    @BeforeEach
    void setup() {
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTH_TOKEN);
    }

    @Nested
    class StartAndSubmitEvent {
        String eventId = "sample-event";
        String eventToken = "t-xyz";
        String userId = "u1-xyz";

        @BeforeEach
        void setUp() {
            when(systemUserService.getUserId(USER_AUTH_TOKEN)).thenReturn(userId);
            when(systemUserService.getSysUserToken()).thenReturn(USER_AUTH_TOKEN);

            when(coreCaseDataApi.startEventForCaseWorker(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, userId, JURISDICTION,
                CASE_TYPE, Long.toString(CASE_ID), eventId))
                .thenReturn(buildStartEventResponse(eventId, eventToken));
        }

        @Test
        void shouldStartAndSubmitEventWithoutEventData() {
            service.triggerEvent(JURISDICTION, CASE_TYPE, CASE_ID, eventId);

            verify(coreCaseDataApi).startEventForCaseWorker(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, userId,
                JURISDICTION, CASE_TYPE, Long.toString(CASE_ID), eventId);
            verify(coreCaseDataApi).submitEventForCaseWorker(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, userId, JURISDICTION,
                CASE_TYPE, Long.toString(CASE_ID), true,
                buildCaseDataContent(eventId, eventToken, emptyMap()));
        }

        @Test
        void shouldStartAndSubmitEventWithEventData() {
            Map<String, Object> eventData = Map.of("A", "B");
            service.triggerEvent(JURISDICTION, CASE_TYPE, CASE_ID, eventId, eventData);

            verify(coreCaseDataApi).startEventForCaseWorker(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, userId,
                JURISDICTION, CASE_TYPE, Long.toString(CASE_ID), eventId);
            verify(coreCaseDataApi).submitEventForCaseWorker(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, userId, JURISDICTION,
                CASE_TYPE, Long.toString(CASE_ID), true,
                buildCaseDataContent(eventId, eventToken, eventData));
        }
    }

    @Test
    void shouldTriggerUpdateCaseEventWhenCaseIsRequestedToBeUpdated() {
        final Map<String, Object> caseUpdate = Map.of("a", "b");

        doNothing().when(service).triggerEvent(any(), any(), any(), any(), any());

        service.updateCase(CASE_ID, caseUpdate);

        verify(service).triggerEvent(JURISDICTION, CASE_TYPE, CASE_ID, "internal-change-UPDATE_CASE", caseUpdate);
    }

    @Test
    void shouldReturnMatchingCaseDetailsIdWhenSearchedByExistingCaseId() {
        CaseDetails expectedCaseDetails = populatedCaseDetails();

        when(requestData.authorisation()).thenReturn(USER_AUTH_TOKEN);

        when(coreCaseDataApi.getCase(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, Long.toString(CASE_ID)))
            .thenReturn(expectedCaseDetails);

        CaseDetails returnedCaseDetails = service.findCaseDetailsById(Long.toString(CASE_ID));

        assertThat(expectedCaseDetails)
            .isEqualTo(returnedCaseDetails);
    }

    @Test
    void shouldReturnNullCaseDetailsIdWhenSearchedByNonExistingCaseId() {
        when(requestData.authorisation()).thenReturn(USER_AUTH_TOKEN);

        CaseDetails returnedCaseDetails = service.findCaseDetailsById("111111111111");

        assertThat(returnedCaseDetails).isNull();
    }

    @Test
    void shouldSearchCasesAsSystemUpdateUser() {
        String query = "query";
        String caseType = "caseType";

        List<CaseDetails> cases = List.of(CaseDetails.builder().id(RandomUtils.nextLong()).build());
        SearchResult searchResult = SearchResult.builder().cases(cases).build();

        when(systemUserService.getSysUserToken()).thenReturn(USER_AUTH_TOKEN);

        when(coreCaseDataApi.searchCases(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, caseType, query))
            .thenReturn(searchResult);

        SearchResult returnedSearchResult = service.searchCases(caseType, query);

        assertThat(returnedSearchResult).isEqualTo(searchResult);
        verify(coreCaseDataApi).searchCases(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, caseType, query);
        verify(systemUserService).getSysUserToken();
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
