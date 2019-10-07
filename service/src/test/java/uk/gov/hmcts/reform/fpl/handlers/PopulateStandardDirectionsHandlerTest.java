package uk.gov.hmcts.reform.fpl.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.fpl.events.PopulateStandardDirectionsEvent;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.configuration.DirectionConfiguration;
import uk.gov.hmcts.reform.fpl.model.configuration.OrderDefinition;
import uk.gov.hmcts.reform.fpl.service.OrdersLookupService;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.io.IOException;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;

@SpringBootTest
@ActiveProfiles("integration-test")
class PopulateStandardDirectionsHandlerTest {
    private static final String CASE_EVENT = "populateSDO";
    private static final String USER_NAME = "fpl-system-update@mailnesia.com";
    private static final String PASSWORD = "Password12";
    private static final String TOKEN = "1";
    private static final String USER_ID = "12345";
    private static final String CASE_ID = "12345";
    private static final String AUTH_TOKEN = "Bearer token";

    @Mock
    private OrdersLookupService ordersLookupService;

    @Mock
    private CoreCaseDataApi coreCaseDataApi;

    @Mock
    private IdamClient idamClient;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private PopulateStandardDirectionsHandler populateStandardDirectionsHandler;

    //TODO: test fails with horrible error, spring dependency related
    @Disabled
    @Test
    void shouldPopulateStandardDirections() throws IOException {
        given(idamClient.authenticateUser(USER_NAME, PASSWORD)).willReturn(TOKEN);

        given(idamClient.getUserDetails(TOKEN)).willReturn(UserDetails.builder()
            .id(USER_ID)
            .build());

        given(authTokenGenerator.generate()).willReturn(AUTH_TOKEN);

        given(coreCaseDataApi.startEventForCaseWorker(TOKEN, AUTH_TOKEN, USER_ID, JURISDICTION, CASE_TYPE, CASE_ID, CASE_EVENT))
            .willReturn(StartEventResponse.builder()
                .caseDetails(CaseDetails.builder()
                    .id(Long.parseLong(CASE_ID))
                    .build())
                .eventId(CASE_EVENT)
                .token(TOKEN)
                .build());

        given(ordersLookupService.getStandardDirectionOrder()).willReturn(OrderDefinition.builder()
            .directions(ImmutableList.of(
                DirectionConfiguration.builder()
                    .assignee(LOCAL_AUTHORITY)
                    .title("Direction")
                    .text("Example Direction")
                    .build()
            ))
            .build());

        populateStandardDirectionsHandler.populateStandardDirections(
            new PopulateStandardDirectionsEvent(callbackRequest(), "", ""));

        verify(coreCaseDataApi, times(1)).submitEventForCaseWorker(
            TOKEN, AUTH_TOKEN, USER_ID, JURISDICTION, CASE_TYPE, CASE_ID, true, CaseDataContent.builder()
                .eventToken(TOKEN)
                .event(Event.builder()
                    .id(CASE_EVENT)
                    .build())
                .data(ImmutableMap.of(
                    LOCAL_AUTHORITY.getValue(), ImmutableList.of(Direction.builder()
                        .assignee(LOCAL_AUTHORITY)
                        .type("Direction")
                        .text("Example Direction")
                        .build())))
                .build());
    }

}
