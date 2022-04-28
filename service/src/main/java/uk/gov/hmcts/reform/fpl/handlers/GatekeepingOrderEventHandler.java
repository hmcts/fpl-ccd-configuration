package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.CtscEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.events.GatekeepingOrderEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.cafcass.UrgentHearingOrderAndNopData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.RecipientsRequest;
import uk.gov.hmcts.reform.fpl.model.notify.sdo.SDONotifyData;
import uk.gov.hmcts.reform.fpl.service.LocalAuthorityRecipientsService;
import uk.gov.hmcts.reform.fpl.service.cafcass.CafcassNotificationService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.SDOIssuedCafcassContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.SDOIssuedContentProvider;
import uk.gov.hmcts.reform.fpl.service.translations.TranslationRequestService;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static uk.gov.hmcts.reform.fpl.enums.notification.GatekeepingOrderNotificationGroup.URGENT_AND_NOP;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassEmailContentProvider.URGENT_HEARING_ORDER_AND_NOP;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class GatekeepingOrderEventHandler {
    private final NotificationService notificationService;
    private final LocalAuthorityRecipientsService localAuthorityRecipients;
    private final CafcassLookupConfiguration cafcassLookupConfiguration;
    private final CtscEmailLookupConfiguration ctscEmailLookupConfiguration;
    private final SDOIssuedCafcassContentProvider cafcassContentProvider;
    private final SDOIssuedContentProvider standardContentProvider;
    private final TranslationRequestService translationRequestService;
    private final CafcassNotificationService cafcassNotificationService;

    @Async
    @EventListener
    public void notifyCafcass(GatekeepingOrderEvent event) {
        CaseData caseData = event.getCaseData();

        SDONotifyData parameters = cafcassContentProvider.getNotifyData(caseData, event.getOrder());
        String recipient = cafcassLookupConfiguration.getCafcass(caseData.getCaseLocalAuthority()).getEmail();

        if (URGENT_AND_NOP == event.getNotificationGroup()) {
            // switch to SendGrid for adding a priority flag to message header
            cafcassNotificationService.sendEmail(
                caseData,
                Set.of(event.getOrder()),
                URGENT_HEARING_ORDER_AND_NOP,
                UrgentHearingOrderAndNopData.builder()
                    .callout(parameters.getCallout())
                    .leadRespondentsName(parameters.getLastName())
                    .build()
            );
        } else {
            notificationService
                .sendEmail(event.getNotificationGroup().getCafcassTemplate(), recipient, parameters, caseData.getId());
        }
    }

    @Async
    @EventListener
    public void notifyLocalAuthority(GatekeepingOrderEvent event) {
        CaseData caseData = event.getCaseData();

        NotifyData notifyData = standardContentProvider.buildNotificationParameters(caseData);

        final RecipientsRequest recipientsRequest = RecipientsRequest.builder()
            .caseData(caseData)
            .build();

        final Collection<String> recipients = localAuthorityRecipients.getRecipients(recipientsRequest);

        notificationService
            .sendEmail(event.getNotificationGroup().getLaTemplate(), recipients, notifyData, caseData.getId());
    }

    @Async
    @EventListener
    public void notifyCTSC(GatekeepingOrderEvent event) {
        CaseData caseData = event.getCaseData();

        NotifyData notifyData = standardContentProvider.buildNotificationParameters(caseData);
        String recipient = ctscEmailLookupConfiguration.getEmail();

        notificationService
            .sendEmail(event.getNotificationGroup().getCtscTemplate(), recipient, notifyData, caseData.getId());
    }

    @Async
    @EventListener
    public void notifyTranslationTeam(GatekeepingOrderEvent event) {
        translationRequestService.sendRequest(event.getCaseData(),
            event.getLanguageTranslationRequirement(),
            event.getOrder(), event.getOrderTitle());

        ObjectUtils.<List<Element<DocumentBundle>>>defaultIfNull(event.getCaseData()
            .getNoticeOfProceedingsBundle(), Collections.emptyList())
            .forEach(nop -> translationRequestService.sendRequest(event.getCaseData(),
                event.getLanguageTranslationRequirement(),
                nop.getValue().getDocument(), nop.getValue().asLabel())
            );
    }
}
