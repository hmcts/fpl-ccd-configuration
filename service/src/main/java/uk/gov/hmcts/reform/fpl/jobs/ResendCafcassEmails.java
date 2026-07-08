package uk.gov.hmcts.reform.fpl.jobs;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement;
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
import uk.gov.hmcts.reform.fpl.service.ResendCafcassEmailService;
import uk.gov.hmcts.reform.fpl.service.cafcass.CafcassNotificationService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.service.email.content.NoticeOfHearingEmailContentProvider;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Set.of;
import static java.util.stream.Collectors.toSet;
import static org.springframework.util.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassNotificationService.DATE_FORMATTER;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.NOTICE_OF_HEARING;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.ORDER;
import static uk.gov.hmcts.reform.fpl.service.search.SearchService.ES_DEFAULT_SIZE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.findElement;

@Slf4j
@Component
@ConditionalOnProperty(value = "scheduler.enabled", havingValue = "true")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ResendCafcassEmails implements Job {

    private final CaseConverter converter;
    private final CafcassNotificationService cafcassNotificationService;
    private final FeatureToggleService featureToggleService;
    private final NoticeOfHearingEmailContentProvider noticeOfHearingEmailContentProvider;
    private final CoreCaseDataService coreCaseDataService;
    private final ResendCafcassEmailService resendCafcassEmailService;

    @SneakyThrows
    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        final String jobName = jobExecutionContext.getJobDetail().getKey().getName();
        log.info("Job '{}' started", jobName);

        int resent = 0;
        int totalResentEmails = 0;
        int failed = 0;

        try {
            for (Long caseId : resendCafcassEmailService.getAllCaseIds()) {
                try {
                    CaseDetails caseDetails = coreCaseDataService.findCaseDetailsByIdNonUser(caseId.toString());
                    log.debug("Job '{}' resending email on case {}", jobName, caseId);
                    CaseData caseData = converter.convert(caseDetails);

                    List<LocalDate> datesToResend = resendCafcassEmailService.getOrderDates(caseId);

                    int resentEmailsCount = 0;

                    if (!datesToResend.isEmpty()) {
                        // check ordersCollection
                        resentEmailsCount += resendGeneratedOrders(caseData, datesToResend);

                        // check hearingOrdersBundlesDrafts (?)
                        resentEmailsCount += resendDraftOrder(caseData, datesToResend);

                        // check sealedCMOs
                        resentEmailsCount += resendSealedCMOs(caseData, datesToResend);
                    }

                    // notice of hearing
                    List<LocalDateTime> dateTimes = resendCafcassEmailService.getNoticeOfHearingDateTimes(caseId);
                    if (!dateTimes.isEmpty()) {
                        resentEmailsCount += resendNoticeOfHearing(caseData, dateTimes);
                    }

                    resent++;
                    log.info("Job '{}' number of emails sent on case {}: {}", jobName, caseId, resentEmailsCount);
                    totalResentEmails += resentEmailsCount;

                } catch (Exception e) {
                    log.error("Job '{}' could not resend email on case {} due to {}", jobName, caseId,
                        e.getMessage(), e);
                    failed++;
                    Thread.sleep(3000); // give ccd time to recover in case it was getting too many request
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

        log.info("Job '{}' finished, total resent emails {}. {} successful cases, {} failed cases", jobName,
            totalResentEmails, resent, failed);
    }

    // and approved orders
    private int resendGeneratedOrders(CaseData caseData, List<LocalDate> datesToResend) {
        if (isEmpty(caseData.getOrderCollection())) {
            return 0;
        }
        List<Element<GeneratedOrder>> ordersToSend = caseData.getOrderCollection().stream()
            .filter(el -> checkOrderDate(el, datesToResend))
            .collect(Collectors.toList());

        int resentEmails = 0;
        for (Element<GeneratedOrder> order : ordersToSend) {
            GeneratedOrderEvent event = new GeneratedOrderEvent(
                caseData,
                order.getValue().getDocument(),
                LanguageTranslationRequirement.NO,
                order.getValue().getTitle(),
                getOrderDate(order),
                order.getValue().getOrderType()
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
            resentEmails++;
        }
        return resentEmails;
    }

    private int resendDraftOrder(CaseData caseData, List<LocalDate> datesToResend) {
        if (isEmpty(caseData.getHearingOrdersBundlesDrafts())) {
            return 0;
        }
        List<Element<HearingOrdersBundle>> draftOrders = caseData.getHearingOrdersBundlesDrafts();

        int resentEmails = 0;
        for (Element<HearingOrdersBundle> bundle : draftOrders) {

            Set<Element<HearingOrder>> orders = bundle.getValue().getOrders().stream()
                .filter(el -> datesToResend.contains(el.getValue().getDateSent()))
                .collect(toSet());

            if (!orders.isEmpty()) {
                Set<DocumentReference> docs = orders.stream()
                    .map(el -> el.getValue().getOrder())
                    .collect(toSet());

                LocalDateTime hearingStartDate = findElement(bundle.getValue().getHearingId(),
                    caseData.getAllHearings())
                    .map(el -> el.getValue().getStartDate())
                    .orElse(null);

                if (featureToggleService.isResendCafcassEmailsEnabled()) {
                    cafcassNotificationService.sendEmail(caseData,
                        docs,
                        ORDER,
                        OrderCafcassData.builder()
                            .documentName("draft order")
                            .hearingDate(hearingStartDate)
                            // Default to using the first order in the bundle
                            .orderApprovalDate(bundle.getValue().getOrders().get(0).getValue().getDateSent())
                            .build()
                    );
                } else {
                    log.info("Would have resent draft orders email about {}, number of drafts: {}", caseData.getId(),
                        docs.size());
                }
                resentEmails++;
            }
        }
        return resentEmails;
    }

    private int resendSealedCMOs(CaseData caseData, List<LocalDate> dates) {
        if (isEmpty(caseData.getSealedCMOs())) {
            return 0;
        }

        List<Element<HearingOrder>> sealedCMOsToResend = caseData.getSealedCMOs().stream()
            .filter(el -> dates.contains(el.getValue().getDateIssued()))
            .collect(Collectors.toList());

        int resentEmails = 0;
        for (Element<HearingOrder> cmo : sealedCMOsToResend) {

            // find the hearing this CMO is for (if there is one)
            String[] splits = cmo.getValue().getHearing().split(", ");
            LocalDate hearingDate = labelToDate(splits[splits.length - 1]);

            Optional<HearingBooking> hearing = caseData.getAllHearings().stream()
                .filter(el -> el.getValue().getStartDate().toLocalDate().equals(hearingDate))
                .findFirst()
                .map(Element::getValue);

            if (featureToggleService.isResendCafcassEmailsEnabled()) {
                cafcassNotificationService.sendEmail(caseData,
                    of(cmo.getValue().getOrder()),
                    ORDER,
                    OrderCafcassData.builder()
                        .documentName(cmo.getValue().getTitle())
                        .hearingDate(hearing.map(HearingBooking::getStartDate).orElse(null))
                        .orderApprovalDate(cmo.getValue().getDateIssued())
                        .build()
                );
            } else {
                log.info("Would have resent sealed CMO email about {}, {}", caseData.getId(),
                    cmo.getValue().getTitle());
            }
            resentEmails++;
        }
        return resentEmails;
    }

    private int resendNoticeOfHearing(CaseData caseData, List<LocalDateTime> dateTimes) {
        if (isEmpty(caseData.getHearingDetails())) {
            return 0;
        }

        List<Element<HearingBooking>> hearings = caseData.getHearingDetails().stream()
            .filter(el -> dateTimes.contains(el.getValue().getStartDate()))
            .collect(Collectors.toList());

        int resentEmails = 0;
        for (Element<HearingBooking> booking : hearings) {

            if (!isEmpty(booking.getValue().getNoticeOfHearing())) {
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
                resentEmails++;
            } else {
                log.info("Skipping notice of hearing email for {}, {} as notice document no longer present",
                    caseData.getId(), booking.getValue().getStartDate().format(DATE_FORMATTER));
            }
        }
        return resentEmails;
    }

    private LocalDate labelToDate(String label) {
        if (label != null) {
            return LocalDate.parse(label, DateTimeFormatter.ofPattern(DATE, Locale.UK));
        }
        return null;
    }

    private boolean checkOrderDate(Element<GeneratedOrder> order, List<LocalDate> dates) {
        if (!isEmpty(order.getValue().getApprovalDate())) {
            return dates.contains(order.getValue().getApprovalDate());
        } else if (!isEmpty(order.getValue().getApprovalDateTime())) {
            return dates.contains(order.getValue().getApprovalDateTime().toLocalDate());
        } else if (!isEmpty(order.getValue().getDateOfIssue())) {
            return dates.contains(labelToDate(order.getValue().getDateOfIssue()));
        }
        return false;
    }

    private LocalDate getOrderDate(Element<GeneratedOrder> order) {
        if (!isEmpty(order.getValue().getApprovalDate())) {
            return order.getValue().getApprovalDate();
        } else if (!isEmpty(order.getValue().getApprovalDateTime())) {
            return order.getValue().getApprovalDateTime().toLocalDate();
        } else if (!isEmpty(order.getValue().getDateOfIssue())) {
            return labelToDate(order.getValue().getDateOfIssue());
        }
        return LocalDate.now();
    }

}


