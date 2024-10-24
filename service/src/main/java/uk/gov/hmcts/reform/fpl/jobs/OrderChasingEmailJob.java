package uk.gov.hmcts.reform.fpl.jobs;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.events.cmo.SendOrderReminderEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.CaseConverter;
import uk.gov.hmcts.reform.fpl.service.cmo.SendOrderReminderService;
import uk.gov.hmcts.reform.fpl.service.search.SearchService;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.BooleanQuery;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.ESClause;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.ESQuery;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.Filter;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.MatchQuery;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.MustNot;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.RangeQuery;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.util.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.enums.State.CLOSED;
import static uk.gov.hmcts.reform.fpl.enums.State.DELETED;
import static uk.gov.hmcts.reform.fpl.enums.State.GATEKEEPING;
import static uk.gov.hmcts.reform.fpl.enums.State.OPEN;
import static uk.gov.hmcts.reform.fpl.enums.State.RETURNED;
import static uk.gov.hmcts.reform.fpl.enums.State.SUBMITTED;
import static uk.gov.hmcts.reform.fpl.service.search.SearchService.ES_DEFAULT_SIZE;
import static uk.gov.hmcts.reform.fpl.utils.JobHelper.buildStats;
import static uk.gov.hmcts.reform.fpl.utils.JobHelper.paginate;

@Slf4j
@Component
@ConditionalOnProperty(value = "scheduler.enabled", havingValue = "true")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class OrderChasingEmailJob implements Job {

    private final CaseConverter converter;
    private final SearchService searchService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final SendOrderReminderService sendOrderReminderService;

    @Override
    @SneakyThrows
    public void execute(JobExecutionContext jobExecutionContext) {
        final String jobName = jobExecutionContext.getJobDetail().getKey().getName();
        log.info("Job '{}' started", jobName);

        log.debug("Job '{}' searching for cases", jobName);

        int total;
        int skipped = 0;
        int chased = 0;
        int failed = 0;

        final ESQuery query = buildQuery();

        try {
            total = searchService.searchResultsSize(query);
            log.info("Job '{}' found {} cases to chase", jobName, total);
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
                        if (shouldSendChasingEmail(caseData)) {
                            log.debug("Job '{}' sending chase email for case {}", jobName, caseId);
                            applicationEventPublisher.publishEvent(new SendOrderReminderEvent(caseData));
                            chased++;
                        } else {
                            log.debug("Job '{}' skipped case {}", jobName, caseId);
                            skipped++;
                        }
                    } catch (Exception e) {
                        log.error("Job '{}' could not send Email on {} due to {}", jobName, caseId, e.getMessage(),
                            e);
                        failed++;
                        Thread.sleep(2000); // If CCD is overwhelmed, stop for 2s before continuing
                    }
                }
            } catch (Exception e) {
                if (Thread.interrupted()) {
                    // if this exception was on Thread.sleep rather than get case details
                    Thread.currentThread().interrupt();
                }
                log.error("Job '{}' could not search for cases due to {}", jobName, e.getMessage(), e);
                failed += ES_DEFAULT_SIZE;
            }
        }

        log.info("Job '{}' finished. {}", jobName, buildStats(total, skipped, chased, failed));
    }

    private boolean isInRange(Element<HearingBooking> booking) {
        // todo - check the dates on this query with some edge cases!
        boolean beforeRange = booking.getValue().getEndDate().toLocalDate().isBefore(LocalDate.now().minusDays(5))
            || booking.getValue().getEndDate().toLocalDate().isEqual(LocalDate.now().minusDays(5));
        boolean afterRange = booking.getValue().getEndDate().toLocalDate().isAfter(LocalDate.now().minusDays(6));

        return beforeRange && afterRange;
    }

    private boolean shouldSendChasingEmail(CaseData caseData) {
        // Check if the hearings within 5(-6) days have an uploaded CMO
        if (isEmpty(caseData.getHearingDetails())) {
            return false;
        }
        List<Element<HearingBooking>> hearingsWithinRange = caseData.getHearingDetails().stream()
                .filter(this::isInRange)
                .collect(Collectors.toList());

        // ES cannot index nulls (automatically), also sometimes the cmo field is not present at all => manual check
        return hearingsWithinRange.stream().anyMatch(booking -> !booking.getValue().hasCMOAssociation()
            && !sendOrderReminderService.checkSealedCMOExistsForHearing(caseData, booking));
    }

    private MustNot getInvalidStates() {
        List<ESClause> notTheseStates = List.of(OPEN, SUBMITTED, GATEKEEPING, CLOSED, DELETED, RETURNED).stream()
            .map(state -> MatchQuery.of("state", state.getValue()))
            .collect(Collectors.toList());

        return MustNot.builder().clauses(notTheseStates).build();
    }

    private ESQuery buildQuery() {
        // todo - check the dates on this query with some edge cases!
        final RangeQuery.RangeQueryBuilder olderThan5Days = RangeQuery.builder()
            .field("data.hearingDetails.value.endDate")
            .lessThanOrEqual("now/d-5d")
            .greaterThanOrEqual("now/d-5d");

        return BooleanQuery.builder()
            .mustNot(getInvalidStates())
            .filter(Filter.builder().clauses(List.of(olderThan5Days.build())).build())
            .build();
    }
}
