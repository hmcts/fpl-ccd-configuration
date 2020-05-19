package uk.gov.hmcts.reform.fpl.handlers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.events.PopulateSDODatesEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.configuration.DirectionConfiguration;
import uk.gov.hmcts.reform.fpl.service.CommonDirectionService;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;
import uk.gov.hmcts.reform.fpl.service.OrdersLookupService;
import uk.gov.hmcts.reform.fpl.service.calendar.CalendarService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static java.lang.Integer.parseInt;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class PopulateSDODatesHandler {
    private final ObjectMapper mapper;
    private final OrdersLookupService ordersLookupService;
    private final CommonDirectionService commonDirectionService;
    private final HearingBookingService hearingBookingService;
    private final CalendarService calendarService;
    private final CoreCaseDataService coreCaseDataService;

    @Async
    @EventListener
    public void populateSDODates(PopulateSDODatesEvent event) throws IOException {
        CaseDetails caseDetails = event.getCallbackRequest().getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);
        LocalDateTime hearingStartDate = getFirstHearingStartDate(caseData.getHearingDetails());
        if (hearingStartDate != null) {
                coreCaseDataService.triggerEvent(caseDetails.getJurisdiction(),
                    caseDetails.getCaseTypeId(),
                    caseDetails.getId(),
                    "populateSDO",
                    populateDates(hearingStartDate, caseDetails.getData()));
        }
    }

    private Map<String, Object> populateDates(LocalDateTime hearingStartDate, Map<String, Object> data) throws IOException {
        List<Element<Direction>> directionsConfig = ordersLookupService.getStandardDirectionOrder().getDirections()
            .stream()
            .map(configuration -> getDirectionElement(hearingStartDate, configuration))
            .collect(toList());

        commonDirectionService.sortDirectionsByAssignee(directionsConfig)
            .forEach((assignee, directionsConfigForAssignee) -> {
                List<Element<Direction>> directionsData = mapper.convertValue(data.get(assignee.getValue()), new TypeReference<>() {});
                for (int directionIndex = 0; directionIndex < directionsData.size(); directionIndex++) {
                    var direction = directionsData.get(directionIndex).getValue();
                    if (direction.getDateToBeCompletedBy() == null) {
                        direction.setDateToBeCompletedBy(directionsConfigForAssignee.get(directionIndex).getValue().getDateToBeCompletedBy());
                    }
                }
                data.put(assignee.getValue(), directionsData);
            });

        return data;
    }

    private LocalDateTime getFirstHearingStartDate(List<Element<HearingBooking>> hearings) {
        return hearingBookingService.getFirstHearing(hearings)
            .map(HearingBooking::getStartDate)
            .orElse(null);
    }

    private Element<Direction> getDirectionElement(LocalDateTime hearingStartDate, DirectionConfiguration config) {
        LocalDateTime completeBy = null;

        if (hearingStartDate != null) {
            completeBy = getCompleteByDate(hearingStartDate, config);
        }

        return commonDirectionService.constructDirectionForCCD(config, completeBy);
    }

    private LocalDateTime getCompleteByDate(LocalDateTime startDate, DirectionConfiguration direction) {
        return ofNullable(direction.getDisplay().getDelta())
            .map(delta -> addDelta(startDate, parseInt(delta)))
            .map(date -> getLocalDateTime(direction, date))
            .orElse(null);
    }

    private LocalDateTime getLocalDateTime(DirectionConfiguration direction, LocalDate date) {
        return ofNullable(direction.getDisplay().getTime())
            .map(time -> LocalDateTime.of(date, LocalTime.parse(time)))
            .orElse(date.atStartOfDay());
    }

    private LocalDate addDelta(LocalDateTime date, int delta) {
        if (delta == 0) {
            return date.toLocalDate();
        }
        return calendarService.getWorkingDayFrom(date.toLocalDate(), delta);
    }
}
