package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.fpl.events.PopulateStandardDirectionsEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.CaseConverter;
import uk.gov.hmcts.reform.fpl.service.StandardDirectionsService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PopulateStandardDirectionsHandler {
    private final CoreCaseDataService coreCaseDataService;
    private final StandardDirectionsService standardDirectionsService;
    private final CaseConverter caseConverter;

    @Async
    @EventListener
    @SuppressWarnings("unchecked")
    public void populateStandardDirections(PopulateStandardDirectionsEvent event) {
        CaseDetails oldCaseDetails = event.getCallbackRequest().getCaseDetails();

        coreCaseDataService.performPostSubmitCallback(oldCaseDetails.getId(), "populateSDO",
            caseDetails -> (Map) standardDirectionsService.populateStandardDirections(caseConverter.convert(caseDetails))
        );
    }
}
