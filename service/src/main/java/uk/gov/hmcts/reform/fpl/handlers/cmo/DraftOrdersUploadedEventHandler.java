package uk.gov.hmcts.reform.fpl.handlers.cmo;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.events.cmo.DraftOrdersUploaded;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.cafcass.OrderCafcassData;
import uk.gov.hmcts.reform.fpl.model.common.AbstractJudge;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.DraftOrdersUploadedTemplate;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;
import uk.gov.hmcts.reform.fpl.service.cafcass.CafcassNotificationService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.cmo.DraftOrdersUploadedContentProvider;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.DRAFT_ORDERS_UPLOADED_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.ORDER;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.findElement;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.nullSafeList;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class DraftOrdersUploadedEventHandler {

    private final NotificationService notificationService;
    private final DraftOrdersUploadedContentProvider draftOrdersContentProvider;
    private final CafcassNotificationService cafcassNotificationService;
    private final CafcassLookupConfiguration cafcassLookupConfiguration;

    @Async
    @EventListener
    public void sendNotificationToJudge(final DraftOrdersUploaded event) {
        final CaseData caseData = event.getCaseData();
        final HearingBooking hearing = getHearingBooking(caseData);
        final List<HearingOrder> orders = getOrders(caseData);

        if (isEmpty(orders)) {
            return;
        }

        AbstractJudge judge = getJudge(caseData, hearing);

        if (judge == null || isEmpty(judge.getJudgeEmailAddress())) {
            return;
        }

        final DraftOrdersUploadedTemplate content = draftOrdersContentProvider.buildContent(
            caseData, hearing, judge, orders
        );

        notificationService.sendEmail(
            DRAFT_ORDERS_UPLOADED_NOTIFICATION_TEMPLATE, judge.getJudgeEmailAddress(), content, caseData.getId()
        );
    }

    @Async
    @EventListener
    public void sendNotificationToCafcass(final DraftOrdersUploaded event) {
        final CaseData caseData = event.getCaseData();

        final Optional<CafcassLookupConfiguration.Cafcass> recipientIsEngland =
                cafcassLookupConfiguration.getCafcassEngland(caseData.getCaseLocalAuthority());

        if (recipientIsEngland.isPresent()) {
            LocalDateTime hearingStartDate = Optional.ofNullable(getHearingBooking(caseData))
                .map(HearingBooking::getStartDate)
                .orElse(null);

            Set<DocumentReference> documentReferences = getOrders(caseData).stream()
                .filter(hearingOrder -> hearingOrder.getDateSent().equals(LocalDate.now()))
                .map(HearingOrder::getDocument)
                .collect(toSet());

            if (!documentReferences.isEmpty()) {
                cafcassNotificationService.sendEmail(caseData,
                    documentReferences,
                    ORDER,
                    OrderCafcassData.builder()
                        .documentName("draft order")
                        .hearingDate(hearingStartDate)
                        .orderApprovalDate(LocalDate.now())
                        .build()

                );
            }
        }
    }

    private HearingBooking getHearingBooking(CaseData caseData) {
        return findElement(caseData.getLastHearingOrderDraftsHearingId(),
            caseData.getHearingDetails())
            .map(Element::getValue)
            .orElse(null);
    }

    private AbstractJudge getJudge(CaseData caseData, HearingBooking hearing) {
        AbstractJudge judge = null;

        if (hearing != null && hearing.getJudgeAndLegalAdvisor() != null
            && isNotEmpty(hearing.getJudgeAndLegalAdvisor().getJudgeEmailAddress())) {
            judge = hearing.getJudgeAndLegalAdvisor();
        } else if (caseData.hasAllocatedJudgeEmail()) {
            judge = caseData.getAllocatedJudge();
        }
        return judge;
    }

    private List<HearingOrder> getOrders(CaseData caseData) {
        return nullSafeList(caseData.getHearingOrdersBundlesDrafts()).stream()
            .map(Element::getValue)
            .filter(b -> Objects.equals(b.getHearingId(), caseData.getLastHearingOrderDraftsHearingId()))
            .findFirst()
            .map(HearingOrdersBundle::getOrders)
            .map(ElementUtils::unwrapElements)
            .orElse(emptyList());
    }
}
