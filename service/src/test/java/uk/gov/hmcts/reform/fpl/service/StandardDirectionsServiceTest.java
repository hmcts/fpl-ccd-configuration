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

    private final List<DirectionConfiguration> directionConfigurations = testDirectionConfigurations();

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
            .directions(directionConfigurations)
            .build());
        given(calendarService.getWorkingDayFrom(any(), anyInt())).willReturn(LocalDate.of(2040, 1, 1));
        given(commonDirectionService.constructDirectionForCCD(any(), any())).willReturn(DIRECTION_ELEMENT);
    }

    @Test
    void shouldReturnDirectionsWhenThereIsNoHearing() {
        List<Element<Direction>> directions = service.getDirections(null);

        assertThat(directions).containsExactly(DIRECTION_ELEMENT, DIRECTION_ELEMENT, DIRECTION_ELEMENT);
        verify(ordersLookupService).getStandardDirectionOrder();
        verify(calendarService, never()).getWorkingDayFrom(any(), anyInt());
        verify(commonDirectionService).constructDirectionForCCD(directionConfigurations.get(0), null);
        verify(commonDirectionService).constructDirectionForCCD(directionConfigurations.get(1), null);
        verify(commonDirectionService).constructDirectionForCCD(directionConfigurations.get(2), null);
        verifyNoMoreInteractions(commonDirectionService);
    }

    @Test
    void shouldReturnDirectionsWhenThereIsNoHearingDate() {
        List<Element<Direction>> directions = service.getDirections(HearingBooking.builder().build());

        assertThat(directions).containsExactly(DIRECTION_ELEMENT, DIRECTION_ELEMENT, DIRECTION_ELEMENT);
        verify(ordersLookupService).getStandardDirectionOrder();
        verify(calendarService, never()).getWorkingDayFrom(any(), anyInt());
        verify(commonDirectionService).constructDirectionForCCD(directionConfigurations.get(0), null);
        verify(commonDirectionService).constructDirectionForCCD(directionConfigurations.get(1), null);
        verify(commonDirectionService).constructDirectionForCCD(directionConfigurations.get(2), null);
        verifyNoMoreInteractions(commonDirectionService);
    }

    @Test
    void shouldReturnDirectionsWhenThereIsHearingDate() {
        LocalDate date = LocalDate.of(2099, 6, 1);
        HearingBooking hearingBooking = HearingBooking.builder().startDate(date.atTime(12, 0, 0)).build();
        given(calendarService.getWorkingDayFrom(date,-3)).willReturn(date.minusDays(3));
        given(calendarService.getWorkingDayFrom(date,-2)).willReturn(date.minusDays(2));
        given(calendarService.getWorkingDayFrom(date,0)).willReturn(date);

        List<Element<Direction>> directions = service.getDirections(hearingBooking);

        assertThat(directions).containsExactly(DIRECTION_ELEMENT, DIRECTION_ELEMENT, DIRECTION_ELEMENT);
        verify(ordersLookupService).getStandardDirectionOrder();
        verify(calendarService).getWorkingDayFrom(date, -3);
        verify(calendarService).getWorkingDayFrom(date, -2);
        verifyNoMoreInteractions(calendarService);
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
}
