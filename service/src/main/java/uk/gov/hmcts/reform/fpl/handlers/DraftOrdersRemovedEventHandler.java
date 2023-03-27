package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.events.cmo.DraftOrdersRemovedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.AbstractJudge;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.notify.RecipientsRequest;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.DraftOrdersRemovedTemplate;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.service.CourtService;
import uk.gov.hmcts.reform.fpl.service.LocalAuthorityRecipientsService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.cmo.DraftOrdersRemovedContentProvider;
import uk.gov.hmcts.reform.fpl.service.representative.RepresentativeNotificationService;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.DRAFT_ORDER_REMOVED_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.DRAFT_ORDER_REMOVED_TEMPLATE_FOR_JUDGES;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@Slf4j
public class DraftOrdersRemovedEventHandler {

    private final DraftOrdersRemovedContentProvider draftOrdersRemovedContentProvider;

    private final LocalAuthorityRecipientsService localAuthorityRecipients;
    private final CourtService courtService;

    private final NotificationService notificationService;
    private final RepresentativeNotificationService representativeNotificationService;

    @Async
    @EventListener
    public void sendNotification(final DraftOrdersRemovedEvent event) {
        // get the case data before the order was removed
        final CaseData caseDataBefore = event.getCaseDataBefore();
        final Element<HearingOrder> draftOrderRemoved = event.getDraftOrderRemoved();

        Optional<HearingBooking> hearingBooking = getHearingBookingFromDraftOrder(caseDataBefore,
            draftOrderRemoved.getId());

        AbstractJudge judge = getJudge(caseDataBefore, hearingBooking);

        DraftOrdersRemovedTemplate draftOrdersRemovedTemplate = draftOrdersRemovedContentProvider
            .buildContent(caseDataBefore, hearingBooking, judge,
                draftOrderRemoved.getValue(), event.getRemovalReason());

        try {
            sendToJudge(caseDataBefore, judge, draftOrdersRemovedTemplate);
        } catch (Exception e) {
            log.error("Fail to notify judges", e);
        }
        try {
            sendToRepresentatives(caseDataBefore, draftOrdersRemovedTemplate);
        } catch (Exception e) {
            log.error("Fail to notify representatives", e);
        }
        try {
            sendToAdminAndLA(caseDataBefore, draftOrdersRemovedTemplate);
        } catch (Exception e) {
            log.error("Fail to notify admin and LA", e);
        }
    }

    private void sendToJudge(CaseData caseData, AbstractJudge judge,
                             DraftOrdersRemovedTemplate draftOrdersRemovedTemplate) {
        if (judge != null) {
            notificationService.sendEmail(
                DRAFT_ORDER_REMOVED_TEMPLATE_FOR_JUDGES,
                judge.getJudgeEmailAddress(),
                draftOrdersRemovedTemplate,
                caseData.getId());
        }
    }

    private void sendToAdminAndLA(CaseData caseData, DraftOrdersRemovedTemplate draftOrdersRemovedTemplate) {
        final Set<String> recipients = new HashSet<>();

        ofNullable(courtService.getCourtEmailNotCtsc(caseData))
                .ifPresent(email -> recipients.add(email));

        recipients.addAll(localAuthorityRecipients.getRecipients(RecipientsRequest.builder()
            .caseData(caseData).build()));

        notificationService.sendEmail(
            DRAFT_ORDER_REMOVED_TEMPLATE,
            recipients,
            draftOrdersRemovedTemplate,
            caseData.getId());
    }

    private void sendToRepresentatives(CaseData caseData, DraftOrdersRemovedTemplate draftOrdersRemovedTemplate) {
        Stream.of(DIGITAL_SERVICE, EMAIL).forEach(preference ->
            representativeNotificationService.sendToRepresentativesByServedPreference(
                preference,
                DRAFT_ORDER_REMOVED_TEMPLATE,
                draftOrdersRemovedTemplate,
                caseData
            )
        );
    }

    private Optional<HearingBooking> getHearingBookingFromDraftOrder(CaseData caseData, UUID draftOrderId) {
        return caseData.getHearingOrderBundleThatContainsOrder(draftOrderId)
           .flatMap(hearingOrdersBundleElement ->
               caseData.findHearingBookingElement(hearingOrdersBundleElement.getValue().getHearingId())
                   .map(Element::getValue));
    }

    private AbstractJudge getJudge(CaseData caseData, Optional<HearingBooking> hearing) {
        AbstractJudge judge = null;

        if (hearing.isPresent() && hearing.get().getJudgeAndLegalAdvisor() != null
            && isNotEmpty(hearing.get().getJudgeAndLegalAdvisor().getJudgeEmailAddress())) {
            judge = hearing.get().getJudgeAndLegalAdvisor();
        } else if (caseData.hasAllocatedJudgeEmail()) {
            judge = caseData.getAllocatedJudge();
        }
        return judge;
    }
}
