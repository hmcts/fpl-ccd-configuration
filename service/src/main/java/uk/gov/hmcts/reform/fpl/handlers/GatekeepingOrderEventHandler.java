package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.CtscEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.events.GatekeepingOrderEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.RecipientsRequest;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.LocalAuthorityRecipientsService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.SDOIssuedCafcassContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.SDOIssuedContentProvider;
import uk.gov.hmcts.reform.fpl.service.translations.TranslationRequestService;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Slf4j
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
    private final FeatureToggleService featureToggleService;

    @Async
    @EventListener
    public void notifyCafcass(GatekeepingOrderEvent event) {
        CaseData caseData = event.getCaseData();

        NotifyData parameters = cafcassContentProvider.getNotifyData(
            caseData,
            event.getOrder(),
            event.getDirectionsOrderType());

        String recipient = cafcassLookupConfiguration.getCafcass(caseData.getCaseLaOrRelatingLa()).getEmail();

        notificationService
            .sendEmail(event.getNotificationGroup().getCafcassTemplate(), recipient, parameters, caseData.getId());
    }

    @Async
    @EventListener
    public void notifyLocalAuthority(GatekeepingOrderEvent event) {
        CaseData caseData = event.getCaseData();

        NotifyData notifyData = standardContentProvider.buildNotificationParameters(
            caseData,
            event.getDirectionsOrderType());

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
        if (featureToggleService.isWATaskEmailsEnabled()) {
            CaseData caseData = event.getCaseData();

            NotifyData notifyData = standardContentProvider.buildNotificationParameters(
                caseData,
                event.getDirectionsOrderType());
            String recipient = ctscEmailLookupConfiguration.getEmail();

            notificationService
                .sendEmail(event.getNotificationGroup().getCtscTemplate(), recipient, notifyData, caseData.getId());
        } else {
            log.info("WA EMAIL SKIPPED - gatekeeping completed - {}", event.getCaseData().getId());
        }
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
