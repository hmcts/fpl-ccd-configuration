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
import uk.gov.hmcts.reform.fpl.events.PopulateStandardDirectionsEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.CommonDirectionService;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;
import uk.gov.hmcts.reform.fpl.service.JsonOrdersLookupService;
import uk.gov.hmcts.reform.fpl.service.calendar.CalendarService;
import uk.gov.hmcts.reform.fpl.service.config.SystemUpdateTestConfig;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Integer.parseInt;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    JacksonAutoConfiguration.class, HearingBookingService.class, PopulateStandardDirectionsHandler.class,
    SystemUpdateTestConfig.class, CommonDirectionService.class, JsonOrdersLookupService.class,
    FixedTimeConfiguration.class
})
class PopulateStandardDirectionsHandlerTest {
    private static final String CASE_EVENT = "populateSDO";
    private static final String TOKEN = "1";
    private static final String USER_ID = "12345";
    private static final String CASE_ID = "12345";
    private static final String AUTH_TOKEN = "Bearer token";
    private static final LocalDate DATE = LocalDate.of(2099, 1, 12);
    private static final String TWO_DAYS_BEFORE_HEARING = "-2";
    private static final String THREE_DAYS_BEFORE_HEARING = "-3";

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private CoreCaseDataApi coreCaseDataApi;

    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    @MockBean
    private IdamClient idamClient;

    @Mock
    private RequestData requestData;

    @MockBean
    private CalendarService calendarService;

    @Autowired
    private PopulateStandardDirectionsHandler handler;

    @Captor
    private ArgumentCaptor<CaseDataContent> caseDataContent;

    @BeforeEach
    void before() {
        given(idamClient.authenticateUser(any(), any())).willReturn(TOKEN);
        given(idamClient.getUserInfo(TOKEN)).willReturn(UserInfo.builder().uid(USER_ID).build());
        given(authTokenGenerator.generate()).willReturn(AUTH_TOKEN);
        given(requestData.userId()).willReturn(USER_ID);
        given(requestData.authorisation()).willReturn(AUTH_TOKEN);

        when(calendarService.getWorkingDayFrom(eq(DATE), eq(parseInt(TWO_DAYS_BEFORE_HEARING))))
            .thenReturn(DATE.minusDays(2));
        when(calendarService.getWorkingDayFrom(eq(DATE), eq(parseInt(THREE_DAYS_BEFORE_HEARING))))
            .thenReturn(DATE.minusDays(3));

    }

    @Test
    void shouldAddDirectionsToCaseData() throws IOException {
        CallbackRequest callbackRequest = getCallbackRequestWithHearing();
        when(startEventResponse()).thenReturn(getStartEventResponse(callbackRequest));

        handler.populateStandardDirections(new PopulateStandardDirectionsEvent(callbackRequest, requestData));

        verifyCoreCaseDataApiIsCalledWithCorrectParameters();

        CaseData caseData = mapper.convertValue(caseDataContent.getValue().getData(), CaseData.class);
        List<Direction> allParties = unwrapElements(caseData.getAllParties());
        List<Direction> localAuthority = unwrapElements(caseData.getLocalAuthorityDirections());

        assertThat(allParties).containsOnly(expectedAllPartiesDirection());

        assertThat(localAuthority).containsOnly(expectedLocalAuthorityDirections());
    }

    private Direction[] expectedLocalAuthorityDirections() {
        return new Direction[]{Direction.builder()
            .assignee(LOCAL_AUTHORITY)
            .directionType("Test SDO type 2")
            .directionText("Test body 2\n")
            .readOnly("No")
            .directionRemovable("No")
            .directionNeeded("Yes")
            .dateToBeCompletedBy(DATE.minusDays(3).atTime(12, 0, 0))
            .responses(emptyList())
            .build(),
            Direction.builder()
                .assignee(LOCAL_AUTHORITY)
                .directionType("Test SDO type 3")
                .directionText("Test body 3\n")
                .readOnly("No")
                .directionRemovable("Yes")
                .directionNeeded("Yes")
                .dateToBeCompletedBy(DATE.minusDays(2).atTime(16, 0, 0))
                .responses(emptyList())
                .build()};
    }

    private Direction expectedAllPartiesDirection() {
        return Direction.builder()
            .assignee(ALL_PARTIES)
            .directionType("Test SDO type 1")
            .directionText("- Test body 1 \n\n- Two\n")
            .readOnly("Yes")
            .directionRemovable("No")
            .directionNeeded("Yes")
            .dateToBeCompletedBy(DATE.atStartOfDay())
            .responses(emptyList())
            .build();
    }

    private StartEventResponse startEventResponse() {
        return coreCaseDataApi.startEventForCaseWorker(
            TOKEN, AUTH_TOKEN, USER_ID, JURISDICTION, CASE_TYPE, CASE_ID, CASE_EVENT);
    }

    private CallbackRequest getCallbackRequestWithHearing() {
        Map<String, Object> data = new HashMap<>();

        data.put("hearingDetails", wrapElements(HearingBooking.builder()
            .startDate(DATE.atTime(12,0,0))
            .build()));

        return CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(Long.parseLong(CASE_ID))
                .jurisdiction(JURISDICTION)
                .caseTypeId(CASE_TYPE)
                .data(data)
                .build())
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
