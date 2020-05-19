package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.configuration.DirectionConfiguration;
import uk.gov.hmcts.reform.fpl.model.configuration.Display;
import uk.gov.hmcts.reform.fpl.model.configuration.OrderDefinition;
import uk.gov.hmcts.reform.fpl.service.calendar.CalendarService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static java.lang.Integer.parseInt;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ResourceReader.readString;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class JsonOrdersLookupService implements OrdersLookupService {
    private final ObjectMapper objectMapper;
    private final CalendarService calendarService;

    public OrderDefinition getStandardDirectionOrder() throws IOException {
        String content = readString("ordersConfig.json");

        return this.objectMapper.readValue(content, OrderDefinition.class);
    }

    public List<Element<Direction>> getStandardDirections(HearingBooking hearingBooking) throws IOException {
        LocalDateTime hearingStartDate = ofNullable(hearingBooking).map(HearingBooking::getStartDate).orElse(null);

        return getStandardDirectionOrder().getDirections()
            .stream()
            .map(configuration -> constructDirectionForCCD(hearingStartDate, configuration))
            .collect(toList());
    }

    private Element<Direction> constructDirectionForCCD(LocalDateTime hearingDate, DirectionConfiguration direction) {
        LocalDateTime dateToBeCompletedBy = ofNullable(hearingDate)
            .map(date -> getCompleteByDate(date, direction.getDisplay()))
            .orElse(null);

        return element(Direction.builder()
            .directionType(direction.getTitle())
            .directionText(direction.getText())
            .assignee(direction.getAssignee())
            .directionNeeded(YES.getValue())
            .directionRemovable(booleanToYesOrNo(direction.getDisplay().isDirectionRemovable()))
            .readOnly(booleanToYesOrNo(direction.getDisplay().isShowDateOnly()))
            .dateToBeCompletedBy(dateToBeCompletedBy)
            .build());
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

    private String booleanToYesOrNo(boolean value) {
        return value ? "Yes" : "No";
    }
}
