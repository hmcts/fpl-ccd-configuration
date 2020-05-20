package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.configuration.DirectionConfiguration;
import uk.gov.hmcts.reform.fpl.model.configuration.Display;
import uk.gov.hmcts.reform.fpl.model.configuration.OrderDefinition;
import uk.gov.hmcts.reform.fpl.service.calendar.CalendarService;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.model.configuration.Language.ENGLISH;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class})
class JsonOrdersLookUpServiceTest {
    private static final String DIRECTION_TYPE_1 = "Test SDO type 1";
    private static final String DIRECTION_TEXT_1 = "- Test body 1 \n\n- Two\n";
    private static final String DIRECTION_TYPE_2 = "Test SDO type 2";
    private static final String DIRECTION_TEXT_2 = "Test body 2\n";
    private static final String DIRECTION_TYPE_3 = "Test SDO type 3";
    private static final String DIRECTION_TEXT_3 = "Test body 3\n";
    private JsonOrdersLookupService service;

    @Mock
    private CalendarService calendarService;

    @Autowired
    private ObjectMapper mapper;

    @BeforeEach
    void setup() {
        service = new JsonOrdersLookupService(mapper, calendarService);
    }

    @Test
    void shouldPopulateOrderDefinitionForStandardDirectionOrder() throws IOException {
        OrderDefinition expectedOrderDefinition = OrderDefinition.builder()
            .type("standardDirectionOrder")
            .language(ENGLISH)
            .service("FPL")
            .directions(ImmutableList.of(
                DirectionConfiguration.builder()
                    .title(DIRECTION_TYPE_1)
                    .text(DIRECTION_TEXT_1)
                    .assignee(ALL_PARTIES)
                    .display(Display.builder()
                        .due(Display.Due.ON)
                        .templateDateFormat("d MMMM yyyy 'at' h:mma")
                        .directionRemovable(false)
                        .showDateOnly(true)
                        .delta("0")
                        .build())
                    .build(),
                DirectionConfiguration.builder()
                    .title(DIRECTION_TYPE_2)
                    .text(DIRECTION_TEXT_2)
                    .assignee(LOCAL_AUTHORITY)
                    .display(Display.builder()
                        .due(Display.Due.BY)
                        .templateDateFormat("h:mma, d MMMM yyyy")
                        .directionRemovable(false)
                        .showDateOnly(false)
                        .delta("-3")
                        .time("12:00:00")
                        .build())
                    .build(),
                DirectionConfiguration.builder()
                    .title(DIRECTION_TYPE_3)
                    .text(DIRECTION_TEXT_3)
                    .assignee(LOCAL_AUTHORITY)
                    .display(Display.builder()
                        .due(Display.Due.BY)
                        .templateDateFormat("h:mma, d MMMM yyyy")
                        .directionRemovable(true)
                        .showDateOnly(false)
                        .delta("-2")
                        .time("16:00:00")
                        .build())
                    .build()))
            .build();

        OrderDefinition orderDefinition = service.getStandardDirectionOrder();

        assertThat(orderDefinition).isEqualToComparingFieldByField(expectedOrderDefinition);
    }

    @Test
    void shouldReturnExpectedListOfDirectionsWithPopulatedDatesWhenThereIsHearingDate() throws IOException {
        LocalDate date = LocalDate.of(2099, 6, 1);
        given(calendarService.getWorkingDayFrom(eq(date), eq(-2))).willReturn(date.minusDays(2));
        given(calendarService.getWorkingDayFrom(eq(date), eq(-3))).willReturn(date.minusDays(3));

        List<Element<Direction>> directions = service.getStandardDirections(hearingOnDateAtMidday(date));

        assertThat(unwrapElements(directions)).containsOnly(expectedDirections(date));
    }

    @Test
    void shouldReturnExpectedListOfDirectionsWithNullDatesWhenThereIsNoHearingDate() throws IOException {
        List<Element<Direction>> directions = service.getStandardDirections(null);

        assertThat(unwrapElements(directions)).containsOnly(expectedDirections(null));
    }

    private HearingBooking hearingOnDateAtMidday(LocalDate hearingDate) {
        return HearingBooking.builder().startDate(hearingDate.atTime(12, 0, 0)).build();
    }

    private Direction[] expectedDirections(LocalDate date) {
        Optional<LocalDate> hearingDate = ofNullable(date);

        return new Direction[]{Direction.builder()
            .assignee(ALL_PARTIES)
            .directionType(DIRECTION_TYPE_1)
            .directionText(DIRECTION_TEXT_1)
            .readOnly("Yes")
            .directionRemovable("No")
            .directionNeeded("Yes")
            .dateToBeCompletedBy(hearingDate.map(LocalDate::atStartOfDay).orElse(null))
            .responses(emptyList())
            .build(),
            Direction.builder()
                .assignee(LOCAL_AUTHORITY)
                .directionType(DIRECTION_TYPE_2)
                .directionText(DIRECTION_TEXT_2)
                .readOnly("No")
                .directionRemovable("No")
                .directionNeeded("Yes")
                .dateToBeCompletedBy(hearingDate.map(x -> x.minusDays(3).atTime(12, 0, 0)).orElse(null))
                .responses(emptyList())
                .build(),
            Direction.builder()
                .assignee(LOCAL_AUTHORITY)
                .directionType(DIRECTION_TYPE_3)
                .directionText(DIRECTION_TEXT_3)
                .readOnly("No")
                .directionRemovable("Yes")
                .directionNeeded("Yes")
                .dateToBeCompletedBy(hearingDate.map(x -> x.minusDays(2).atTime(16, 0, 0)).orElse(null))
                .responses(emptyList())
                .build()};
    }
}
