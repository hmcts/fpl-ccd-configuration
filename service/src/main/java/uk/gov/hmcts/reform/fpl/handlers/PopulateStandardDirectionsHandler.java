package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.events.PopulateStandardDirectionsEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.service.CaseConverter;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;
import uk.gov.hmcts.reform.fpl.service.StandardDirectionsService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;

import java.util.Map;

import static uk.gov.hmcts.reform.fpl.model.Directions.getAssigneeToDirectionMapping;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PopulateStandardDirectionsHandler {
    private final CoreCaseDataService coreCaseDataService;
    private final StandardDirectionsService standardDirectionsService;
    private final CaseConverter caseConverter;
    private final HearingBookingService hearingService;

    @Async
    @EventListener
    public void populateStandardDirections(PopulateStandardDirectionsEvent event) {
        CaseDetails caseDetails = event.getCallbackRequest().getCaseDetails();

        coreCaseDataService.triggerEvent(caseDetails.getJurisdiction(),
            caseDetails.getCaseTypeId(),
            caseDetails.getId(),
            "populateSDO",
            populateStandardDirections(caseDetails));
    }

    private Map<String, Object> populateStandardDirections(CaseDetails caseDetails) {
        CaseData caseData = caseConverter.convert(caseDetails);
        HearingBooking hearingBooking = hearingService.getFirstHearing(caseData.getHearingDetails()).orElse(null);

        getAssigneeToDirectionMapping(standardDirectionsService.getDirections(hearingBooking))
            .forEach((assignee, directionsElements) ->
                caseDetails.getData().put(assignee.getValue(), directionsElements));

        return caseDetails.getData();
    }
}
