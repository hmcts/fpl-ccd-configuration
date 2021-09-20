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
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.Must;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.MustNot;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.RangeQuery;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.math.RoundingMode.UP;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.service.search.SearchService.ES_DEFAULT_SIZE;

@Slf4j
@Component
@ConditionalOnProperty(value = "scheduler.enabled", havingValue = "true")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class UpdateSummaryCaseDetails implements Job {
    private static final String EVENT_NAME = "internal-update-case-summary";
    private static final String RANGE_FIELD = "data.caseSummaryNextHearingDate";

    private final CaseConverter converter;
    private final ObjectMapper mapper;
    private final SearchService searchService;
    private final CoreCaseDataService ccdService;
    private final FeatureToggleService toggleService;
    private final CaseSummaryService summaryService;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        final String jobName = jobExecutionContext.getJobDetail().getKey().getName();
        log.info("Job '{}' started", jobName);

        log.debug("Job '{}' searching for cases", jobName);

        final ESQuery query = buildQuery(toggleService.isSummaryTabFirstCronRunEnabled());

        int total;
        int skipped = 0;
        int updated = 0;
        int failed = 0;

        try {
            total = searchService.searchResultsSize(query);
            log.info("Job '{}' found {} cases", jobName, total);
        } catch (Exception e) {
            log.error("Job '{}' could not determine the number of cases to search for due to {}",
                jobName, e.getMessage(), e
            );
            log.info("Job '{}' finished unsuccessfully.", jobName);
            return;
        }

        int pages = paginate(total);
        log.debug("Job '{}' split the search query over {} pages", jobName, pages);
        for (int i = 0; i < pages; i++) {
            try {
                List<CaseDetails> cases = searchService.search(query, ES_DEFAULT_SIZE, i * ES_DEFAULT_SIZE);
                for (CaseDetails caseDetails : cases) {
                    final Long caseId = caseDetails.getId();
                    try {
                        CaseData caseData = converter.convert(caseDetails);
                        Map<String, Object> updatedData = summaryService.generateSummaryFields(caseData);
                        if (shouldUpdate(updatedData, caseData)) {
                            log.debug("Job '{}' updating case {}", jobName, caseId);
                            ccdService.triggerEvent(JURISDICTION, CASE_TYPE, caseId, EVENT_NAME, updatedData);
                            log.info("Job '{}' updated case {}", jobName, caseId);
                            updated++;
                        } else {
                            log.debug("Job '{}' skipped case {}", jobName, caseId);
                            skipped++;
                        }
                    } catch (Exception e) {
                        log.error("Job '{}' could not update case {} due to {}", jobName, caseId, e.getMessage(), e);
                        failed++;
                        Thread.sleep(3000); // give ccd time to recover in case it was getting too many request
                    }
                }
            } catch (Exception e) {
                log.error("Job '{}' could not search for cases due to {}", jobName, e.getMessage(), e);
                failed += ES_DEFAULT_SIZE;
            }
        }

        log.info("Job '{}' finished. {}", jobName, buildStats(total, skipped, updated, failed));
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
        Must must = null;

        if (firstPassEnabled) {
            mustNot.clauses(List.of(openCases, deletedCases, returnedCases));
        } else {
            mustNot.clauses(List.of(openCases, deletedCases, returnedCases, closedCases));
            must = Must.builder()
                .clauses(List.of(
                    RangeQuery.builder()
                        .field(RANGE_FIELD)
                        .lessThan("now/d")
                        .build()
                ))
                .build();
        }

        return BooleanQuery.builder()
            .must(must)
            .mustNot(mustNot.build())
            .build();
    }

    private int paginate(int total) {
        return new BigDecimal(total).divide(new BigDecimal(ES_DEFAULT_SIZE), UP).intValue();
    }


    private String buildStats(int total, int skipped, int updated, int failed) {
        double percentUpdated = updated * 100.0 / total;
        double percentSkipped = skipped * 100.0 / total;
        double percentFailed = failed * 100.0 / total;

        return String.format("total cases: %1$d, "
                + "updated cases: %2$d/%1$d (%5$.0f%%), "
                + "skipped cases: %3$d/%1$d (%6$.0f%%), "
                + "failed cases: %4$d/%1$d (%7$.0f%%)",
            total, updated, skipped, failed, percentUpdated, percentSkipped, percentFailed
        );
    }
}
