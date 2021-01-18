package uk.gov.hmcts.reform.fpl.jobs;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.QueryBuilders;
import org.json.JSONObject;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.service.CaseConverter;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.service.search.SearchService;

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
            log.info("Job {} :: skipping due to feature toggle", jobName);
            return;
        }
        log.info("Job {} :: started", jobName);

        log.debug("Job {} :: searching for cases", jobName);
        List<CaseDetails> cases = searchService.search(buildQuery(toggleService.isSummaryTabFirstCronRunEnabled()));
        log.info("Job {} :: {} cases found", jobName, cases.size());
        cases.forEach(caseDetails -> {
            Map<String, Object> updatedData = updateSummaryTab(caseDetails);
            final Long caseId = caseDetails.getId();
            try {
                log.debug("Job {} :: updating case {}", jobName, caseId);
                ccdService.triggerEvent(JURISDICTION, CASE_TYPE, caseId, SOME_EVENT, updatedData);
                log.info("Job {} :: updated case {}", jobName, caseId);
            } catch (Exception e) {
                log.error("Job {} :: could not update case {} due to {}", jobName, caseId, e.getMessage(), e);
            }
        });
        log.info("Job {} finished", jobName);
    }

    private Map<String, Object> updateSummaryTab(CaseDetails caseDetails) {
        // call update service here
        return caseDetails.getData();
    }

    public String buildQuery(boolean enabled) {
        if (enabled) {
            return new JSONObject(query(bool(mustNot(match("state", "Open"), match("state", "Deleted"))))).toString();
        }
        return new JSONObject(query(bool(mustNot(match("state", "Open"), match("state", "Deleted"), match("state", "Closed"))))).toString();
    }

    private Map<String, Object> query(Map<String, Object> query) {
        return Map.of("query", query);
    }

    private Map<String, Object> bool(Map<String, Object> booleanQuery) {
        return Map.of("bool", booleanQuery);
    }

    @SafeVarargs
    private Map<String, Object> mustNot(Map<String, Object>... queries) {
        return Map.of("must_not", List.of(queries));
    }

    private Map<String, Object> match(String field, String value) {
        return Map.of("match", Map.of(field, Map.of("query", value)));
    }

    public static void main(String[] args) {
        String query = QueryBuilders.boolQuery()
            .mustNot(QueryBuilders.matchQuery("state", "Deleted"))
            .mustNot(QueryBuilders.matchQuery("state", "Open"))
            .toString();

        UpdateSummaryTab tab = new UpdateSummaryTab(null, null, null, null, null);
        System.out.println("tab.buildQuery(true) = " + tab.buildQuery(true));
        System.out.println("tab.buildQuery(false) = " + tab.buildQuery(false));
    }

}
