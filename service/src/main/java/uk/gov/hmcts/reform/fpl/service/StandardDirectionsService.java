package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionTemplate;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.configuration.DirectionConfiguration;
import uk.gov.hmcts.reform.fpl.model.configuration.Display;
import uk.gov.hmcts.reform.fpl.service.calendar.CalendarService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static java.lang.Integer.parseInt;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.model.Directions.getAssigneeToDirectionMapping;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class StandardDirectionsService {
    private final CalendarService calendarService;
    private final OrdersLookupService ordersLookupService;

    public boolean hasEmptyDates(CaseData caseData) {
        return Stream.of(caseData.getAllParties(),
            caseData.getLocalAuthorityDirections(),
            caseData.getRespondentDirections(),
            caseData.getCafcassDirections(),
            caseData.getOtherPartiesDirections(),
            caseData.getCourtDirections())
            .flatMap(Collection::stream)
            .map(Element::getValue)
            .map(StandardDirectionTemplate::getDateToBeCompletedBy)
            .anyMatch(Objects::isNull);
    }

    public boolean hasEmptyDirections(CaseData caseData) {
        return Stream.of(caseData.getAllParties(),
            caseData.getLocalAuthorityDirections(),
            caseData.getRespondentDirections(),
            caseData.getCafcassDirections(),
            caseData.getOtherPartiesDirections(),
            caseData.getCourtDirections())
            .allMatch(ObjectUtils::isEmpty);
    }

    public Map<String, List<Element<StandardDirectionTemplate>>> populateStandardDirections(CaseData caseData) {
        List<Element<StandardDirectionTemplate>> directions = getDirections(caseData.getFirstHearingOfType(CASE_MANAGEMENT));

        return getAssigneeToDirectionMapping(directions).entrySet().stream()
            .collect(toMap(pair -> pair.getKey().getValue(), Map.Entry::getValue));
    }

    public List<Element<StandardDirectionTemplate>> getDirections(HearingBooking hearingBooking) {
        return getDirections(Optional.ofNullable(hearingBooking));
    }

    private List<Element<StandardDirectionTemplate>> getDirections(Optional<HearingBooking> hearingBooking) {
        LocalDateTime hearingStartDate = hearingBooking.map(HearingBooking::getStartDate).orElse(null);

        return ordersLookupService.getStandardDirectionOrder().getDirections()
            .stream()
            .map(configuration -> constructDirectionForCCD(hearingStartDate, configuration))
            .collect(toList());
    }

    private Element<StandardDirectionTemplate> constructDirectionForCCD(LocalDateTime hearingDate, DirectionConfiguration direction) {
        LocalDateTime dateToBeCompletedBy = ofNullable(hearingDate)
            .map(date -> getCompleteByDate(date, direction.getDisplay()))
            .orElse(null);

        return element(StandardDirectionTemplate.builder()
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
