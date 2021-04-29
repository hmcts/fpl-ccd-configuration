package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApiV2;
import uk.gov.hmcts.reform.ccd.model.AuditEvent;
import uk.gov.hmcts.reform.ccd.model.AuditEventsResponse;
import uk.gov.hmcts.reform.fpl.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.idam.client.IdamClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditEventServiceTest {

    private static final String USERNAME = "Username";
    private static final String PASSWORD = "Password";
    private static final String USER_TOKEN = "USER_TOKEN";
    private static final String SERVICE_TOKEN = "SERVICE_TOKEN";
    private static final String CASE_ID = "1111";

    @Mock
    private IdamClient idamClient;

    @Mock
    private CoreCaseDataApiV2 caseDataApi;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private AuditEventsResponse auditEventsResponse;

    @Mock
    private SystemUpdateUserConfiguration userConfig;

    @InjectMocks
    private AuditEventService auditEventService;

    private static final LocalDateTime A_LOCAL_DATE_TIME = LocalDateTime.now();

    @BeforeEach
    void setup() {
        when(userConfig.getUserName()).thenReturn(USERNAME);
        when(userConfig.getPassword()).thenReturn(PASSWORD);
        when(idamClient.getAccessToken(USERNAME, PASSWORD)).thenReturn(USER_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
        when(caseDataApi.getAuditEvents(USER_TOKEN, SERVICE_TOKEN, false, CASE_ID))
            .thenReturn(auditEventsResponse);
    }

    @Test
    void shouldGetAuditEventByName() {
        AuditEvent expectedAuditEvent = buildAuditEvent("nocRequest", A_LOCAL_DATE_TIME);

        List<AuditEvent> auditEventList = List.of(
            expectedAuditEvent,
            buildAuditEvent("enterChildren", A_LOCAL_DATE_TIME),
            buildAuditEvent("taskList", A_LOCAL_DATE_TIME));

        when(auditEventsResponse.getAuditEvents()).thenReturn(auditEventList);

        Optional<AuditEvent> actualAuditEvent
            = auditEventService.getLatestAuditEventByName(CASE_ID, "nocRequest");

        assertThat(actualAuditEvent).isPresent().contains(expectedAuditEvent);
    }

    @Test
    void shouldGetLatestInstanceOfAuditEventByName() {
        AuditEvent expectedAuditEvent = buildAuditEvent("nocRequest", A_LOCAL_DATE_TIME);

        List<AuditEvent> auditEventList = List.of(
            buildAuditEvent("nocRequest", A_LOCAL_DATE_TIME.minusMinutes(3)),
            expectedAuditEvent,
            buildAuditEvent("nocRequest", A_LOCAL_DATE_TIME.minusMinutes(2)));

        when(auditEventsResponse.getAuditEvents()).thenReturn(auditEventList);

        Optional<AuditEvent> actualAuditEvent
            = auditEventService.getLatestAuditEventByName(CASE_ID, "nocRequest");

        assertThat(actualAuditEvent).isPresent().contains(expectedAuditEvent);
    }

    @Test
    void shouldReturnEmptyOptionalIfAuditEventWithNameCannotBeFound() {
        List<AuditEvent> auditEventList = List.of(
            buildAuditEvent("enterChildren", A_LOCAL_DATE_TIME),
            buildAuditEvent("manageDocuments", A_LOCAL_DATE_TIME));

        when(auditEventsResponse.getAuditEvents()).thenReturn(auditEventList);

        Optional<AuditEvent> actualAuditEvent
            = auditEventService.getLatestAuditEventByName(CASE_ID, "nocRequest");

        assertThat(actualAuditEvent).isEmpty();
    }

    @Test
    void shouldReturnEmptyOptionalIfAuditEventsIsEmpty() {

        when(auditEventsResponse.getAuditEvents()).thenReturn(List.of());

        Optional<AuditEvent> actualAuditEvent
            = auditEventService.getLatestAuditEventByName(CASE_ID, "nocRequest");

        assertThat(actualAuditEvent).isEmpty();
    }

    private AuditEvent buildAuditEvent(String eventId, LocalDateTime createdDate) {
        return AuditEvent.builder()
            .id(eventId)
            .userFirstName("Tom")
            .userLastName("Jones")
            .createdDate(createdDate)
            .build();
    }
}
