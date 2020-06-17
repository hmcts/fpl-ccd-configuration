package uk.gov.hmcts.reform.fpl.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.events.CaseDataChanged;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;

import java.util.Map;

import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseDataChangedHandler {
    private final ObjectMapper mapper;
    private final CoreCaseDataService coreCaseDataService;
    private final RequestData requestData;
    private final CaseStateService caseStateService;

    @EventListener
    public void onDataChanged(final CaseDataChanged event) {

        Long caseId = event.getCallbackRequest().getCaseDetails().getId();
        CaseDetails caseDetails = event.getCallbackRequest().getCaseDetails();

        String laStatus = caseStateService.getStatusForLA(caseDetails);
        String adminStatus = caseStateService.getStatusForAdmin(caseDetails);

        coreCaseDataService.triggerEvent(JURISDICTION, CASE_TYPE, caseId,"internal-update-case-info", Map.of("caseStateLA", laStatus, "caseStateAdmin", adminStatus));
    }


}
