package uk.gov.hmcts.reform.fpl.handlers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.DirectionAssignee;
import uk.gov.hmcts.reform.fpl.enums.HearingType;
import uk.gov.hmcts.reform.fpl.events.PopulateStandardDirectionsOrderDatesEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.StandardDirectionsService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.fpl.model.Directions.getAssigneeToDirectionMapping;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PopulateStandardDirectionsOrderDatesHandler {
    private final CoreCaseDataService coreCaseDataService;
    private final StandardDirectionsService standardDirectionsService;
    private final ObjectMapper mapper;

    @Async
    @EventListener
    public void populateDates(PopulateStandardDirectionsOrderDatesEvent event) {
        CaseDetails caseDetails = event.getCallbackRequest().getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        Optional<HearingBooking> hearing = caseData.getFirstHearingOfType(HearingType.CASE_MANAGEMENT);

        // only want to update dates if there is a case management hearing
        if (hearing.isPresent()) {
            Map<String, Object> updatedCaseData = getDataWithDates(hearing.get(), caseDetails.getData());

            coreCaseDataService.triggerEvent(caseDetails.getJurisdiction(),
                caseDetails.getCaseTypeId(),
                caseDetails.getId(),
                "populateSDO",
                updatedCaseData);
        }
    }

    private Map<String, Object> getDataWithDates(HearingBooking hearingBooking, Map<String, Object> data) {
        List<Element<Direction>> directions = standardDirectionsService.getDirections(hearingBooking);
        getAssigneeToDirectionMapping(directions).forEach((assignee, directionElements) -> {
            if (!directionElements.isEmpty()) {
                populateEmptyDates(data, assignee, directionElements);
            }
        });

        return data;
    }

    private void populateEmptyDates(Map<String, Object> data, DirectionAssignee assignee,
                                    List<Element<Direction>> configDirectionsForAssignee) {
        List<Element<Direction>> directionsForAssignee = mapper.convertValue(
            data.get(assignee.getValue()), new TypeReference<>() {}
        );

        for (int i = 0; i < directionsForAssignee.size(); i++) {
            var direction = directionsForAssignee.get(i).getValue();
            if (direction.getDateToBeCompletedBy() == null) {
                direction.setDateToBeCompletedBy(configDirectionsForAssignee.get(i)
                    .getValue()
                    .getDateToBeCompletedBy());
            }
        }
        data.put(assignee.getValue(), directionsForAssignee);
    }
}
