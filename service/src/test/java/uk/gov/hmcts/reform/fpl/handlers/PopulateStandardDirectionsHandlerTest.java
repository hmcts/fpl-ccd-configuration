package uk.gov.hmcts.reform.fpl.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.fpl.enums.DirectionAssignee;
import uk.gov.hmcts.reform.fpl.events.PopulateStandardDirectionsEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.CommonDirectionService;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;
import uk.gov.hmcts.reform.fpl.service.StandardDirectionsService;
import uk.gov.hmcts.reform.fpl.service.config.SystemUpdateTestConfig;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.CAFCASS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.COURT;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.OTHERS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.PARENTS_AND_RESPONDENTS;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    JacksonAutoConfiguration.class, SystemUpdateTestConfig.class, PopulateStandardDirectionsHandler.class
})
class PopulateStandardDirectionsHandlerTest {
    private static final String CASE_EVENT = "populateSDO";
    private static final String TOKEN = "1";
    private static final String USER_ID = "12345";
    private static final String CASE_ID = "12345";
    private static final String AUTH_TOKEN = "Bearer token";
    private static final LocalDate DATE = LocalDate.of(2099, 1, 12);

    @Autowired
    private ObjectMapper mapper;

    @Mock
    private RequestData requestData;

    @MockBean
    private CoreCaseDataApi coreCaseDataApi;

    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    @MockBean
    private IdamClient idamClient;

    @MockBean
    private StandardDirectionsService standardDirectionsService;

    @MockBean
    private HearingBookingService hearingBookingService;

    @MockBean
    private CommonDirectionService directionService;

    @Autowired
    private PopulateStandardDirectionsHandler handler;

    @Captor
    private ArgumentCaptor<CaseDataContent> caseDataContent;

    @Captor
    private ArgumentCaptor<HearingBooking> hearingContent;

    @Captor
    private ArgumentCaptor<List<Element<Direction>>> directionsContent;

    @BeforeEach
    void before() {
        given(idamClient.authenticateUser(any(), any())).willReturn(TOKEN);
        given(idamClient.getUserInfo(TOKEN)).willReturn(UserInfo.builder().uid(USER_ID).build());
        given(authTokenGenerator.generate()).willReturn(AUTH_TOKEN);
        given(requestData.userId()).willReturn(USER_ID);
        given(requestData.authorisation()).willReturn(AUTH_TOKEN);

        when(hearingBookingService.getFirstHearing(any())).thenReturn(Optional.of(getHearing()));
        when(directionService.sortDirectionsByAssignee(directionsContent.capture())).thenReturn(directionMap());
    }

    @Test
    void shouldAddDirectionsToCaseData() throws IOException {
        List<Element<Direction>> directionsToReturn = directionsToReturn();

        when(standardDirectionsService.getDirections(hearingContent.capture())).thenReturn(directionsToReturn);

        CallbackRequest callbackRequest = getCallbackRequestWithHearing();
        when(startEventResponse()).thenReturn(getStartEventResponse(callbackRequest));

        handler.populateStandardDirections(new PopulateStandardDirectionsEvent(callbackRequest, requestData));

        assertThat(hearingContent.getValue()).isEqualTo(getHearing());
        assertThat(directionsContent.getValue()).isEqualTo(directionsToReturn);
        verifyCoreCaseDataApiIsCalledWithCorrectParameters();
        assertAssigneeDirectionsAreCorrect(caseDataContent.getValue());
    }

    private void assertAssigneeDirectionsAreCorrect(CaseDataContent caseDataContent) {
        CaseData caseData = mapper.convertValue(caseDataContent.getData(), CaseData.class);

        assertThatDirectionsAreExpected(caseData.getAllParties(), ALL_PARTIES);
        assertThatDirectionsAreExpected(caseData.getLocalAuthorityDirections(), LOCAL_AUTHORITY);
        assertThatDirectionsAreExpected(caseData.getCafcassDirections(), CAFCASS);
        assertThatDirectionsAreExpected(caseData.getOtherPartiesDirections(), OTHERS);
        assertThatDirectionsAreExpected(caseData.getRespondentDirections(), PARENTS_AND_RESPONDENTS);
        assertThatDirectionsAreExpected(caseData.getCourtDirections(), COURT);
    }

    private void assertThatDirectionsAreExpected(List<Element<Direction>> directions, DirectionAssignee assignee) {
        assertThat(unwrapElements(directions)).containsOnly(directionForAssignee(assignee));
    }

    private Map<DirectionAssignee, List<Element<Direction>>> directionMap() {
        Map<DirectionAssignee, List<Element<Direction>>> directionsMap = new HashMap<>();
        Stream.of(DirectionAssignee.values())
            .forEach(assignee -> directionsMap.put(assignee, wrapElements(directionForAssignee(assignee))));

        return directionsMap;
    }

    private Direction directionForAssignee(DirectionAssignee assignee) {
        return Direction.builder().directionText("example title").assignee(assignee).build();
    }

    private List<Element<Direction>> directionsToReturn() {
        List<Element<Direction>> directions = new ArrayList<>();
        Stream.of(DirectionAssignee.values())
            .forEach(assignee -> directions.add(element(directionForAssignee(assignee))));

        return directions;
    }

    private StartEventResponse startEventResponse() {
        return coreCaseDataApi.startEventForCaseWorker(
            TOKEN, AUTH_TOKEN, USER_ID, JURISDICTION, CASE_TYPE, CASE_ID, CASE_EVENT);
    }

    private CallbackRequest getCallbackRequestWithHearing() {
        Map<String, Object> data = new HashMap<>();

        data.put("hearingDetails", wrapElements(getHearing()));

        return CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(Long.parseLong(CASE_ID))
                .jurisdiction(JURISDICTION)
                .caseTypeId(CASE_TYPE)
                .data(data)
                .build())
            .build();
    }

    private HearingBooking getHearing() {
        return HearingBooking.builder()
            .startDate(DATE.atTime(12, 0, 0))
            .build();
    }

    private StartEventResponse getStartEventResponse(CallbackRequest callbackRequest) {
        return StartEventResponse.builder()
            .caseDetails(callbackRequest.getCaseDetails())
            .eventId(CASE_EVENT)
            .token(TOKEN)
            .build();
    }

    private void verifyCoreCaseDataApiIsCalledWithCorrectParameters() {
        verify(coreCaseDataApi).submitEventForCaseWorker(
            eq(TOKEN),
            eq(AUTH_TOKEN),
            eq(USER_ID),
            eq(JURISDICTION),
            eq(CASE_TYPE),
            eq(CASE_ID),
            eq(true),
            caseDataContent.capture());
    }
}
