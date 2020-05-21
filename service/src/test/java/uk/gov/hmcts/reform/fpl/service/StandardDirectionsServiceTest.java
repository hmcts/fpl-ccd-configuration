package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.configuration.DirectionConfiguration;
import uk.gov.hmcts.reform.fpl.model.configuration.Display;
import uk.gov.hmcts.reform.fpl.model.configuration.OrderDefinition;
import uk.gov.hmcts.reform.fpl.service.calendar.CalendarService;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.model.configuration.Display.Due.BY;
import static uk.gov.hmcts.reform.fpl.model.configuration.Display.Due.ON;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {StandardDirectionsService.class})
class StandardDirectionsServiceTest {
    private static final String DIRECTION_TYPE_1 = "Test SDO type 1";
    private static final String DIRECTION_TEXT_1 = "- Test body 1 \n\n- Two\n";
    private static final String DIRECTION_TYPE_2 = "Test SDO type 2";
    private static final String DIRECTION_TEXT_2 = "Test body 2\n";
    private static final String DIRECTION_TYPE_3 = "Test SDO type 3";
    private static final String DIRECTION_TEXT_3 = "Test body 3\n";
    private static final Element<Direction> DIRECTION_ELEMENT = element(Direction.builder().build());

    private final List<DirectionConfiguration> DIRECTION_CONFIGURATION = testDirectionConfigurations();

    @MockBean
    private CalendarService calendarService;

    @MockBean
    private CommonDirectionService commonDirectionService;

    @MockBean
    private OrdersLookupService ordersLookupService;

    @Autowired
    private StandardDirectionsService service;

    @BeforeEach
    void setup() {
        given(ordersLookupService.getStandardDirectionOrder()).willReturn(OrderDefinition.builder()
            .directions(DIRECTION_CONFIGURATION)
            .build());
        given(calendarService.getWorkingDayFrom(any(), anyInt())).willReturn(LocalDate.of(2040, 1, 1));
        given(commonDirectionService.constructDirectionForCCD(any(), any())).willReturn(DIRECTION_ELEMENT);
    }

    @Test
    void shouldReturnDirectionsWhenThereIsNoHearing() {
        List<Element<Direction>> directions = service.getDirections(null);

        verify(ordersLookupService).getStandardDirectionOrder();
        verify(calendarService, never()).getWorkingDayFrom(any(), anyInt());
        verify(commonDirectionService).constructDirectionForCCD(DIRECTION_CONFIGURATION.get(0), null);
        verify(commonDirectionService).constructDirectionForCCD(DIRECTION_CONFIGURATION.get(1), null);
        verify(commonDirectionService).constructDirectionForCCD(DIRECTION_CONFIGURATION.get(2), null);
        verifyNoMoreInteractions(commonDirectionService);
        assertThat(directions).containsExactly(DIRECTION_ELEMENT, DIRECTION_ELEMENT, DIRECTION_ELEMENT);
    }

    @Test
    void shouldReturnDirectionsWhenThereIsNoHearingDate() {
        List<Element<Direction>> directions = service.getDirections(HearingBooking.builder().build());

        verify(ordersLookupService).getStandardDirectionOrder();
        verify(calendarService, never()).getWorkingDayFrom(any(), anyInt());
        verify(commonDirectionService).constructDirectionForCCD(DIRECTION_CONFIGURATION.get(0), null);
        verify(commonDirectionService).constructDirectionForCCD(DIRECTION_CONFIGURATION.get(1), null);
        verify(commonDirectionService).constructDirectionForCCD(DIRECTION_CONFIGURATION.get(2), null);
        verifyNoMoreInteractions(commonDirectionService);
        assertThat(directions).containsExactly(DIRECTION_ELEMENT, DIRECTION_ELEMENT, DIRECTION_ELEMENT);
    }

    @Test
    void shouldReturnDirectionsWhenThereIsHearingDate() {
        LocalDate date = LocalDate.of(2099, 6, 1);
        HearingBooking hearingBooking = HearingBooking.builder().startDate(date.atTime(12, 0, 0)).build();
        given(calendarService.getWorkingDayFrom(date,-3)).willReturn(date.minusDays(3));
        given(calendarService.getWorkingDayFrom(date,-2)).willReturn(date.minusDays(2));
        given(calendarService.getWorkingDayFrom(date,0)).willReturn(date);

        List<Element<Direction>> directions = service.getDirections(hearingBooking);

        verify(ordersLookupService).getStandardDirectionOrder();
        verify(calendarService).getWorkingDayFrom(date, -3);
        verify(calendarService).getWorkingDayFrom(date, -2);
        verifyNoMoreInteractions(calendarService);
//        verify(commonDirectionService).constructDirectionForCCD(DIRECTION_CONFIGURATION.get(0), date.minusDays(3).atTime(16, 0, 0));
//        verify(commonDirectionService).constructDirectionForCCD(DIRECTION_CONFIGURATION.get(1), date.minusDays(2).atTime(15, 0, 0));
//        verify(commonDirectionService).constructDirectionForCCD(DIRECTION_CONFIGURATION.get(2), date.atTime(12, 0, 0));
//        verifyNoMoreInteractions(commonDirectionService);
        assertThat(directions).containsExactly(DIRECTION_ELEMENT, DIRECTION_ELEMENT, DIRECTION_ELEMENT);
    }

    private List<DirectionConfiguration> testDirectionConfigurations() {
        return List.of(
            DirectionConfiguration.builder()
                .title(DIRECTION_TYPE_1)
                .text(DIRECTION_TEXT_1)
                .assignee(ALL_PARTIES)
                .display(
                    Display.builder()
                        .due(ON)
                        .delta("-3")
                        .templateDateFormat("d MMMM yyyy 'at' h:mma")
                        .directionRemovable(false)
                        .showDateOnly(true)
                        .time("16:00:00")
                        .build())
                .build(),
            DirectionConfiguration.builder()
                .title(DIRECTION_TYPE_2)
                .text(DIRECTION_TEXT_2)
                .assignee(LOCAL_AUTHORITY)
                .display(
                    Display.builder()
                        .due(BY)
                        .delta("-2")
                        .templateDateFormat("h:mma, d MMMM yyyy")
                        .directionRemovable(false)
                        .showDateOnly(false)
                        .time("15:00:00")
                        .build())
                .build(),
            DirectionConfiguration.builder()
                .title(DIRECTION_TYPE_3)
                .text(DIRECTION_TEXT_3)
                .assignee(LOCAL_AUTHORITY)
                .display(
                    Display.builder()
                        .due(BY)
                        .delta("0")
                        .templateDateFormat("h:mma, d MMMM yyyy")
                        .directionRemovable(true)
                        .showDateOnly(false)
                        .build())
                .build()
        );
    }







    private HearingBooking hearingOnDateAtMidday(LocalDate hearingDate) {
        return HearingBooking.builder().startDate(hearingDate.atTime(12, 0, 0)).build();
    }

//
//    @Test
//    void shouldAddDirectionCompleteByDateBeforeWeekendWhenDeltaLandsOnWeekendDay() {
//        callbackRequest = getCallbackRequestWithCustomHearingOnMonday();
//
//        given(startPopulateStandardDirectionsEvent()).willReturn(getStartEventResponse(callbackRequest));
//
//        // delta of -2 would result in Saturday complete by date.
//        given(ordersLookupService.getStandardDirectionOrder())
//            .willReturn(getOrderDefinition(DIRECTION_TEXT, TWO_DAYS_BEFORE_HEARING));
//
//        handler.populateStandardDirections(new PopulateStandardDirectionsEvent(callbackRequest, requestData));
//
//        verifyCoreCaseDataApiIsCalledWithCorrectParameters();
//
//        List<Direction> directions = localAuthorityDirections(getCaseData());
//        // ignoring Saturday and Sunday and taking into account delta = -2 results in 4.
//        assertThat(directions).containsOnly(expectedDirection(DIRECTION_TEXT, MONDAY_DATE_AT_NOON.minusDays(4)));
//        assertThat(directions.get(0).getDateToBeCompletedBy().getDayOfWeek()).isEqualTo(DayOfWeek.THURSDAY);
//    }
//
//    @Test
//    void shouldPrepopulateDirectionsCorrectlyWhenDifferentDeltaValues() {
//        callbackRequest = getCallbackRequestWithCustomHearingOnMonday();
//
//        given(startPopulateStandardDirectionsEvent()).willReturn(getStartEventResponse(callbackRequest));
//
//        // delta of -2 and -3
//        given(ordersLookupService.getStandardDirectionOrder())
//            .willReturn(getOrderDefinitionContainingTwoDirectionsWithDifferentDeltas());
//
//        handler.populateStandardDirections(new PopulateStandardDirectionsEvent(callbackRequest, requestData));
//
//        verifyCoreCaseDataApiIsCalledWithCorrectParameters();
//
//        List<Direction> directions = localAuthorityDirections(getCaseData());
//
//        assertThat(directions).containsOnly(
//            expectedDirection(DIRECTION_TEXT, MONDAY_DATE_AT_NOON.minusDays(4)),
//            expectedDirection(DIRECTION_TEXT, MONDAY_DATE_AT_NOON.minusDays(5)));
//    }
//
//    @Test
//    void shouldPopulateStandardDirectionsWhenPopulatedDisplayInConfiguration() {
//        callbackRequest = getCallbackRequestWithCustomHearingOnMonday();
//
//        given(startPopulateStandardDirectionsEvent()).willReturn(getStartEventResponse(callbackRequest));
//
//        given(ordersLookupService.getStandardDirectionOrder())
//            .willReturn(getOrderDefinition(DIRECTION_TEXT, SAME_DAY_AS_HEARING));
//
//        handler.populateStandardDirections(new PopulateStandardDirectionsEvent(callbackRequest, requestData));
//
//        verifyCoreCaseDataApiIsCalledWithCorrectParameters();
//
//        assertThat(caseDataContent.getValue()).isEqualTo(expectedCaseDataContent(callbackRequest));
//        assertThat(localAuthorityDirections(getCaseData()))
//            .containsOnly(expectedDirection(DIRECTION_TEXT, MONDAY_DATE_AT_NOON));
//    }
//
//    @Test
//    void shouldPopulateStandardDirectionsWhenNullDeltaValueInConfiguration() {
//        given(startPopulateStandardDirectionsEvent()).willReturn(getStartEventResponse(callbackRequest));
//
//        given(ordersLookupService.getStandardDirectionOrder())
//            .willReturn(getOrderDefinition(DIRECTION_TEXT, null));
//
//        handler.populateStandardDirections(new PopulateStandardDirectionsEvent(callbackRequest, requestData));
//
//        verifyCoreCaseDataApiIsCalledWithCorrectParameters();
//
//        assertThat(caseDataContent.getValue()).isEqualTo(expectedCaseDataContent(callbackRequest));
//        assertThat(localAuthorityDirections(getCaseData()))
//            .containsOnly(expectedDirection(DIRECTION_TEXT, null));
//    }
//
//    @Test
//    void shouldPopulateStandardDirectionsWhenTextContainsSpecialCharacters() {
//        callbackRequest = getCallbackRequestWithCustomHearingOnMonday();
//
//        given(startPopulateStandardDirectionsEvent()).willReturn(getStartEventResponse(callbackRequest));
//
//        given(ordersLookupService.getStandardDirectionOrder())
//            .willReturn(getOrderDefinition(DIRECTION_TEXT_SPECIAL_CHARACTERS, SAME_DAY_AS_HEARING));
//
//        handler.populateStandardDirections(new PopulateStandardDirectionsEvent(callbackRequest, requestData));
//
//        verifyCoreCaseDataApiIsCalledWithCorrectParameters();
//
//        assertThat(localAuthorityDirections(getCaseData()))
//            .containsOnly(expectedDirection(DIRECTION_TEXT_SPECIAL_CHARACTERS, MONDAY_DATE_AT_NOON));
//    }
//
//    //TODO: this test just asserts previous functionality. To be looked into in FPLA-1516.
//    @Test
//    void shouldAddNoCompleteByDateWhenNoHearings() {
//        callbackRequest.getCaseDetails().getData().remove("hearingDetails");
//
//        given(startPopulateStandardDirectionsEvent()).willReturn(getStartEventResponse(callbackRequest));
//
//        given(ordersLookupService.getStandardDirectionOrder())
//            .willReturn(getOrderDefinition(DIRECTION_TEXT_SPECIAL_CHARACTERS, SAME_DAY_AS_HEARING));
//
//        handler.populateStandardDirections(new PopulateStandardDirectionsEvent(callbackRequest, requestData));
//
//        verifyCoreCaseDataApiIsCalledWithCorrectParameters();
//
//        assertThat(localAuthorityDirections(getCaseData()))
//            .containsOnly(expectedDirection(DIRECTION_TEXT_SPECIAL_CHARACTERS, null));
//    }
//
//    @Test
//    void shouldDefaultToBeginningOfTheDayWhenNoTimeSpecifiedInDirectionConfig() {
//        callbackRequest = getCallbackRequestWithCustomHearingOnMonday();
//
//        given(startPopulateStandardDirectionsEvent()).willReturn(getStartEventResponse(callbackRequest));
//
//        given(ordersLookupService.getStandardDirectionOrder()).willReturn(getOrderDefinition(null));
//
//        handler.populateStandardDirections(new PopulateStandardDirectionsEvent(callbackRequest, requestData));
//
//        verifyCoreCaseDataApiIsCalledWithCorrectParameters();
//
//        assertThat(localAuthorityDirections(getCaseData()))
//            .containsOnly(expectedDirection(DIRECTION_TEXT, MONDAY_DATE.atStartOfDay()));
//
//    }
//
//    private StartEventResponse startPopulateStandardDirectionsEvent() {
//        return coreCaseDataApi.startEventForCaseWorker(
//            TOKEN, AUTH_TOKEN, USER_ID, JURISDICTION, CASE_TYPE, CASE_ID, CASE_EVENT);
//    }
//
//    private OrderDefinition getOrderDefinition(String text, String delta) {
//        return OrderDefinition.builder()
//            .directions(ImmutableList.of(directionConfiguration(text, delta)))
//            .build();
//    }
//
//    private OrderDefinition getOrderDefinition(String time) {
//        return OrderDefinition.builder()
//            .directions(ImmutableList.of(directionConfiguration(time)))
//            .build();
//    }
//
//    private CallbackRequest getCallbackRequestWithCustomHearingOnMonday() {
//        return CallbackRequest.builder()
//            .caseDetails(CaseDetails.builder()
//                .id(Long.parseLong(CASE_ID))
//                .jurisdiction(JURISDICTION)
//                .caseTypeId(CASE_TYPE)
//                .data(getCaseDetailsWithMondayHearing())
//                .build())
//            .build();
//    }
//
//    private Map<String, Object> getCaseDetailsWithMondayHearing() {
//        CaseData caseData = CaseData.builder()
//            .hearingDetails(wrapElements(HearingBooking.builder()
//                .startDate(MONDAY_DATE_AT_NOON)
//                .build()))
//            .build();
//
//        return mapper.convertValue(caseData, new TypeReference<>() {
//        });
//    }
//
//    private StartEventResponse getStartEventResponse(CallbackRequest callbackRequest) {
//        return StartEventResponse.builder()
//            .caseDetails(callbackRequest.getCaseDetails())
//            .eventId(CASE_EVENT)
//            .token(TOKEN)
//            .build();
//    }
//
//    private CaseDataContent expectedCaseDataContent(CallbackRequest callbackRequest) {
//        return CaseDataContent.builder()
//            .eventToken(TOKEN)
//            .event(Event.builder()
//                .id(CASE_EVENT)
//                .build())
//            .data(callbackRequest.getCaseDetails().getData())
//            .build();
//    }
//
//    private void verifyCoreCaseDataApiIsCalledWithCorrectParameters() {
//        verify(coreCaseDataApi).submitEventForCaseWorker(
//            eq(TOKEN),
//            eq(AUTH_TOKEN),
//            eq(USER_ID),
//            eq(JURISDICTION),
//            eq(CASE_TYPE),
//            eq(CASE_ID),
//            eq(true),
//            caseDataContent.capture());
//    }
//
//    private CaseData getCaseData() {
//        return mapper.convertValue(caseDataContent.getValue().getData(), CaseData.class);
//    }
//
//    private List<Direction> localAuthorityDirections(CaseData caseData) {
//        return unwrapElements(caseData.getLocalAuthorityDirections());
//    }
//
//    private Direction expectedDirection(String directionText, LocalDateTime localDateTime) {
//        return Direction.builder()
//            .directionType(DIRECTION_TITLE)
//            .directionText(directionText)
//            .assignee(LOCAL_AUTHORITY)
//            .readOnly("No")
//            .directionRemovable("No")
//            .directionNeeded("Yes")
//            .dateToBeCompletedBy(localDateTime)
//            .build();
//    }
//
//    private OrderDefinition getOrderDefinitionContainingTwoDirectionsWithDifferentDeltas() {
//        return OrderDefinition.builder()
//            .directions(ImmutableList.of(
//                directionConfiguration(DIRECTION_TEXT, TWO_DAYS_BEFORE_HEARING),
//                directionConfiguration(DIRECTION_TEXT, THREE_DAYS_BEFORE_HEARING)))
//            .build();
//    }
//
//    private DirectionConfiguration directionConfiguration(String directionText, String deltaValue) {
//        return DirectionConfiguration.builder()
//            .assignee(LOCAL_AUTHORITY)
//            .title(DIRECTION_TITLE)
//            .text(directionText)
//            .display(Display.builder()
//                .delta(deltaValue)
//                .due(Display.Due.BY)
//                .templateDateFormat("h:mma, d MMMM yyyy")
//                .directionRemovable(false)
//                .time("12:00:00")
//                .build())
//            .build();
//    }
//
//    private DirectionConfiguration directionConfiguration(String time) {
//        return DirectionConfiguration.builder()
//            .assignee(LOCAL_AUTHORITY)
//            .title(DIRECTION_TITLE)
//            .text(DIRECTION_TEXT)
//            .display(Display.builder()
//                .delta(SAME_DAY_AS_HEARING)
//                .due(Display.Due.BY)
//                .templateDateFormat("h:mma, d MMMM yyyy")
//                .directionRemovable(false)
//                .time(time)
//                .build())
//            .build();
//    }

}
