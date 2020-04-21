package uk.gov.hmcts.reform.fpl.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.fpl.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.fpl.events.PopulateStandardDirectionsEvent;
import uk.gov.hmcts.reform.fpl.model.configuration.DirectionConfiguration;
import uk.gov.hmcts.reform.fpl.model.configuration.Display;
import uk.gov.hmcts.reform.fpl.model.configuration.OrderDefinition;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.CommonDirectionService;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;
import uk.gov.hmcts.reform.fpl.service.OrdersLookupService;
import uk.gov.hmcts.reform.fpl.service.UserDetailsService;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static java.util.Collections.EMPTY_LIST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class, HearingBookingService.class})
class PopulateStandardDirectionsHandlerTest {
    private static final String CASE_EVENT = "populateSDO";
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

    @Mock
    private SystemUpdateUserConfiguration userConfig;

    @Mock
    private RequestData requestData;

    @Autowired
    private HearingBookingService hearingBookingService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserDetailsService userDetailsService;

    @InjectMocks
    private CommonDirectionService commonDirectionService;

    private PopulateStandardDirectionsHandler populateStandardDirectionsHandler;

    @BeforeEach
    void before() {
        populateStandardDirectionsHandler = new PopulateStandardDirectionsHandler(objectMapper, ordersLookupService,
            coreCaseDataApi, authTokenGenerator, idamClient, userConfig, commonDirectionService, hearingBookingService);

        given(idamClient.authenticateUser(userConfig.getUserName(), userConfig.getPassword())).willReturn(TOKEN);

        given(idamClient.getUserInfo(TOKEN)).willReturn(UserInfo.builder()
            .uid(USER_ID)
            .build());

        given(authTokenGenerator.generate()).willReturn(AUTH_TOKEN);

        given(userDetailsService.getUserName()).willReturn("Emma Taylor");

        given(requestData.userId()).willReturn(USER_ID);

        given(requestData.authorisation()).willReturn(AUTH_TOKEN);
    }

    @Test
    void shouldPopulateStandardDirectionsWhenPopulatedDisplayInConfiguration() throws IOException {
        CallbackRequest callbackRequest = callbackRequest();

        given(coreCaseDataApi.startEventForCaseWorker(
            TOKEN, AUTH_TOKEN, USER_ID, JURISDICTION, CASE_TYPE, CASE_ID, CASE_EVENT))
            .willReturn(StartEventResponse.builder()
                .caseDetails(callbackRequest.getCaseDetails())
                .eventId(CASE_EVENT)
                .token(TOKEN)
                .build());

        given(ordersLookupService.getStandardDirectionOrder()).willReturn(OrderDefinition.builder()
            .directions(ImmutableList.of(
                DirectionConfiguration.builder()
                    .assignee(LOCAL_AUTHORITY)
                    .title("Direction")
                    .text("Example Direction")
                    .display(Display.builder()
                        .delta("0")
                        .due(Display.Due.BY)
                        .templateDateFormat("h:mma, d MMMM yyyy")
                        .directionRemovable(false)
                        .build())
                    .build()
            ))
            .build());

        populateStandardDirectionsHandler.populateStandardDirections(
            new PopulateStandardDirectionsEvent(callbackRequest, requestData));

        verify(coreCaseDataApi).submitEventForCaseWorker(
            TOKEN, AUTH_TOKEN, USER_ID, JURISDICTION, CASE_TYPE, CASE_ID, true, CaseDataContent.builder()
                .eventToken(TOKEN)
                .event(Event.builder()
                    .id(CASE_EVENT)
                    .build())
                .data(callbackRequest.getCaseDetails().getData())
                .build());
    }

    @Test
    void shouldPopulateStandardDirectionsWhenNullDeltaValueInConfiguration() throws IOException {
        CallbackRequest callbackRequest = callbackRequest();

        given(coreCaseDataApi.startEventForCaseWorker(
            TOKEN, AUTH_TOKEN, USER_ID, JURISDICTION, CASE_TYPE, CASE_ID, CASE_EVENT))
            .willReturn(StartEventResponse.builder()
                .caseDetails(callbackRequest.getCaseDetails())
                .eventId(CASE_EVENT)
                .token(TOKEN)
                .build());

        given(ordersLookupService.getStandardDirectionOrder()).willReturn(OrderDefinition.builder()
            .directions(ImmutableList.of(
                DirectionConfiguration.builder()
                    .assignee(LOCAL_AUTHORITY)
                    .title("Direction")
                    .text("Example Direction")
                    .display(Display.builder()
                        .delta(null)
                        .due(Display.Due.BY)
                        .templateDateFormat("h:mma, d MMMM yyyy")
                        .directionRemovable(false)
                        .build())
                    .build()
            ))
            .build());

        populateStandardDirectionsHandler.populateStandardDirections(
            new PopulateStandardDirectionsEvent(callbackRequest, requestData));

        verify(coreCaseDataApi).submitEventForCaseWorker(
            TOKEN, AUTH_TOKEN, USER_ID, JURISDICTION, CASE_TYPE, CASE_ID, true, CaseDataContent.builder()
                .eventToken(TOKEN)
                .event(Event.builder()
                    .id(CASE_EVENT)
                    .build())
                .data(callbackRequest.getCaseDetails().getData())
                .build());
    }

    @Test
    void shouldPopulateStandardDirectionsWhenTextContainsSpecialCharacters() throws IOException {
        CallbackRequest callbackRequest = callbackRequest();

        given(coreCaseDataApi.startEventForCaseWorker(
            TOKEN, AUTH_TOKEN, USER_ID, JURISDICTION, CASE_TYPE, CASE_ID, CASE_EVENT))
            .willReturn(StartEventResponse.builder()
                .caseDetails(callbackRequest.getCaseDetails())
                .eventId(CASE_EVENT)
                .token(TOKEN)
                .build());

        given(ordersLookupService.getStandardDirectionOrder()).willReturn(OrderDefinition.builder()
            .directions(ImmutableList.of(
                DirectionConfiguration.builder()
                    .assignee(LOCAL_AUTHORITY)
                    .title("Direction")
                    .text("- Test body's 1 \n\n- Two")
                    .display(Display.builder()
                        .delta("0")
                        .due(Display.Due.BY)
                        .templateDateFormat("h:mma, d MMMM yyyy")
                        .directionRemovable(false)
                        .build())
                    .build()
            ))
            .build());

        populateStandardDirectionsHandler.populateStandardDirections(
            new PopulateStandardDirectionsEvent(callbackRequest, requestData));

        assertThat(objectMapper.convertValue(
            callbackRequest.getCaseDetails().getData().get("localAuthorityDirections"), List.class).get(0))
            .extracting("value")
            .isEqualTo(Map.of(
                "assignee", "LOCAL_AUTHORITY",
                "dateToBeCompletedBy", "2020-01-01T15:30:00",
                "directionText", "- Test body's 1 \n\n- Two",
                "directionType", "Direction",
                "directionRemovable", "No",
                "readOnly", "No",
                "directionNeeded", "Yes",
                "responses", EMPTY_LIST));

        verify(coreCaseDataApi).submitEventForCaseWorker(
            TOKEN, AUTH_TOKEN, USER_ID, JURISDICTION, CASE_TYPE, CASE_ID, true, CaseDataContent.builder()
                .eventToken(TOKEN)
                .event(Event.builder()
                    .id(CASE_EVENT)
                    .build())
                .data(callbackRequest.getCaseDetails().getData())
                .build());
    }

    //TODO: this test just asserts previous functionality. To be looked into in FPLA-1516.
    @Test
    void shouldAddNoCompleteByDateWhenNoHearings() throws IOException {
        CallbackRequest callbackRequest = callbackRequest();
        callbackRequest.getCaseDetails().getData().remove("hearingDetails");

        given(coreCaseDataApi.startEventForCaseWorker(
            TOKEN, AUTH_TOKEN, USER_ID, JURISDICTION, CASE_TYPE, CASE_ID, CASE_EVENT))
            .willReturn(StartEventResponse.builder()
                .caseDetails(callbackRequest.getCaseDetails())
                .eventId(CASE_EVENT)
                .token(TOKEN)
                .build());

        given(ordersLookupService.getStandardDirectionOrder()).willReturn(OrderDefinition.builder()
            .directions(ImmutableList.of(
                DirectionConfiguration.builder()
                    .assignee(LOCAL_AUTHORITY)
                    .title("Direction")
                    .text("- Test body's 1 \n\n- Two")
                    .display(Display.builder()
                        .delta("0")
                        .due(Display.Due.BY)
                        .templateDateFormat("h:mma, d MMMM yyyy")
                        .directionRemovable(false)
                        .build())
                    .build()
            ))
            .build());

        populateStandardDirectionsHandler.populateStandardDirections(
            new PopulateStandardDirectionsEvent(callbackRequest, requestData));

        assertThat(objectMapper.convertValue(
            callbackRequest.getCaseDetails().getData().get("localAuthorityDirections"), List.class).get(0))
            .extracting("value")
            .isEqualTo(Map.of(
                "assignee", "LOCAL_AUTHORITY",
                "directionText", "- Test body's 1 \n\n- Two",
                "directionType", "Direction",
                "directionRemovable", "No",
                "readOnly", "No",
                "directionNeeded", "Yes",
                "responses", EMPTY_LIST));

        verify(coreCaseDataApi).submitEventForCaseWorker(
            TOKEN, AUTH_TOKEN, USER_ID, JURISDICTION, CASE_TYPE, CASE_ID, true, CaseDataContent.builder()
                .eventToken(TOKEN)
                .event(Event.builder()
                    .id(CASE_EVENT)
                    .build())
                .data(callbackRequest.getCaseDetails().getData())
                .build());
    }
}
