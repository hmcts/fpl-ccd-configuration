package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences;
import uk.gov.hmcts.reform.fpl.events.SendNoticeOfHearing;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.LocalAuthorityInboxRecipientsRequest;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.NoticeOfHearingEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.representative.RepresentativeNotificationService;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.NOTICE_OF_NEW_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SendNoticeOfHearingHandler {

    private static final List<RepresentativeServingPreferences> SERVING_PREFERENCES = List.of(EMAIL, DIGITAL_SERVICE);

    private final NoticeOfHearingEmailContentProvider newHearingContent;
    private final NotificationService notificationService;
    private final RepresentativeNotificationService representativeNotificationService;
    private final InboxLookupService inboxLookupService;
    private final CafcassLookupConfiguration cafcassLookupConfiguration;
    private final CoreCaseDataService coreCaseDataService;

    @Async
    @EventListener
    public void notifyLocalAuthority(final SendNoticeOfHearing event) {
        final CaseData caseData = event.getCaseData();
        Collection<String> emails = inboxLookupService.getRecipients(
            LocalAuthorityInboxRecipientsRequest.builder().caseData(caseData).build());

        NotifyData notifyData = newHearingContent.buildNewNoticeOfHearingNotification(caseData,
            event.getSelectedHearing(), DIGITAL_SERVICE);

        notificationService.sendEmail(NOTICE_OF_NEW_HEARING, emails, notifyData, caseData.getId().toString());
    }

    @Async
    @EventListener
    public void notifyCafcass(final SendNoticeOfHearing event) {
        final CaseData caseData = event.getCaseData();

        final String recipient = cafcassLookupConfiguration.getCafcass(caseData.getCaseLocalAuthority()).getEmail();

        NotifyData notifyData = newHearingContent.buildNewNoticeOfHearingNotification(caseData,
            event.getSelectedHearing(), EMAIL);

        notificationService.sendEmail(NOTICE_OF_NEW_HEARING, recipient, notifyData, caseData.getId());
    }

    @Async
    @EventListener
    public void notifyRepresentatives(final SendNoticeOfHearing event) {
        final CaseData caseData = event.getCaseData();

        SERVING_PREFERENCES.forEach(servingPreference -> {
            NotifyData notifyData =
                newHearingContent.buildNewNoticeOfHearingNotification(
                    caseData, event.getSelectedHearing(), servingPreference);

            representativeNotificationService
                .sendToRepresentativesByServedPreference(
                    servingPreference, NOTICE_OF_NEW_HEARING, notifyData, caseData);
        });
    }

    @Async
    @EventListener
    public void sendDocumentToRepresentatives(final SendNoticeOfHearing event) {
        coreCaseDataService.triggerEvent(
            JURISDICTION,
            CASE_TYPE,
            event.getCaseData().getId(),
            "internal-change-SEND_DOCUMENT",
            Map.of("documentToBeSent", event.getSelectedHearing().getNoticeOfHearing()));
    }
}
