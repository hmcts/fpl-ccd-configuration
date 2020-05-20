package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.events.PopulateSDODatesEvent;
import uk.gov.hmcts.reform.fpl.service.StandardDirectionOrderPopulationService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;

import java.io.IOException;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class PopulateSDODatesHandler {
    private final CoreCaseDataService coreCaseDataService;
    private final StandardDirectionOrderPopulationService standardDirectionOrderPopulationService;

    @Async
    @EventListener
    public void populateSDODates(PopulateSDODatesEvent event) throws IOException {
        CaseDetails caseDetails = event.getCallbackRequest().getCaseDetails();
        if (standardDirectionOrderPopulationService.hearingDetailsAdded(caseDetails)) {
            coreCaseDataService.triggerEvent(caseDetails.getJurisdiction(),
                caseDetails.getCaseTypeId(),
                caseDetails.getId(),
                "populateSDO",
                standardDirectionOrderPopulationService.populateDates(caseDetails));
        }
    }
}
