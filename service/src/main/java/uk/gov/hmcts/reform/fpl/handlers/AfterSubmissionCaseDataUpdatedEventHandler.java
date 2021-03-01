package uk.gov.hmcts.reform.fpl.handlers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.events.AfterSubmissionCaseDataUpdated;
import uk.gov.hmcts.reform.fpl.model.summary.SyntheticCaseSummary;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.service.summary.CaseSummaryService;

import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AfterSubmissionCaseDataUpdatedEventHandler {

    private final CoreCaseDataService coreCaseDataService;
    private final CaseSummaryService caseSummaryService;
    private final ObjectMapper objectMapper;

    @EventListener
    public void handleCaseDataChange(final AfterSubmissionCaseDataUpdated event) {
        Map<String, Object> originalSummaryFields = objectMapper.convertValue(
            originalSyntheticCaseSummary(event),
            new TypeReference<>() {});

        Map<String, Object> updatedSummaryFields = caseSummaryService.generateSummaryFields(event.getCaseData());

        if (fieldsHaveChanged(originalSummaryFields, updatedSummaryFields)) {
            coreCaseDataService.triggerEvent(
                JURISDICTION,
                CASE_TYPE,
                event.getCaseData().getId(),
                "internal-update-case-summary",
                updatedSummaryFields);
        }
    }

    private SyntheticCaseSummary originalSyntheticCaseSummary(AfterSubmissionCaseDataUpdated event) {
        return Optional.ofNullable(event.getCaseDataBefore())
            .flatMap(caseData -> Optional.ofNullable(caseData.getSyntheticCaseSummary()))
            .orElse(SyntheticCaseSummary.emptySummary());
    }

    private boolean fieldsHaveChanged(Map<String, Object> originalSummaryFields,
                                      Map<String, Object> updatedSummaryFields) {
        return !originalSummaryFields.equals(updatedSummaryFields);
    }

}
