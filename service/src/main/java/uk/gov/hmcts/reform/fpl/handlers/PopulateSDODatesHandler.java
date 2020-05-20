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
import uk.gov.hmcts.reform.fpl.enums.DirectionAssignee;
import uk.gov.hmcts.reform.fpl.events.PopulateSDODatesEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.CommonDirectionService;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;
import uk.gov.hmcts.reform.fpl.service.StandardDirectionsService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class PopulateSDODatesHandler {
    private final CoreCaseDataService coreCaseDataService;
    private final StandardDirectionsService standardDirectionsService;
    private final CommonDirectionService commonDirectionService;
    private final ObjectMapper mapper;
    private final HearingBookingService hearingBookingService;

    @Async
    @EventListener
    public void populateSDODates(PopulateSDODatesEvent event) {
        CaseDetails caseDetails = event.getCallbackRequest().getCaseDetails();

        getFirstHearing(caseDetails).ifPresent(firstHearing ->
        {
            try {
                coreCaseDataService.triggerEvent(caseDetails.getJurisdiction(),
                    caseDetails.getCaseTypeId(),
                    caseDetails.getId(),
                    "populateSDO",
                    populateDates(firstHearing, caseDetails.getData()));
            } catch (IOException e) {
                //TODO: this exception should be handled in JsonOrdersLookupService
            }
        });
    }

    private Optional<HearingBooking> getFirstHearing(CaseDetails caseDetails) {
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        return hearingBookingService.getFirstHearing(caseData.getHearingDetails());
    }

    private Map<String, Object> populateDates(HearingBooking hearingBooking, Map<String, Object> data) throws IOException {
        List<Element<Direction>> standardDirections = standardDirectionsService.getDirections(hearingBooking);

        commonDirectionService.sortDirectionsByAssignee(standardDirections).forEach(
            (assignee, directionsConfigForAssignee) -> populateEmptyDates(data, assignee, directionsConfigForAssignee));

        return data;
    }

    private void populateEmptyDates(Map<String, Object> data, DirectionAssignee assignee,
                                    List<Element<Direction>> directionsConfigForAssignee) {
        List<Element<Direction>> directionsForAssignee = mapper.convertValue(data.get(assignee.getValue()),
            new TypeReference<>() {});
        for (int directionIndex = 0; directionIndex < directionsForAssignee.size(); directionIndex++) {
            var direction = directionsForAssignee.get(directionIndex).getValue();
            if (direction.getDateToBeCompletedBy() == null) {
                direction.setDateToBeCompletedBy(directionsConfigForAssignee.get(directionIndex)
                    .getValue()
                    .getDateToBeCompletedBy());
            }
        }
        data.put(assignee.getValue(), directionsForAssignee);
    }
}
