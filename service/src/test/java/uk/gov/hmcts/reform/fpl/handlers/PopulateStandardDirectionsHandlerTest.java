package uk.gov.hmcts.reform.fpl.handlers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.fpl.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.fpl.events.PopulateStandardDirectionsEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.configuration.DirectionConfiguration;
import uk.gov.hmcts.reform.fpl.model.configuration.Display;
import uk.gov.hmcts.reform.fpl.model.configuration.OrderDefinition;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.CommonDirectionService;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;
import uk.gov.hmcts.reform.fpl.service.OrdersLookupService;
import uk.gov.hmcts.reform.fpl.service.UserDetailsService;
import uk.gov.hmcts.reform.fpl.service.calendar.BankHolidaysService;
import uk.gov.hmcts.reform.fpl.service.calendar.CalendarService;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class, HearingBookingService.class})
class PopulateStandardDirectionsHandlerTest {
    private static final String CASE_EVENT = "populateSDO";
    private static final String TOKEN = "1";
    private static final String USER_ID = "12345";
    private static final String CASE_ID = "12345";
    private static final String AUTH_TOKEN = "Bearer token";
    private static final String DIRECTION_TITLE = "Direction";
    private static final String DIRECTION_TEXT = "Example Direction";
    private static final String DIRECTION_TEXT_SPECIAL_CHARACTERS = "- Test body's 1 \n\n- Two";
    private static final LocalDate MONDAY_DATE = LocalDate.of(2099, 1, 12);
    private static final LocalDateTime MONDAY_DATE_AT_NOON = LocalDateTime.of(MONDAY_DATE, LocalTime.NOON);
    private static final String TWO_DAYS_BEFORE_HEARING = "-2";
    private static final String THREE_DAYS_BEFORE_HEARING = "-3";
    private static final String SAME_DAY_AS_HEARING = "0";

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
    private ObjectMapper mapper;

    @MockBean
    private UserDetailsService userDetailsService;

    @InjectMocks
    private CommonDirectionService commonDirectionService;

    @Mock
    private BankHolidaysService bankHolidaysService;

    @InjectMocks
    private CalendarService calendarService;

    @Captor
    private ArgumentCaptor<CaseDataContent> caseDataContent;

    private PopulateStandardDirectionsHandler handler;

    private CallbackRequest callbackRequest;

    @BeforeEach
    void before() {
        handler = new PopulateStandardDirectionsHandler(mapper, ordersLookupService, coreCaseDataApi,
            authTokenGenerator, idamClient, userConfig, commonDirectionService, hearingBookingService, calendarService);

        given(idamClient.authenticateUser(userConfig.getUserName(), userConfig.getPassword())).willReturn(TOKEN);
        given(idamClient.getUserInfo(TOKEN)).willReturn(UserInfo.builder().uid(USER_ID).build());
        given(authTokenGenerator.generate()).willReturn(AUTH_TOKEN);
        given(userDetailsService.getUserName()).willReturn("Emma Taylor");
        given(requestData.userId()).willReturn(USER_ID);
        given(requestData.authorisation()).willReturn(AUTH_TOKEN);
        given(bankHolidaysService.getBankHolidays()).willReturn(Set.of(LocalDate.now()));

        callbackRequest = callbackRequest();
    }

    @Test
    void shouldAddDirectionCompleteByDateBeforeWeekendWhenDeltaLandsOnWeekendDay() throws IOException {
        callbackRequest = getCallbackRequestWithCustomHearingOnMonday();

        given(startPopulateStandardDirectionsEvent()).willReturn(getStartEventResponse(callbackRequest));

        // delta of -2 would result in Saturday complete by date.
        given(ordersLookupService.getStandardDirectionOrder())
            .willReturn(getOrderDefinition(DIRECTION_TEXT, TWO_DAYS_BEFORE_HEARING));

        handler.populateStandardDirections(new PopulateStandardDirectionsEvent(callbackRequest, requestData));

        verifyCoreCaseDataApiIsCalledWithCorrectParameters();

        List<Direction> directions = localAuthorityDirections(getCaseData());
        // ignoring Saturday and Sunday and taking into account delta = -2 results in 4.
        assertThat(directions).containsOnly(expectedDirection(DIRECTION_TEXT, MONDAY_DATE_AT_NOON.minusDays(4)));
        assertThat(directions.get(0).getDateToBeCompletedBy().getDayOfWeek()).isEqualTo(DayOfWeek.THURSDAY);
    }

    @Test
    void shouldPrepopulateDirectionsCorrectlyWhenDifferentDeltaValues() throws IOException {
        callbackRequest = getCallbackRequestWithCustomHearingOnMonday();

        given(startPopulateStandardDirectionsEvent()).willReturn(getStartEventResponse(callbackRequest));

        // delta of -2 and -3
        given(ordersLookupService.getStandardDirectionOrder())
            .willReturn(getOrderDefinitionContainingTwoDirectionsWithDifferentDeltas());

        handler.populateStandardDirections(new PopulateStandardDirectionsEvent(callbackRequest, requestData));

        verifyCoreCaseDataApiIsCalledWithCorrectParameters();

        List<Direction> directions = localAuthorityDirections(getCaseData());

        assertThat(directions).containsOnly(
            expectedDirection(DIRECTION_TEXT, MONDAY_DATE_AT_NOON.minusDays(4)),
            expectedDirection(DIRECTION_TEXT, MONDAY_DATE_AT_NOON.minusDays(5)));
    }

    @Test
    void shouldPopulateStandardDirectionsWhenPopulatedDisplayInConfiguration() throws IOException {
        callbackRequest = getCallbackRequestWithCustomHearingOnMonday();

        given(startPopulateStandardDirectionsEvent()).willReturn(getStartEventResponse(callbackRequest));

        given(ordersLookupService.getStandardDirectionOrder())
            .willReturn(getOrderDefinition(DIRECTION_TEXT, SAME_DAY_AS_HEARING));

        handler.populateStandardDirections(new PopulateStandardDirectionsEvent(callbackRequest, requestData));

        verifyCoreCaseDataApiIsCalledWithCorrectParameters();

        assertThat(caseDataContent.getValue()).isEqualTo(expectedCaseDataContent(callbackRequest));
        assertThat(localAuthorityDirections(getCaseData()))
            .containsOnly(expectedDirection(DIRECTION_TEXT, MONDAY_DATE_AT_NOON));
    }

    @Test
    void shouldPopulateStandardDirectionsWhenNullDeltaValueInConfiguration() throws IOException {
        given(startPopulateStandardDirectionsEvent()).willReturn(getStartEventResponse(callbackRequest));

        given(ordersLookupService.getStandardDirectionOrder())
            .willReturn(getOrderDefinition(DIRECTION_TEXT, null));

        handler.populateStandardDirections(new PopulateStandardDirectionsEvent(callbackRequest, requestData));

        verifyCoreCaseDataApiIsCalledWithCorrectParameters();

        assertThat(caseDataContent.getValue()).isEqualTo(expectedCaseDataContent(callbackRequest));
        assertThat(localAuthorityDirections(getCaseData()))
            .containsOnly(expectedDirection(DIRECTION_TEXT, null));
    }

    @Test
    void shouldPopulateStandardDirectionsWhenTextContainsSpecialCharacters() throws IOException {
        callbackRequest = getCallbackRequestWithCustomHearingOnMonday();

        given(startPopulateStandardDirectionsEvent()).willReturn(getStartEventResponse(callbackRequest));

        given(ordersLookupService.getStandardDirectionOrder())
            .willReturn(getOrderDefinition(DIRECTION_TEXT_SPECIAL_CHARACTERS, SAME_DAY_AS_HEARING));

        handler.populateStandardDirections(new PopulateStandardDirectionsEvent(callbackRequest, requestData));

        verifyCoreCaseDataApiIsCalledWithCorrectParameters();

        assertThat(localAuthorityDirections(getCaseData()))
            .containsOnly(expectedDirection(DIRECTION_TEXT_SPECIAL_CHARACTERS, MONDAY_DATE_AT_NOON));
    }

    //TODO: this test just asserts previous functionality. To be looked into in FPLA-1516.
    @Test
    void shouldAddNoCompleteByDateWhenNoHearings() throws IOException {
        callbackRequest.getCaseDetails().getData().remove("hearingDetails");

        given(startPopulateStandardDirectionsEvent()).willReturn(getStartEventResponse(callbackRequest));

        given(ordersLookupService.getStandardDirectionOrder())
            .willReturn(getOrderDefinition(DIRECTION_TEXT_SPECIAL_CHARACTERS, SAME_DAY_AS_HEARING));

        handler.populateStandardDirections(new PopulateStandardDirectionsEvent(callbackRequest, requestData));

        verifyCoreCaseDataApiIsCalledWithCorrectParameters();

        assertThat(localAuthorityDirections(getCaseData()))
            .containsOnly(expectedDirection(DIRECTION_TEXT_SPECIAL_CHARACTERS, null));
    }

    @Test
    void shouldDefaultToBeginningOfTheDayWhenNoTimeSpecifiedInDirectionConfig() throws IOException {
        callbackRequest = getCallbackRequestWithCustomHearingOnMonday();

        given(startPopulateStandardDirectionsEvent()).willReturn(getStartEventResponse(callbackRequest));

        given(ordersLookupService.getStandardDirectionOrder()).willReturn(getOrderDefinition(null));

        handler.populateStandardDirections(new PopulateStandardDirectionsEvent(callbackRequest, requestData));

        verifyCoreCaseDataApiIsCalledWithCorrectParameters();

        assertThat(localAuthorityDirections(getCaseData()))
            .containsOnly(expectedDirection(DIRECTION_TEXT, MONDAY_DATE.atStartOfDay()));

    }

    private StartEventResponse startPopulateStandardDirectionsEvent() {
        return coreCaseDataApi.startEventForCaseWorker(
            TOKEN, AUTH_TOKEN, USER_ID, JURISDICTION, CASE_TYPE, CASE_ID, CASE_EVENT);
    }

    private OrderDefinition getOrderDefinition(String text, String delta) {
        return OrderDefinition.builder()
            .directions(ImmutableList.of(directionConfiguration(text, delta)))
            .build();
    }

    private OrderDefinition getOrderDefinition(String time) {
        return OrderDefinition.builder()
            .directions(ImmutableList.of(directionConfiguration(time)))
            .build();
    }

    private CallbackRequest getCallbackRequestWithCustomHearingOnMonday() {
        return CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(Long.parseLong(CASE_ID))
                .jurisdiction(JURISDICTION)
                .caseTypeId(CASE_TYPE)
                .data(getCaseDetailsWithMondayHearing())
                .build())
            .build();
    }

    private Map<String, Object> getCaseDetailsWithMondayHearing() {
        CaseData caseData = CaseData.builder()
            .hearingDetails(wrapElements(HearingBooking.builder()
                .startDate(MONDAY_DATE_AT_NOON)
                .build()))
            .build();

        return mapper.convertValue(caseData, new TypeReference<>() {});
    }

    private StartEventResponse getStartEventResponse(CallbackRequest callbackRequest) {
        return StartEventResponse.builder()
            .caseDetails(callbackRequest.getCaseDetails())
            .eventId(CASE_EVENT)
            .token(TOKEN)
            .build();
    }

    private CaseDataContent expectedCaseDataContent(CallbackRequest callbackRequest) {
        return CaseDataContent.builder()
            .eventToken(TOKEN)
            .event(Event.builder()
                .id(CASE_EVENT)
                .build())
            .data(callbackRequest.getCaseDetails().getData())
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

    private CaseData getCaseData() {
        return mapper.convertValue(caseDataContent.getValue().getData(), CaseData.class);
    }

    private List<Direction> localAuthorityDirections(CaseData caseData) {
        return unwrapElements(caseData.getLocalAuthorityDirections());
    }

    private Direction expectedDirection(String directionText, LocalDateTime localDateTime) {
        return Direction.builder()
            .directionType(DIRECTION_TITLE)
            .directionText(directionText)
            .assignee(LOCAL_AUTHORITY)
            .readOnly("No")
            .directionRemovable("No")
            .dateToBeCompletedBy(localDateTime)
            .build();
    }

    private OrderDefinition getOrderDefinitionContainingTwoDirectionsWithDifferentDeltas() {
        return OrderDefinition.builder()
            .directions(ImmutableList.of(
                directionConfiguration(DIRECTION_TEXT, TWO_DAYS_BEFORE_HEARING),
                directionConfiguration(DIRECTION_TEXT, THREE_DAYS_BEFORE_HEARING)))
            .build();
    }

    private DirectionConfiguration directionConfiguration(String directionText, String deltaValue) {
        return DirectionConfiguration.builder()
            .assignee(LOCAL_AUTHORITY)
            .title(DIRECTION_TITLE)
            .text(directionText)
            .display(Display.builder()
                .delta(deltaValue)
                .due(Display.Due.BY)
                .templateDateFormat("h:mma, d MMMM yyyy")
                .directionRemovable(false)
                .time("12:00:00")
                .build())
            .build();
    }

    private DirectionConfiguration directionConfiguration(String time) {
        return DirectionConfiguration.builder()
            .assignee(LOCAL_AUTHORITY)
            .title(DIRECTION_TITLE)
            .text(DIRECTION_TEXT)
            .display(Display.builder()
                .delta(SAME_DAY_AS_HEARING)
                .due(Display.Due.BY)
                .templateDateFormat("h:mma, d MMMM yyyy")
                .directionRemovable(false)
                .time(time)
                .build())
            .build();
    }
}
