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
import uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.events.order.GeneratedOrderEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.cafcass.NoticeOfHearingCafcassData;
import uk.gov.hmcts.reform.fpl.model.cafcass.OrderCafcassData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.CaseConverter;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.cafcass.CafcassNotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.NoticeOfHearingEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.search.SearchService;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.BooleanQuery;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.ESQuery;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.MatchQuery;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.MustNot;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.math.RoundingMode.UP;
import static java.util.Set.of;
import static java.util.stream.Collectors.toSet;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassNotificationService.DATE_FORMATTER;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.NOTICE_OF_HEARING;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.ORDER;
import static uk.gov.hmcts.reform.fpl.service.search.SearchService.ES_DEFAULT_SIZE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.findElement;

@Slf4j
@Component
@ConditionalOnProperty(value = "scheduler.enabled", havingValue = "true")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ResendCafcassEmails implements Job {
    private static final String EVENT_NAME = "internal-update-case-summary";
    private static final String RANGE_FIELD = "data.caseSummaryNextHearingDate";

    private final CaseConverter converter;
    private final ObjectMapper mapper;
    private final SearchService searchService;
    private final CafcassNotificationService cafcassNotificationService;
    private final FeatureToggleService featureToggleService;
    private final NoticeOfHearingEmailContentProvider noticeOfHearingEmailContentProvider;

    private Map<Long, List<LocalDate>> casesToResend;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        final String jobName = jobExecutionContext.getJobDetail().getKey().getName();
        log.info("Job '{}' started", jobName);

        log.debug("Job '{}' searching for cases", jobName);

        final ESQuery query = buildQuery();

        int total;
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
                        if (shouldResendEmail(caseId)) {
                            log.debug("Job '{}' resending email on case {}", jobName, caseId);
                            CaseData caseData = converter.convert(caseDetails);

                            List<LocalDate> datesToResend = casesToResend.get(caseId);

                            // check ordersCollection
                            resendGeneratedOrders(caseData, datesToResend);

                            // check hearingOrdersBundlesDrafts (?)
                            resendDraftOrder(caseData, datesToResend);

                            // check sealedCMOs
                            resendSealedCMOs(caseData, datesToResend);

                            // notice of hearing
                            // resendNoticeOfHearing(caseData, dateTimesToResend); todo - NoH

                            updated++;
                        }
                    } catch (Exception e) {
                        log.error("Job '{}' could not resend email on case {} due to {}", jobName, caseId,
                            e.getMessage(), e);
                        failed++;
                        Thread.sleep(3000); // give ccd time to recover in case it was getting too many request
                    }
                }
            } catch (Exception e) {
                log.error("Job '{}' could not search for cases due to {}", jobName, e.getMessage(), e);
                failed += ES_DEFAULT_SIZE;
            }
        }

        log.info("Job '{}' finished. {}", jobName, buildStats(total, updated, failed));
    }

    // and approved orders
    private void resendGeneratedOrders(CaseData caseData, List<LocalDate> datesToResend) {
        List<Element<GeneratedOrder>> ordersToSend = caseData.getOrderCollection().stream()
            .filter(el -> datesToResend.contains(el.getValue().getApprovalDate()))
            // todo - add the other date condition
            .collect(Collectors.toList());

        for (Element<GeneratedOrder> order : ordersToSend) {
            GeneratedOrderEvent event = new GeneratedOrderEvent(
                caseData,
                order.getValue().getDocument(),
                LanguageTranslationRequirement.NO,
                order.getValue().getTitle(),
                order.getValue().getApprovalDate() // todo other date
            );

            if (featureToggleService.isResendCafcassEmailsEnabled()) {
                cafcassNotificationService.sendEmail(caseData,
                    of(event.getOrderDocument()),
                    ORDER,
                    OrderCafcassData.builder()
                        .documentName(event.getOrderDocument().getFilename())
                        .orderApprovalDate(event.getOrderApprovalDate())
                        .build()
                );
            } else {
                log.info("Would have resent generated order email about {}, {}", caseData.getId(),
                    order.getValue().getTitle());
            }
        }
    }

    private void resendDraftOrder(CaseData caseData, List<LocalDate> datesToResend) {

        List<Element<HearingOrdersBundle>> draftOrders = caseData.getHearingOrdersBundlesDrafts();

        for (Element<HearingOrdersBundle> bundle : draftOrders) {

            Set<DocumentReference> docs = bundle.getValue().getOrders().stream()
                .filter(el -> datesToResend.contains(el.getValue().getDateSent()))
                .map(el -> el.getValue().getOrder())
                .collect(toSet());

            if (!docs.isEmpty()) {
                LocalDateTime hearingStartDate = findElement(bundle.getId(), caseData.getAllHearings())
                    .map(el -> el.getValue().getStartDate())
                    .orElse(null);

                if (featureToggleService.isResendCafcassEmailsEnabled()) {
                    cafcassNotificationService.sendEmail(caseData,
                        docs,
                        ORDER,
                        OrderCafcassData.builder()
                            .documentName("draft order")
                            .hearingDate(hearingStartDate)
//                        .orderApprovalDate() todo - just let it use today's date
                            .build()
                    );
                } else {
                    log.info("Would have resent draft orders email about {}, number of drafts: {}", caseData.getId(),
                        docs.size());
                }

            }
        }
    }

    private void resendSealedCMOs(CaseData caseData, List<LocalDate> dates) {

        List<Element<HearingOrder>> sealedCMOsToResend = caseData.getSealedCMOs().stream()
            .filter(el -> dates.contains(el.getValue().getDateIssued()))
            .collect(Collectors.toList());

        for (Element<HearingOrder> cmo : sealedCMOsToResend) {
            if (featureToggleService.isResendCafcassEmailsEnabled()) {
                cafcassNotificationService.sendEmail(caseData,
                    of(cmo.getValue().getOrder()),
                    ORDER,
                    OrderCafcassData.builder()
                        .documentName(cmo.getValue().getTitle())
                        // .hearingDate(hearingStartDate) // todo - scrapping?
                        .orderApprovalDate(cmo.getValue().getDateIssued())
                        .build()
                );
            } else {
                log.info("Would have resent sealed CMO email about {}, {}", caseData.getId(),
                    cmo.getValue().getTitle());
            }

        }
    }

    private void resendNoticeOfHearing(CaseData caseData, List<LocalDateTime> dateTimes) {

        List<Element<HearingBooking>> hearings = caseData.getHearingDetails().stream()
            .filter(el -> dateTimes.contains(el.getValue().getStartDate()))
            .collect(Collectors.toList());

        for (Element<HearingBooking> booking : hearings) {

            NoticeOfHearingCafcassData noticeOfHearingCafcassData =
                noticeOfHearingEmailContentProvider.buildNewNoticeOfHearingNotificationCafcassData(
                    caseData,
                    booking.getValue()
                );

            if (featureToggleService.isResendCafcassEmailsEnabled()) {
                cafcassNotificationService.sendEmail(caseData,
                    of(booking.getValue().getNoticeOfHearing()),
                    NOTICE_OF_HEARING,
                    noticeOfHearingCafcassData);
            } else {
                log.info("Would have resent notice of hearing email about {}, {}", caseData.getId(),
                    booking.getValue().getStartDate().format(DATE_FORMATTER));
            }
        }
    }

    private boolean shouldResendEmail(Long caseId) {
        return casesToResend.containsKey(caseId);
    }

    private ESQuery buildQuery() {
        final String field = "state";
        final MatchQuery openCases = MatchQuery.of(field, State.OPEN.getValue());
        final MatchQuery deletedCases = MatchQuery.of(field, State.DELETED.getValue());
        final MatchQuery returnedCases = MatchQuery.of(field, State.RETURNED.getValue());

        MustNot.MustNotBuilder mustNot = MustNot.builder();
        mustNot.clauses(List.of(openCases, deletedCases, returnedCases));

        return BooleanQuery.builder()
            .mustNot(mustNot.build())
            .build();
    }

    private int paginate(int total) {
        return new BigDecimal(total).divide(new BigDecimal(ES_DEFAULT_SIZE), UP).intValue();
    }

    private String buildStats(int total, int updated, int failed) {
        double percentUpdated = updated * 100.0 / total;
        double percentFailed = failed * 100.0 / total;

        return String.format("total cases: %1$d, "
                + "resent cases: %2$d/%1$d (%5$.0f%%), "
                + "failed cases: %4$d/%1$d (%7$.0f%%)",
            total, updated, failed, percentUpdated, percentFailed
        );
    }
}
