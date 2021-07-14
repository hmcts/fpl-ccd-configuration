package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.CtscEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences;
import uk.gov.hmcts.reform.fpl.events.SendNoticeOfHearing;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Recipient;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.notify.LocalAuthorityInboxRecipientsRequest;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.SendDocumentService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.NoticeOfHearingEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.NoticeOfHearingNoOtherAddressEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.representative.RepresentativeNotificationService;

import java.util.Collection;
import java.util.List;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.NOTICE_OF_NEW_HEARING;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.NOTICE_OF_NEW_HEARING_CHILD_NAME;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.NOTICE_OF_NEW_HEARING_NO_OTHER_ADDRESS;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SendNoticeOfHearingHandler {

    private static final List<RepresentativeServingPreferences> SERVING_PREFERENCES = List.of(EMAIL, DIGITAL_SERVICE);

    private final NoticeOfHearingEmailContentProvider noticeOfHearingEmailContentProvider;
    private final NoticeOfHearingNoOtherAddressEmailContentProvider noticeOfHearingNoOtherAddressEmailContentProvider;
    private final NotificationService notificationService;
    private final RepresentativeNotificationService representativeNotificationService;
    private final InboxLookupService inboxLookupService;
    private final CafcassLookupConfiguration cafcassLookupConfiguration;
    private final CtscEmailLookupConfiguration ctscEmailLookupConfiguration;
    private final SendDocumentService sendDocumentService;
    private final FeatureToggleService toggleService;

    @Async
    @EventListener
    public void notifyLocalAuthority(final SendNoticeOfHearing event) {
        final CaseData caseData = event.getCaseData();
        Collection<String> emails = inboxLookupService.getRecipients(
            LocalAuthorityInboxRecipientsRequest.builder().caseData(caseData).build());

        NotifyData notifyData = noticeOfHearingEmailContentProvider.buildNewNoticeOfHearingNotification(
            caseData, event.getSelectedHearing(), DIGITAL_SERVICE
        );

        notificationService.sendEmail(getTemplate(), emails, notifyData, caseData.getId().toString());
    }

    @Async
    @EventListener
    public void notifyCafcass(final SendNoticeOfHearing event) {
        final CaseData caseData = event.getCaseData();

        final String recipient = cafcassLookupConfiguration.getCafcass(caseData.getCaseLocalAuthority()).getEmail();

        NotifyData notifyData = noticeOfHearingEmailContentProvider.buildNewNoticeOfHearingNotification(
            caseData, event.getSelectedHearing(), EMAIL
        );

        notificationService.sendEmail(getTemplate(), recipient, notifyData, caseData.getId());
    }

    @Async
    @EventListener
    public void notifyRepresentatives(final SendNoticeOfHearing event) {
        final CaseData caseData = event.getCaseData();

        String template = getTemplate();

        SERVING_PREFERENCES.forEach(servingPreference -> {
            NotifyData notifyData = noticeOfHearingEmailContentProvider.buildNewNoticeOfHearingNotification(
                caseData, event.getSelectedHearing(), servingPreference
            );

            representativeNotificationService.sendToRepresentativesByServedPreference(
                servingPreference, template, notifyData, caseData
            );
        });
    }

    @Async
    @EventListener
    public void sendNoticeOfHearingByPost(final SendNoticeOfHearing event) {
        final CaseData caseData = event.getCaseData();
        final DocumentReference noticeOfHearing = event.getSelectedHearing().getNoticeOfHearing();

        final List<Recipient> recipients = sendDocumentService.getStandardRecipients(caseData);

        sendDocumentService.sendDocuments(caseData, List.of(noticeOfHearing), recipients);
    }

    @Async
    @EventListener
    public void notifyCtsc(final SendNoticeOfHearing event) {
        final CaseData caseData = event.getCaseData();

        String recipient = ctscEmailLookupConfiguration.getEmail();

        List<Other> others = unwrapElements(caseData.getAllOthers());

        others.forEach(other -> {
            if (other.getRepresentedBy().isEmpty() && !other.hasAddressAdded()) {
                NotifyData notifyData =
                    noticeOfHearingNoOtherAddressEmailContentProvider.buildNewNoticeOfHearingNoOtherAddressNotification(
                        caseData, event.getSelectedHearing(), other);

                notificationService.sendEmail(NOTICE_OF_NEW_HEARING_NO_OTHER_ADDRESS,
                    recipient,
                    notifyData,
                    caseData.getId());
            }
        });
    }

    private String getTemplate() {
        return toggleService.isEldestChildLastNameEnabled() ? NOTICE_OF_NEW_HEARING_CHILD_NAME : NOTICE_OF_NEW_HEARING;
    }
}
