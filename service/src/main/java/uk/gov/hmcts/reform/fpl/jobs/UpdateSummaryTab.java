package uk.gov.hmcts.reform.fpl.jobs;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.CaseConverter;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.service.search.SearchService;
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
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class UpdateSummaryTab implements Job {
    private static final String EVENT_NAME = "internal-update-summary-tab";

    private final CaseConverter converter;
    private final ObjectMapper mapper;
    private final SearchService searchService;
    private final CoreCaseDataService ccdService;
    private final FeatureToggleService toggleService;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        final String jobName = jobExecutionContext.getJobDetail().getKey().getName();
        if (!toggleService.isSummaryTabEnabled()) {
            log.info("Job {} skipping due to feature toggle", jobName);
            return;
        }
        log.info("Job {} started", jobName);

        log.debug("Job {} searching for cases", jobName);
        List<CaseDetails> cases = searchService.search(buildQuery(toggleService.isSummaryTabFirstCronRunEnabled()));

        int total = cases.size();
        int skipped = 0;
        int updated = 0;

        log.info("Job {} found {} cases", jobName, total);
        for (CaseDetails caseDetails : cases) {
            CaseData caseData = converter.convert(caseDetails);
            Object updatedData = updateSummaryTab(caseData);
            final Long caseId = caseDetails.getId();
            try {
                if (!shouldUpdate(updatedData, caseData)) {
                    log.debug("Job {} skipped case {}", jobName, caseId);
                    skipped++;
                } else {
                    log.debug("Job {} updating case {}", jobName, caseId);
                    Map<String, Object> dataToAdd = mapper.convertValue(updatedData, new TypeReference<>() {});
                    ccdService.triggerEvent(JURISDICTION, CASE_TYPE, caseId, EVENT_NAME, dataToAdd);
                    log.info("Job {} updated case {}", jobName, caseId);
                    updated++;
                }
            } catch (Exception e) {
                log.error("Job {} could not update case {} due to {}", jobName, caseId, e.getMessage(), e);
            }
        }
        log.info("Job {} finished. {}", jobName, buildStats(total, skipped, updated));
    }

    private Object updateSummaryTab(CaseData caseData) {
        // TODO: 20/01/2021 call update service here
        return caseData;
    }

    private boolean shouldUpdate(Object updatedData, CaseData oldData) {
        // TODO: 20/01/2021 get the old data field here, update params with the actual data type
        return !Objects.equals(updatedData, oldData);
    }

    private ESQuery buildQuery(boolean firstPassEnabled) {
        MatchQuery openCases = MatchQuery.of("state", State.OPEN.getValue());
        MatchQuery deletedCases = MatchQuery.of("state", State.DELETED.getValue());
        MatchQuery closedCases = MatchQuery.of("state", State.CLOSED.getValue());

        MustNot.MustNotBuilder mustNot = MustNot.builder();

        if (firstPassEnabled) {
            mustNot.clauses(List.of(openCases, deletedCases));
        } else {
            mustNot.clauses(List.of(openCases, deletedCases, closedCases));
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
