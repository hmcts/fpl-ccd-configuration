package uk.gov.hmcts.reform.fpl.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.events.AfterSubmissionCaseDataUpdated;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.summary.SyntheticCaseSummary;
import uk.gov.hmcts.reform.fpl.service.CaseConverter;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.service.summary.CaseSummaryService;

import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AfterSubmissionCaseDataUpdatedEventHandler {

    private final CoreCaseDataService coreCaseDataService;
    private final CaseSummaryService caseSummaryService;
    private final ObjectMapper objectMapper;
    private final CaseConverter caseConverter;

    @EventListener
    @Async
    public void handleCaseDataChange(final AfterSubmissionCaseDataUpdated event) {
        coreCaseDataService.performPostSubmitCallback(event.getCaseData().getId(),
            "internal-update-case-summary",
            caseDetails -> {
                CaseData currentCaseData = caseConverter.convert(caseDetails);
                return caseSummaryService.generateSummaryFields(currentCaseData);
            }
        );
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
