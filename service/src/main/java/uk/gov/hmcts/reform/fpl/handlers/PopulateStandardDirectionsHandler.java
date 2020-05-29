package uk.gov.hmcts.reform.fpl.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.events.PopulateStandardDirectionsEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;
import uk.gov.hmcts.reform.fpl.service.StandardDirectionsService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;

import java.util.Map;

import static uk.gov.hmcts.reform.fpl.model.Directions.getMapping;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PopulateStandardDirectionsHandler {
    private final CoreCaseDataService coreCaseDataService;
    private final StandardDirectionsService standardDirectionsService;
    private final ObjectMapper mapper;
    private final HearingBookingService hearingService;

    @Async
    @EventListener
    public void populateStandardDirections(PopulateStandardDirectionsEvent event) {
        CaseDetails caseDetails = event.getCallbackRequest().getCaseDetails();

        coreCaseDataService.triggerEvent(caseDetails.getJurisdiction(),
            caseDetails.getCaseTypeId(),
            caseDetails.getId(),
            "populateSDO",
            populateStandardDirections(caseDetails.getData()));
    }

    private Map<String, Object> populateStandardDirections(Map<String, Object> data) {
        CaseData caseData = mapper.convertValue(data, CaseData.class);
        HearingBooking hearingBooking = hearingService.getFirstHearing(caseData.getHearingDetails()).orElse(null);

        getMapping(standardDirectionsService.getDirections(hearingBooking))
            .forEach((assignee, directionsElements) -> data.put(assignee.getValue(), directionsElements));

        return data;
    }
}
