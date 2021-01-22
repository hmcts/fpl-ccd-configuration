package uk.gov.hmcts.reform.fpl.jobs;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.summary.SyntheticCaseSummary;
import uk.gov.hmcts.reform.fpl.service.CaseConverter;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.service.search.SearchService;
import uk.gov.hmcts.reform.fpl.service.summary.CaseSummaryService;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.BooleanQuery;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.ESQuery;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.MatchQuery;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.MustNot;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;

@Slf4j
@Component
@ConditionalOnProperty(value = "scheduler.enabled", havingValue = "true")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class UpdateSummaryCaseDetails implements Job {
    private static final String EVENT_NAME = "internal-update-case-summary";
    private static final int SEARCH_SIZE = 3000;

    private final CaseConverter converter;
    private final ObjectMapper mapper;
    private final SearchService searchService;
    private final CoreCaseDataService ccdService;
    private final FeatureToggleService toggleService;
    private final CaseSummaryService summaryService;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        final String jobName = jobExecutionContext.getJobDetail().getKey().getName();
        if (!toggleService.isSummaryTabEnabled()) {
            log.info("Job {} skipping due to feature toggle", jobName);
            return;
        }
        log.info("Job {} started", jobName);

        log.debug("Job {} searching for cases", jobName);
        final ESQuery query = buildQuery(toggleService.isSummaryTabFirstCronRunEnabled());
        List<CaseDetails> cases = searchService.search(query, SEARCH_SIZE);

        int total = cases.size();
        int skipped = 0;
        int updated = 0;

        log.info("Job {} found {} cases", jobName, total);
        for (CaseDetails caseDetails : cases) {
            CaseData caseData = converter.convert(caseDetails);
            Map<String, Object> updatedData = summaryService.generateSummaryFields(caseData);
            final Long caseId = caseDetails.getId();
            try {
                if (shouldUpdate(updatedData, caseData)) {
                    log.debug("Job {} updating case {}", jobName, caseId);
                    ccdService.triggerEvent(JURISDICTION, CASE_TYPE, caseId, EVENT_NAME, updatedData);
                    log.info("Job {} updated case {}", jobName, caseId);
                    updated++;
                } else {
                    log.debug("Job {} skipped case {}", jobName, caseId);
                    skipped++;
                }
            } catch (Exception e) {
                log.error("Job {} could not update case {} due to {}", jobName, caseId, e.getMessage(), e);
            }
        }
        log.info("Job {} finished. {}", jobName, buildStats(total, skipped, updated));
    }

    private boolean shouldUpdate(Map<String, Object> updatedData, CaseData oldData) {
        SyntheticCaseSummary newSummaryData = mapper.convertValue(updatedData, SyntheticCaseSummary.class);
        return !Objects.equals(newSummaryData, oldData.getSyntheticCaseSummary());
    }

    private ESQuery buildQuery(boolean firstPassEnabled) {
        final String field = "state";
        final MatchQuery openCases = MatchQuery.of(field, State.OPEN.getValue());
        final MatchQuery deletedCases = MatchQuery.of(field, State.DELETED.getValue());
        final MatchQuery returnedCases = MatchQuery.of(field, State.RETURNED.getValue());
        final MatchQuery closedCases = MatchQuery.of(field, State.CLOSED.getValue());

        MustNot.MustNotBuilder mustNot = MustNot.builder();

        if (firstPassEnabled) {
            mustNot.clauses(List.of(openCases, deletedCases, returnedCases));
        } else {
            mustNot.clauses(List.of(openCases, deletedCases, returnedCases, closedCases));
        }

        return BooleanQuery.builder()
            .mustNot(mustNot.build())
            .build();
    }

    private String buildStats(int total, int skipped, int updated) {
        double percentUpdated = updated * 100.0 / total;
        double percentSkipped = skipped * 100.0 / total;

        return String.format(
            "total cases: %1$d, updated cases: %2$d/%1$d (%4$.0f%%), skipped cases: %3$d/%1$d (%5$.0f%%)",
            total,
            updated,
            skipped,
            percentUpdated,
            percentSkipped);
    }
}
