package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.events.AfterSubmissionCaseDataUpdated;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.service.summary.CaseSummaryService;

import java.util.Map;

import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AfterSubmissionCaseDataUpdatedEventHandler {

    private final CoreCaseDataService coreCaseDataService;
    private final CaseSummaryService caseSummaryService;

    @EventListener
    public void handleCaseDataChange(final AfterSubmissionCaseDataUpdated event) {

        Map<String, Object> originalSummaryFields = caseSummaryService.generateSummaryFields(event.getCaseDataBefore());
        Map<String, Object> updatedSummaryFields = caseSummaryService.generateSummaryFields(event.getCaseData());

        if(fieldsHaveChanged(originalSummaryFields, updatedSummaryFields)){
            coreCaseDataService.triggerEvent(
                JURISDICTION,
                CASE_TYPE,
                event.getCaseData().getId(),
                "internal-update-case-summary",
                updatedSummaryFields);
        }

    }

    private boolean fieldsHaveChanged(Map<String, Object> originalSummaryFields,
                                      Map<String, Object> updatedSummaryFields) {
        return !originalSummaryFields.equals(updatedSummaryFields);
    }

}
