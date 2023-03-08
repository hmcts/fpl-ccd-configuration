package uk.gov.hmcts.reform.fpl.service.ccd;

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
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.fpl.service.SystemUserService;

import java.util.Map;

import static java.util.Collections.emptyMap;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.quality.Strictness.LENIENT;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
class CCDConcurrencyHelperTest {
    private static final String USER_AUTH_TOKEN = "Bearer user-xyz";
    private static final String SERVICE_AUTH_TOKEN = "Bearer service-xyz";
    private static final long CASE_ID = 1L;

    @Mock
    private SystemUserService systemUserService;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private CoreCaseDataApi coreCaseDataApi;

    @Spy
    @InjectMocks
    private CCDConcurrencyHelper helper;

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
        void shouldStartEvent() {
            helper.startEvent(CASE_ID, eventId);

            verify(coreCaseDataApi).startEventForCaseWorker(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, userId,
                JURISDICTION, CASE_TYPE, Long.toString(CASE_ID), eventId);
        }

        @Test
        void shouldSubmitEventWithoutCaseData() {
            StartEventResponse startEventResponse = StartEventResponse.builder()
                .eventId(eventId)
                .token(eventToken)
                .caseDetails(CaseDetails.builder().data(emptyMap()).build())
                .build();

            helper.submitEvent(startEventResponse, CASE_ID, emptyMap());

            verify(coreCaseDataApi).submitEventForCaseWorker(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, userId, JURISDICTION,
                CASE_TYPE, Long.toString(CASE_ID), true,
                buildCaseDataContent(eventId, eventToken, emptyMap()));
        }

        @Test
        void shouldSubmitEventWithCaseData() {
            StartEventResponse startEventResponse = StartEventResponse.builder()
                .eventId(eventId)
                .token(eventToken)
                .caseDetails(CaseDetails.builder().data(Map.of("id", 12345L)).build())
                .build();

            Map<String, Object> updates = Map.of("caseName", "new case name");

            helper.submitEvent(startEventResponse, CASE_ID, updates);

            verify(coreCaseDataApi).submitEventForCaseWorker(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, userId, JURISDICTION,
                CASE_TYPE, Long.toString(CASE_ID), true,
                buildCaseDataContent(eventId, eventToken, updates));
        }


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
