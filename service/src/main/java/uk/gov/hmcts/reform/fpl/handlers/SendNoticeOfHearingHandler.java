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
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Recipient;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.RecipientsRequest;
import uk.gov.hmcts.reform.fpl.service.LocalAuthorityRecipientsService;
import uk.gov.hmcts.reform.fpl.service.SendDocumentService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.NoticeOfHearingEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.NoticeOfHearingNoOtherAddressEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.others.OtherRecipientsInbox;
import uk.gov.hmcts.reform.fpl.service.representative.RepresentativeNotificationService;
import uk.gov.hmcts.reform.fpl.service.translations.TranslationRequestService;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.NOTICE_OF_NEW_HEARING;
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
    private final LocalAuthorityRecipientsService localAuthorityRecipients;
    private final OtherRecipientsInbox otherRecipientsInbox;
    private final CafcassLookupConfiguration cafcassLookupConfiguration;
    private final CtscEmailLookupConfiguration ctscEmailLookupConfiguration;
    private final SendDocumentService sendDocumentService;
    private final TranslationRequestService translationRequestService;

    @Async
    @EventListener
    public void notifyLocalAuthority(final SendNoticeOfHearing event) {
        final CaseData caseData = event.getCaseData();

        final RecipientsRequest recipientsRequest = RecipientsRequest.builder().caseData(caseData).build();

        final Collection<String> recipients = localAuthorityRecipients.getRecipients(recipientsRequest);

        NotifyData notifyData = noticeOfHearingEmailContentProvider.buildNewNoticeOfHearingNotification(
            caseData, event.getSelectedHearing(), DIGITAL_SERVICE);

        notificationService.sendEmail(NOTICE_OF_NEW_HEARING, recipients, notifyData, caseData.getId());
    }

    @Async
    @EventListener
    public void notifyCafcass(final SendNoticeOfHearing event) {
        final CaseData caseData = event.getCaseData();

        final String recipient = cafcassLookupConfiguration.getCafcass(caseData.getCaseLocalAuthority()).getEmail();

        NotifyData notifyData = noticeOfHearingEmailContentProvider.buildNewNoticeOfHearingNotification(
            caseData, event.getSelectedHearing(), EMAIL
        );

        notificationService.sendEmail(NOTICE_OF_NEW_HEARING, recipient, notifyData, caseData.getId());
    }

    @Async
    @EventListener
    public void notifyRepresentatives(final SendNoticeOfHearing event) {
        final CaseData caseData = event.getCaseData();
        final HearingBooking hearingBooking = event.getSelectedHearing();

        SERVING_PREFERENCES.forEach(servingPreference -> {
            NotifyData notifyData = noticeOfHearingEmailContentProvider.buildNewNoticeOfHearingNotification(
                caseData, event.getSelectedHearing(), servingPreference
            );

            List<Element<Other>> othersSelected = hearingBooking.getOthers();

            representativeNotificationService.sendToRepresentativesByServedPreference(
                servingPreference, NOTICE_OF_NEW_HEARING, notifyData, caseData, othersSelected
            );
        });
    }

    @Async
    @EventListener
    public void sendNoticeOfHearingByPost(final SendNoticeOfHearing event) {
        final CaseData caseData = event.getCaseData();
        final DocumentReference noticeOfHearing = event.getSelectedHearing().getNoticeOfHearing();
        final List<Element<Other>> others = event.getSelectedHearing().getOthers();

        final List<Recipient> recipients = sendDocumentService.getStandardRecipients(caseData);
        recipients.addAll(otherRecipientsInbox.getSelectedRecipientsWithNoRepresentation(others));

        sendDocumentService.sendDocuments(caseData, List.of(noticeOfHearing), recipients);
    }

    @Async
    @EventListener
    public void notifyCtsc(final SendNoticeOfHearing event) {
        final CaseData caseData = event.getCaseData();
        final HearingBooking hearingBooking = event.getSelectedHearing();

        String recipient = ctscEmailLookupConfiguration.getEmail();

        List<Other> others = unwrapElements(hearingBooking.getOthers());

        others.forEach(other -> {
            if (!other.isRepresented() && !other.hasAddressAdded() && isNotEmpty(other.getName())) {
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

    @Async
    @EventListener
    public void notifyTranslationTeam(SendNoticeOfHearing event) {
        HearingBooking selectedHearing = event.getSelectedHearing();
        translationRequestService.sendRequest(event.getCaseData(),
            Optional.ofNullable(selectedHearing.getTranslationRequirements()),
            selectedHearing.getNoticeOfHearing(), selectedHearing.asLabel()
        );
    }
}
