package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.DirectionAssignee;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Order;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.configuration.DirectionConfiguration;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisDirection;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisJudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisStandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.service.calendar.CalendarService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.lang.Integer.parseInt;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class StandardDirectionOrderPopulationService {
    private final ObjectMapper mapper;
    private final OrdersLookupService ordersLookupService;
    private final CommonDirectionService commonDirectionService;
    private final HearingBookingService hearingBookingService;
    private final CalendarService calendarService;


    public Map<String, Object> populateDates(CaseDetails caseDetails) throws IOException {
        Map<String, Object> data = caseDetails.getData();
        List<Element<Direction>> directionsConfig = getDirectionsConfiguration(caseDetails);

        commonDirectionService.sortDirectionsByAssignee(directionsConfig).forEach(
            (assignee, directionsConfigForAssignee) -> populateEmptyDates(data, assignee, directionsConfigForAssignee));

        return data;
    }

    public Map<String, Object> populateStandardDirections(CallbackRequest callbackRequest) throws IOException {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        List<Element<Direction>> directionsConfig = getDirectionsConfiguration(caseDetails);

        commonDirectionService.sortDirectionsByAssignee(directionsConfig)
            .forEach((directionAssignee, directionsElements) ->
                caseDetails.getData().put(directionAssignee.getValue(), directionsElements));

        return caseDetails.getData();
    }

    public boolean hearingDetailsAdded(CaseDetails caseDetails) {
        return getFirstHearingStartDate(caseDetails).isPresent();
    }

    private Optional<LocalDateTime> getFirstHearingStartDate(CaseDetails caseDetails) {
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        return hearingBookingService.getFirstHearing(caseData.getHearingDetails())
            .map(HearingBooking::getStartDate);
    }

    private  List<Element<Direction>> getDirectionsConfiguration(CaseDetails caseDetails) throws IOException {
        return ordersLookupService.getStandardDirectionOrder().getDirections()
            .stream()
            .map(configuration -> getDirectionElement(configuration, caseDetails))
            .collect(toList());
    }

    private Element<Direction> getDirectionElement(DirectionConfiguration config, CaseDetails caseDetails) {
        var completeBy = getFirstHearingStartDate(caseDetails)
            .map(startDate -> getCompleteByDate(startDate, config))
            .orElse(null);

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

    private void populateEmptyDates(Map<String, Object> data, DirectionAssignee assignee,
                                    List<Element<Direction>> directionsConfigForAssignee) {
        List<Element<Direction>> directionsData = mapper.convertValue(data.get(assignee.getValue()), new TypeReference<>() {});
        for (int directionIndex = 0; directionIndex < directionsData.size(); directionIndex++) {
            var direction = directionsData.get(directionIndex).getValue();
            if (direction.getDateToBeCompletedBy() == null) {
                direction.setDateToBeCompletedBy(directionsConfigForAssignee.get(directionIndex).getValue().getDateToBeCompletedBy());
            }
        }
        data.put(assignee.getValue(), directionsData);
    }
}
