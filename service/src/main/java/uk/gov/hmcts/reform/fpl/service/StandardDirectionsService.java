package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.DirectionAssignee;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.configuration.DirectionConfiguration;
import uk.gov.hmcts.reform.fpl.model.configuration.Display;
import uk.gov.hmcts.reform.fpl.service.calendar.CalendarService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static java.lang.Integer.parseInt;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class StandardDirectionsService {
    private final CalendarService calendarService;
    private final CommonDirectionService commonDirectionService;
    private final OrdersLookupService ordersLookupService;
    private final ObjectMapper mapper;

    public List<Element<Direction>> getDirections(HearingBooking hearingBooking) {
        LocalDateTime hearingStartDate = ofNullable(hearingBooking).map(HearingBooking::getStartDate).orElse(null);

        return ordersLookupService.getStandardDirectionOrder().getDirections()
            .stream()
            .map(configuration -> constructDirectionForCCD(hearingStartDate, configuration))
            .collect(toList());
    }

    public boolean hasEmptyDates(Map<String, Object> data) {
        return Stream.of(DirectionAssignee.values()).anyMatch(directionAssignee -> {
            var directionsForAssignee = Optional.of(data.get(directionAssignee.getValue()))
                .orElseThrow(IllegalStateException::new);
            List<Element<Direction>> directions = mapper.convertValue(directionsForAssignee, new TypeReference<>() {});

            return directions.stream()
                .map(Element::getValue)
                .map(Direction::getDateToBeCompletedBy)
                .anyMatch(Objects::isNull);
        });
    }

    private Element<Direction> constructDirectionForCCD(LocalDateTime hearingDate, DirectionConfiguration direction) {
        LocalDateTime dateToBeCompletedBy = ofNullable(hearingDate)
            .map(date -> getCompleteByDate(date, direction.getDisplay()))
            .orElse(null);

        return commonDirectionService.constructDirectionForCCD(direction, dateToBeCompletedBy);
    }

    private LocalDateTime getCompleteByDate(LocalDateTime startDate, Display display) {
        return ofNullable(display.getDelta())
            .map(delta -> addDelta(startDate, parseInt(delta)))
            .map(date -> getLocalDateTime(display.getTime(), date))
            .orElse(null);
    }

    private LocalDateTime getLocalDateTime(String time, LocalDate date) {
        return ofNullable(time).map(item -> LocalDateTime.of(date, LocalTime.parse(item))).orElse(date.atStartOfDay());
    }

    private LocalDate addDelta(LocalDateTime date, int delta) {
        if (delta == 0) {
            return date.toLocalDate();
        }
        return calendarService.getWorkingDayFrom(date.toLocalDate(), delta);
    }
}
