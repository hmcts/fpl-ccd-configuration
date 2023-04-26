package uk.gov.hmcts.reform.fpl.jobs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.config.MigrationConfig;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.service.search.SearchService;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.ESQuery;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static java.math.RoundingMode.UP;
import static org.springframework.util.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.controllers.support.MigrateCaseController.MIGRATION_ID_KEY;
import static uk.gov.hmcts.reform.fpl.service.search.SearchService.ES_DEFAULT_SIZE;

@Slf4j
@Component
@ConditionalOnProperty(value = "scheduler.enabled", havingValue = "true")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class CaseMigrationJob implements Job {

    private final CoreCaseDataService coreCaseDataService;
    private final SearchService searchService;
    private final MigrationConfig migrationConfig;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        final String jobName = context.getJobDetail().getKey().getName();
        log.info("Job '{}' started", jobName);

        switch (migrationConfig.getMigrationJobMode()) {
            case ID_LIST:
                log.info("Starting ID List Migration");
                doIdListMigration(jobName);
                break;
            case ES_QUERY:
                log.info("Starting ES Query Migration");
                doEsQueryMigration(jobName);
                break;
            default:
                log.error("Invalid Migration Job Mode");
        }
    }

    public void doIdListMigration(String jobName) {
        AtomicInteger updated = new AtomicInteger();
        AtomicInteger failed = new AtomicInteger();

        migrationConfig.getCaseIds()
            .parallelStream().forEach(id -> {
                    CaseDetails after = coreCaseDataService.performPostSubmitCallback(id, "migrateCase",
                        caseDetails -> Map.of(MIGRATION_ID_KEY, migrationConfig.getMigrationId()));

                    if (isEmpty(after)) {
                        failed.getAndIncrement();
                    } else {
                        updated.getAndIncrement();
                    }
                }
            );

        log.info("Job '{}' completed, {} success, {} failed", jobName, updated.get(), failed.get());
    }

    public void doEsQueryMigration(String jobName) {
        ESQuery query = migrationConfig.getMigrationQueries().get(migrationConfig.getMigrationId()).get();

        int total;

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

        AtomicInteger updated = new AtomicInteger();
        AtomicInteger failed = new AtomicInteger();

        int pages = paginate(total);
        log.debug("Job '{}' split the search query over {} pages", jobName, pages);
        for (int i = 0; i < pages; i++) {
            try {
                List<CaseDetails> cases = searchService.search(query, ES_DEFAULT_SIZE, i * ES_DEFAULT_SIZE);
                cases.parallelStream().forEach(caseDetails -> {
                    final Long caseId = caseDetails.getId();

                    CaseDetails after = coreCaseDataService.performPostSubmitCallback(caseId, "migrateCase",
                        caseDetails1 -> Map.of(MIGRATION_ID_KEY, migrationConfig.getMigrationId()));

                    if (isEmpty(after)) {
                        failed.getAndIncrement();
                    } else {
                        updated.getAndIncrement();
                    }

                });
            } catch (Exception e) {
                log.error("Job '{}' could not search for cases due to {}", jobName, e.getMessage(), e);
                failed.addAndGet(ES_DEFAULT_SIZE);
            }
        }

        log.info("Job '{}' completed, {} success, {} failed", jobName, updated.get(), failed.get());
    }

    private int paginate(int total) {
        return new BigDecimal(total).divide(new BigDecimal(ES_DEFAULT_SIZE), UP).intValue();
    }

}
