package uk.gov.hmcts.reform.fpl.jobs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.CaseConverter;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.search.SearchService;
import uk.gov.hmcts.reform.fpl.service.workallocation.WorkAllocationTaskService;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.BooleanQuery;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.ESClause;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.ESQuery;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.Filter;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.MatchQuery;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.MustNot;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.RangeQuery;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static java.math.RoundingMode.UP;
import static org.springframework.util.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.enums.State.CLOSED;
import static uk.gov.hmcts.reform.fpl.enums.State.DELETED;
import static uk.gov.hmcts.reform.fpl.enums.State.GATEKEEPING;
import static uk.gov.hmcts.reform.fpl.enums.State.OPEN;
import static uk.gov.hmcts.reform.fpl.enums.State.RETURNED;
import static uk.gov.hmcts.reform.fpl.enums.State.SUBMITTED;
import static uk.gov.hmcts.reform.fpl.enums.WorkAllocationTaskType.ORDER_NOT_UPLOADED;
import static uk.gov.hmcts.reform.fpl.service.search.SearchService.ES_DEFAULT_SIZE;

@Slf4j
@Component
@ConditionalOnProperty(value = "scheduler.enabled", havingValue = "true")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class CreateWAOrderChasingTasks implements Job {

    private final CaseConverter converter;
    private final SearchService searchService;
    private final FeatureToggleService toggleService;
    private final WorkAllocationTaskService workAllocationTaskService;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        final String jobName = jobExecutionContext.getJobDetail().getKey().getName();
        log.info("Job '{}' started", jobName);

        log.debug("Job '{}' searching for cases", jobName);

        final boolean isFirstRun = toggleService.isChaseOrdersFirstCronRunEnabled();

        final ESQuery query = buildQuery(isFirstRun);

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
                        if (shouldCreateChaseTask(caseData, isFirstRun)) {
                            log.debug("Job '{}' creating chase task {}", jobName, caseId);
                            workAllocationTaskService.createWorkAllocationTask(caseData, ORDER_NOT_UPLOADED);
                            log.info("Job '{}' created chase task {}", jobName, caseId);
                            updated++;
                        } else {
                            log.debug("Job '{}' skipped case {}", jobName, caseId);
                            skipped++;
                        }
                    } catch (Exception e) {
                        log.error("Job '{}' could not create WA task on {} due to {}", jobName, caseId, e.getMessage(),
                            e);
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

    private boolean isInRange(HearingBooking booking, boolean isFirstRun) {
        // todo - check the dates on this query with some edge cases!
        boolean beforeRange = booking.getEndDate().toLocalDate().isBefore(LocalDate.now().minusDays(5))
            || booking.getEndDate().toLocalDate().isEqual(LocalDate.now().minusDays(5));
        boolean afterRange = booking.getEndDate().toLocalDate().isAfter(LocalDate.now().minusDays(6));

        return isFirstRun ? (beforeRange) : (beforeRange && afterRange);
    }

    private boolean shouldCreateChaseTask(CaseData caseData, boolean isFirstRun) {
        // Check if the hearings within 5(-6) days have an uploaded CMO
        if (isEmpty(caseData.getHearingDetails())) {
            return false;
        }
        List<HearingBooking> hearingsWithinRange = caseData.getHearingDetails().stream()
                .map(Element::getValue)
                .filter(booking -> isInRange(booking, isFirstRun))
                .collect(Collectors.toList());

        // ES cannot index nulls (automatically), also sometimes the cmo field is not present at all => manual check
        return hearingsWithinRange.stream().anyMatch(booking -> !booking.hasCMOAssociation());
    }

    private MustNot getInvalidStates() {
        List<ESClause> notTheseStates = List.of(OPEN, SUBMITTED, GATEKEEPING, CLOSED, DELETED, RETURNED).stream()
            .map(state -> MatchQuery.of("state", state.getValue()))
            .collect(Collectors.toList());

        return MustNot.builder().clauses(notTheseStates).build();
    }

    private ESQuery buildQuery(boolean firstPassEnabled) {
        // todo - check the dates on this query with some edge cases!
        final RangeQuery.RangeQueryBuilder olderThan5Days = RangeQuery.builder()
            .field("data.hearingDetails.value.endDate")
            .lessThanOrEqual("now/d-5d");

        if (!firstPassEnabled) {
            olderThan5Days.greaterThanOrEqual("now/d-5d"); // toggle this based on first run or not!
        }

        return BooleanQuery.builder()
            .mustNot(getInvalidStates())
            .filter(Filter.builder().clauses(List.of(olderThan5Days.build())).build())
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
