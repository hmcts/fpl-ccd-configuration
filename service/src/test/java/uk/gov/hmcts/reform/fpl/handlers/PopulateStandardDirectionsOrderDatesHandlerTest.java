package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.events.PopulateStandardDirectionsOrderDatesEvent;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;
import uk.gov.hmcts.reform.fpl.service.StandardDirectionsService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.PARENTS_AND_RESPONDENTS;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class, PopulateStandardDirectionsOrderDatesHandler.class})
class PopulateStandardDirectionsOrderDatesHandlerTest {
    private static final String CASE_EVENT = "populateSDO";
    private static final Long CASE_ID = 12345L;
    private static final List<Element<HearingBooking>> HEARING_DETAILS = wrapElements(HearingBooking.builder()
        .type(CASE_MANAGEMENT)
        .build());
    private static final HearingBooking FIRST_HEARING = HearingBooking.builder()
        .startDate(LocalDateTime.of(2050, 10, 6, 13, 0))
        .build();
    private static final List<Element<Direction>> STANDARD_DIRECTIONS = wrapElements(
        Direction.builder()
            .assignee(ALL_PARTIES)
            .dateToBeCompletedBy(LocalDateTime.of(2050, 6, 10, 16, 0))
            .build(),
        Direction.builder()
            .assignee(LOCAL_AUTHORITY)
            .dateToBeCompletedBy(LocalDateTime.of(2050, 6, 8, 14, 0))
            .build(),
        Direction.builder()
            .assignee(PARENTS_AND_RESPONDENTS)
            .dateToBeCompletedBy(LocalDateTime.of(2050, 6, 5, 11, 0))
            .build()
    );

    @Autowired
    private PopulateStandardDirectionsOrderDatesHandler handler;

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @MockBean
    private StandardDirectionsService standardDirectionsService;

    @MockBean
    private HearingBookingService hearingBookingService;

    @MockBean
    private RequestData requestData;

    @Captor
    private ArgumentCaptor<Map<String, Object>> data;

    private CallbackRequest callbackRequest;

    @BeforeEach
    void setup() {
        given(standardDirectionsService.getDirections(any())).willReturn(STANDARD_DIRECTIONS);
        given(hearingBookingService.getFirstHearing(any())).willReturn(Optional.of(FIRST_HEARING));

        callbackRequest = getCallbackRequest();
    }

    @Test
    void shouldTriggerEventWithCaseDataFilledWithDates() {
        handler.populateDates(new PopulateStandardDirectionsOrderDatesEvent(callbackRequest));
        verify(hearingBookingService).getFirstHearing(HEARING_DETAILS);
        verify(standardDirectionsService).getDirections(FIRST_HEARING);
        verify(coreCaseDataService).triggerEvent(
            eq(JURISDICTION),
            eq(CASE_TYPE),
            eq(CASE_ID),
            eq(CASE_EVENT),
            data.capture());

        assertThat(data.getValue()).isEqualTo(getExpectedData());
    }

    @Test
    void shouldFillCaseDataWithMissingDatesOnly() {
        callbackRequest.getCaseDetails().setData(getDataWithSomeDatesFilled());
        handler.populateDates(new PopulateStandardDirectionsOrderDatesEvent(callbackRequest));
        verify(hearingBookingService).getFirstHearing(HEARING_DETAILS);
        verify(standardDirectionsService).getDirections(FIRST_HEARING);
        verify(coreCaseDataService).triggerEvent(
            eq(JURISDICTION),
            eq(CASE_TYPE),
            eq(CASE_ID),
            eq(CASE_EVENT),
            data.capture());

        assertThat(data.getValue()).isEqualTo(getExpectedDataWithUnchangedPreexistingDates());
    }

    private CallbackRequest getCallbackRequest() {
        return CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(CASE_ID)
                .jurisdiction(JURISDICTION)
                .caseTypeId(CASE_TYPE)
                .data(new HashMap<>(Map.of(
                    "hearingDetails", HEARING_DETAILS,
                    ALL_PARTIES.getValue(), wrapElements(
                        Direction.builder().directionText("allParties1").build()),
                    LOCAL_AUTHORITY.getValue(), wrapElements(
                        Direction.builder().directionText("LA2").build()),
                    PARENTS_AND_RESPONDENTS.getValue(), wrapElements(
                        Direction.builder().directionText("P&R3").build()))))
                .build())
            .build();
    }

    private Map<String, Object> getExpectedData() {
        return Map.of(
            "hearingDetails", HEARING_DETAILS,
            ALL_PARTIES.getValue(), wrapElements(
                Direction.builder()
                    .directionText("allParties1")
                    .dateToBeCompletedBy(LocalDateTime.of(2050, 6, 10, 16, 0))
                    .build()),
            LOCAL_AUTHORITY.getValue(), wrapElements(
                Direction.builder()
                    .directionText("LA2")
                    .dateToBeCompletedBy(LocalDateTime.of(2050, 6, 8, 14, 0))
                    .build()),
            PARENTS_AND_RESPONDENTS.getValue(), wrapElements(
                Direction.builder()
                    .directionText("P&R3")
                    .dateToBeCompletedBy(LocalDateTime.of(2050, 6, 5, 11, 0))
                    .build()));
    }

    private Map<String, Object> getDataWithSomeDatesFilled() {
        return new HashMap<>(Map.of(
            "hearingDetails", HEARING_DETAILS,
            ALL_PARTIES.getValue(), wrapElements(
                Direction.builder()
                    .directionText("allParties1")
                    .dateToBeCompletedBy(LocalDateTime.of(2066, 1, 1, 11, 0))
                    .build()),
            LOCAL_AUTHORITY.getValue(), wrapElements(
                Direction.builder()
                    .directionText("LA2")
                    .dateToBeCompletedBy(LocalDateTime.of(2066, 2, 2, 12, 0))
                    .build()),
            PARENTS_AND_RESPONDENTS.getValue(), wrapElements(
                Direction.builder()
                    .directionText("P&R3")
                    .dateToBeCompletedBy(LocalDateTime.of(2066, 3, 3, 13, 0))
                    .build())));
    }

    private Map<String, Object> getExpectedDataWithUnchangedPreexistingDates() {
        return Map.of(
            "hearingDetails", HEARING_DETAILS,
            ALL_PARTIES.getValue(), wrapElements(
                Direction.builder()
                    .directionText("allParties1")
                    .dateToBeCompletedBy(LocalDateTime.of(2066, 1, 1, 11, 0))
                    .build()),
            LOCAL_AUTHORITY.getValue(), wrapElements(
                Direction.builder()
                    .directionText("LA2")
                    .dateToBeCompletedBy(LocalDateTime.of(2066, 2, 2, 12, 0))
                    .build()),
            PARENTS_AND_RESPONDENTS.getValue(), wrapElements(
                Direction.builder()
                    .directionText("P&R3")
                    .dateToBeCompletedBy(LocalDateTime.of(2066, 3, 3, 13, 0))
                    .build()));
    }
}
