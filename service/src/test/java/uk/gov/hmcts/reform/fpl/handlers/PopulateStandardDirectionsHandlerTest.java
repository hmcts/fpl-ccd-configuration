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
import uk.gov.hmcts.reform.fpl.enums.DirectionAssignee;
import uk.gov.hmcts.reform.fpl.enums.HearingType;
import uk.gov.hmcts.reform.fpl.events.PopulateStandardDirectionsEvent;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;
import uk.gov.hmcts.reform.fpl.service.StandardDirectionsService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.CAFCASS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.COURT;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.OTHERS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.PARENTS_AND_RESPONDENTS;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class, PopulateStandardDirectionsHandler.class})
class PopulateStandardDirectionsHandlerTest {
    private static final String CASE_EVENT = "populateSDO";
    private static final Long CASE_ID = 12345L;
    private static final List<Element<HearingBooking>> HEARING_DETAILS = List.of(element(HearingBooking.builder()
        .type(HearingType.ISSUE_RESOLUTION)
        .build()));
    private static final HearingBooking FIRST_HEARING = HearingBooking.builder().type(
        HearingType.CASE_MANAGEMENT).build();
    private static final Element<Direction> ALL_PARTIES_DIRECTION =
        element(Direction.builder().assignee(ALL_PARTIES).build());
    private static final Element<Direction> LOCAL_AUTHORITY_DIRECTION =
        element(Direction.builder().assignee(LOCAL_AUTHORITY).build());
    private static final Element<Direction> RESPONDENT_DIRECTION =
        element(Direction.builder().assignee(PARENTS_AND_RESPONDENTS).build());

    private static final Map<DirectionAssignee, List<Element<Direction>>> DIRECTIONS_SORTED_BY_ASSIGNEE = Map.of(
        ALL_PARTIES, List.of(ALL_PARTIES_DIRECTION),
        LOCAL_AUTHORITY, List.of(LOCAL_AUTHORITY_DIRECTION),
        PARENTS_AND_RESPONDENTS, List.of(RESPONDENT_DIRECTION)
    );
    private static final List<Element<Direction>> STANDARD_DIRECTIONS = List.of(
        ALL_PARTIES_DIRECTION, LOCAL_AUTHORITY_DIRECTION, RESPONDENT_DIRECTION);

    @Autowired
    private PopulateStandardDirectionsHandler handler;

    @MockBean
    private HearingBookingService hearingBookingService;

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @MockBean
    private StandardDirectionsService standardDirectionsService;

    @MockBean
    private RequestData requestData;

    @Captor
    private ArgumentCaptor<Map<String, Object>> data;

    private CallbackRequest callbackRequest;

    @BeforeEach
    void setup() {
        given(hearingBookingService.getFirstHearing(any())).willReturn(Optional.of(FIRST_HEARING));
        given(standardDirectionsService.getDirections(any())).willReturn(STANDARD_DIRECTIONS);

        callbackRequest = getCallbackRequest();
    }

    @Test
    void shouldTriggerEventWithCorrectData() {
        handler.populateStandardDirections(new PopulateStandardDirectionsEvent(callbackRequest));

        verify(coreCaseDataService).triggerEvent(
            eq(JURISDICTION),
            eq(CASE_TYPE),
            eq(CASE_ID),
            eq(CASE_EVENT),
            data.capture());
        verify(hearingBookingService).getFirstHearing(HEARING_DETAILS);
        verify(standardDirectionsService).getDirections(FIRST_HEARING);
        assertThat(data.getValue()).isEqualTo(getExpectedData());
    }

    @Test
    void shouldCallStandardDirectionsServiceWithNullIfNoFirstHearing() {
        given(hearingBookingService.getFirstHearing(any())).willReturn(Optional.empty());

        handler.populateStandardDirections(new PopulateStandardDirectionsEvent(callbackRequest));

        verify(coreCaseDataService).triggerEvent(
            eq(JURISDICTION),
            eq(CASE_TYPE),
            eq(CASE_ID),
            eq(CASE_EVENT),
            data.capture());
        verify(hearingBookingService).getFirstHearing(HEARING_DETAILS);
        verify(standardDirectionsService).getDirections(null);
        assertThat(data.getValue()).isEqualTo(getExpectedData());
    }

    private CallbackRequest getCallbackRequest() {
        return CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(CASE_ID)
                .jurisdiction(JURISDICTION)
                .caseTypeId(CASE_TYPE)
                .data(new HashMap<>(Map.of("hearingDetails", HEARING_DETAILS)))
                .build())
            .build();
    }

    private Map<String, Object> getExpectedData() {
        return Map.of(
            "hearingDetails", HEARING_DETAILS,
            ALL_PARTIES.getValue(), DIRECTIONS_SORTED_BY_ASSIGNEE.get(ALL_PARTIES),
            LOCAL_AUTHORITY.getValue(), DIRECTIONS_SORTED_BY_ASSIGNEE.get(LOCAL_AUTHORITY),
            PARENTS_AND_RESPONDENTS.getValue(), DIRECTIONS_SORTED_BY_ASSIGNEE.get(PARENTS_AND_RESPONDENTS),
            CAFCASS.getValue(), emptyList(),
            COURT.getValue(), emptyList(),
            OTHERS.getValue(), emptyList());
    }
}
