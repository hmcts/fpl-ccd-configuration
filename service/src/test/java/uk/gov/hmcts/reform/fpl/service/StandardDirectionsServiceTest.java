package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.calendar.CalendarService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    StandardDirectionsService.class, JsonOrdersLookupService.class, JacksonAutoConfiguration.class
})
class StandardDirectionsServiceTest {
    private static final String DIRECTION_TYPE_1 = "Test SDO type 1";
    private static final String DIRECTION_TEXT_1 = "- Test body 1 \n\n- Two\n";
    private static final String DIRECTION_TYPE_2 = "Test SDO type 2";
    private static final String DIRECTION_TEXT_2 = "Test body 2\n";
    private static final String DIRECTION_TYPE_3 = "Test SDO type 3";
    private static final String DIRECTION_TEXT_3 = "Test body 3\n";

    @MockBean
    private CalendarService calendarService;

    @Autowired
    private StandardDirectionsService service;

    @Test
    void shouldReturnExpectedListOfDirectionsWithPopulatedDatesWhenThereIsHearingDate() {
        LocalDate date = LocalDate.of(2099, 6, 1);
        given(calendarService.getWorkingDayFrom(eq(date), eq(-2))).willReturn(date.minusDays(2));
        given(calendarService.getWorkingDayFrom(eq(date), eq(-3))).willReturn(date.minusDays(3));

        List<Element<Direction>> directions = service.getDirections(hearingOnDateAtMidday(date));

        assertThat(unwrapElements(directions)).containsOnly(expectedDirections(date));
    }

    @Test
    void shouldReturnExpectedListOfDirectionsWithNullDatesWhenThereIsNoHearingDate() {
        List<Element<Direction>> directions = service.getDirections(null);

        assertThat(unwrapElements(directions)).containsOnly(expectedDirections(null));
    }

    @Test
    void shouldReturnTrueWhenThereAreEmptyDates() {
        CaseData caseData = CaseData.builder()
            .allParties(wrapElements(buildDirectionWithDate(), buildDirectionWithDate()))
            .localAuthorityDirections(wrapElements(buildDirectionWithDate(), buildDirectionWithDate()))
            .respondentDirections(wrapElements(buildDirectionWithDate(), buildDirectionWithDate()))
            .cafcassDirections(wrapElements(buildDirectionWithDate(), Direction.builder().build()))
            .otherPartiesDirections(wrapElements(buildDirectionWithDate(), buildDirectionWithDate()))
            .courtDirections(wrapElements(buildDirectionWithDate(), buildDirectionWithDate()))
            .build();

        assertThat(service.hasEmptyDates(caseData)).isTrue();
    }

    @Test
    void shouldReturnFalseWhenThereAreNoEmptyDates() {
        CaseData caseData = CaseData.builder()
            .allParties(wrapElements(buildDirectionWithDate(), buildDirectionWithDate()))
            .localAuthorityDirections(wrapElements(buildDirectionWithDate(), buildDirectionWithDate()))
            .respondentDirections(wrapElements(buildDirectionWithDate(), buildDirectionWithDate()))
            .cafcassDirections(wrapElements(buildDirectionWithDate(), buildDirectionWithDate()))
            .otherPartiesDirections(wrapElements(buildDirectionWithDate(), buildDirectionWithDate()))
            .courtDirections(wrapElements(buildDirectionWithDate(), buildDirectionWithDate()))
            .build();

        assertThat(service.hasEmptyDates(caseData)).isFalse();
    }

    @Test
    void shouldReturnFalseWhenCafcassDirectionsAreNull() {
        CaseData caseData = CaseData.builder()
            .allParties(wrapElements(buildDirectionWithDate(), buildDirectionWithDate()))
            .localAuthorityDirections(wrapElements(buildDirectionWithDate(), buildDirectionWithDate()))
            .respondentDirections(wrapElements(buildDirectionWithDate(), buildDirectionWithDate()))
            .cafcassDirections(null)
            .otherPartiesDirections(wrapElements(buildDirectionWithDate(), buildDirectionWithDate()))
            .courtDirections(wrapElements(buildDirectionWithDate(), buildDirectionWithDate()))
            .build();

        assertThat(service.hasEmptyDates(caseData)).isFalse();
    }

    private Direction buildDirectionWithDate() {
        return Direction.builder().dateToBeCompletedBy(LocalDateTime.now()).build();
    }

    private HearingBooking hearingOnDateAtMidday(LocalDate hearingDate) {
        return HearingBooking.builder().startDate(hearingDate.atTime(12, 0, 0)).build();
    }

    private Direction[] expectedDirections(LocalDate date) {
        Optional<LocalDate> hearingDate = ofNullable(date);

        return new Direction[] {Direction.builder()
            .assignee(ALL_PARTIES)
            .directionType(DIRECTION_TYPE_1)
            .directionText(DIRECTION_TEXT_1)
            .readOnly("Yes")
            .directionRemovable("No")
            .directionNeeded("Yes")
            .dateToBeCompletedBy(hearingDate.map(LocalDate::atStartOfDay).orElse(null))
            .build(),
            Direction.builder()
                .assignee(LOCAL_AUTHORITY)
                .directionType(DIRECTION_TYPE_2)
                .directionText(DIRECTION_TEXT_2)
                .readOnly("No")
                .directionRemovable("No")
                .directionNeeded("Yes")
                .dateToBeCompletedBy(hearingDate.map(x -> x.minusDays(3).atTime(12, 0, 0)).orElse(null))
                .build(),
            Direction.builder()
                .assignee(LOCAL_AUTHORITY)
                .directionType(DIRECTION_TYPE_3)
                .directionText(DIRECTION_TEXT_3)
                .readOnly("No")
                .directionRemovable("Yes")
                .directionNeeded("Yes")
                .dateToBeCompletedBy(hearingDate.map(x -> x.minusDays(2).atTime(16, 0, 0)).orElse(null))
                .build()};
    }
}
