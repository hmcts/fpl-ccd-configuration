package uk.gov.hmcts.reform.fpl.jobs;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.State;
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

import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class UpdateSummaryTab implements Job {
    private static final String SOME_EVENT = "";

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
        log.info("Job {} {} cases found", jobName, cases.size());
        cases.forEach(caseDetails -> {
            Map<String, Object> updatedData = updateSummaryTab(caseDetails);
            final Long caseId = caseDetails.getId();
            try {
                if (updatedData.equals(caseDetails.getData())) {
                    log.debug("Job {} skipped case {}", jobName, caseId);
                } else {
                    log.debug("Job {} updating case {}", jobName, caseId);
                    ccdService.triggerEvent(JURISDICTION, CASE_TYPE, caseId, SOME_EVENT, updatedData);
                    log.info("Job {} updated case {}", jobName, caseId);
                }
            } catch (Exception e) {
                log.error("Job {} could not update case {} due to {}", jobName, caseId, e.getMessage(), e);
            }
        });
        log.info("Job {} finished", jobName);
    }

    private Map<String, Object> updateSummaryTab(CaseDetails caseDetails) {
        // call update service here
        return caseDetails.getData();
    }

    public ESQuery buildQuery(boolean firstPassEnabled) {
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
}
