package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.CtscEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.events.SendNoticeOfHearing;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Recipient;
import uk.gov.hmcts.reform.fpl.model.cafcass.NoticeOfHearingCafcassData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.RecipientsRequest;
import uk.gov.hmcts.reform.fpl.service.LocalAuthorityRecipientsService;
import uk.gov.hmcts.reform.fpl.service.SendDocumentService;
import uk.gov.hmcts.reform.fpl.service.cafcass.CafcassNotificationService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.NoticeOfHearingEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.NoticeOfHearingNoOtherAddressEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.representative.RepresentativeNotificationService;
import uk.gov.hmcts.reform.fpl.service.translations.TranslationRequestService;
import uk.gov.hmcts.reform.fpl.utils.CafcassHelper;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static java.util.Set.of;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.NOTICE_OF_NEW_HEARING;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.NOTICE_OF_NEW_HEARING_NO_OTHER_ADDRESS;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.NOTICE_OF_HEARING;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SendNoticeOfHearingHandler {

    private static final List<RepresentativeServingPreferences> SERVING_PREFERENCES = List.of(EMAIL, DIGITAL_SERVICE);

    private final NoticeOfHearingEmailContentProvider noticeOfHearingEmailContentProvider;
    private final NoticeOfHearingNoOtherAddressEmailContentProvider noticeOfHearingNoOtherAddressEmailContentProvider;
    private final NotificationService notificationService;
    private final RepresentativeNotificationService representativeNotificationService;
    private final LocalAuthorityRecipientsService localAuthorityRecipients;
    private final CafcassLookupConfiguration cafcassLookupConfiguration;
    private final CtscEmailLookupConfiguration ctscEmailLookupConfiguration;
    private final SendDocumentService sendDocumentService;
    private final TranslationRequestService translationRequestService;
    private final CafcassNotificationService cafcassNotificationService;

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

        if (CafcassHelper.isNotifyingCafcassWelsh(caseData, cafcassLookupConfiguration)) {
            final String recipient = cafcassLookupConfiguration.getCafcass(caseData.getCaseLocalAuthority()).getEmail();

            NotifyData notifyData = noticeOfHearingEmailContentProvider.buildNewNoticeOfHearingNotification(
                caseData, event.getSelectedHearing(), DIGITAL_SERVICE
            );
            notificationService.sendEmail(NOTICE_OF_NEW_HEARING, recipient, notifyData, caseData.getId());
        }
    }

    @Async
    @EventListener
    public void notifyCafcassSendGrid(final SendNoticeOfHearing event) {
        final CaseData caseData = event.getCaseData();
        if (CafcassHelper.isNotifyingCafcassEngland(caseData, cafcassLookupConfiguration)) {
            NoticeOfHearingCafcassData noticeOfHearingCafcassData =
                    noticeOfHearingEmailContentProvider.buildNewNoticeOfHearingNotificationCafcassData(
                        caseData,
                        event.getSelectedHearing()
                    );

            cafcassNotificationService.sendEmail(caseData,
                    of(event.getSelectedHearing().getNoticeOfHearing()),
                    NOTICE_OF_HEARING,
                    noticeOfHearingCafcassData);
        }
    }

    @Async
    @EventListener
    public void notifyRepresentatives(final SendNoticeOfHearing event) {
        if (!event.isGateKeepingHearing()) {
            final CaseData caseData = event.getCaseData();

            SERVING_PREFERENCES.forEach(servingPreference -> {
                NotifyData notifyData = noticeOfHearingEmailContentProvider.buildNewNoticeOfHearingNotification(
                    caseData, event.getSelectedHearing(), servingPreference
                );

                representativeNotificationService.sendToRepresentativesExceptOthersByServedPreference(
                    servingPreference, NOTICE_OF_NEW_HEARING, notifyData, caseData
                );
            });
        }
    }

    @Async
    @EventListener
    public void sendNoticeOfHearingByPost(final SendNoticeOfHearing event) {
        if (!event.isGateKeepingHearing()) {
            if (event.getSelectedHearing().getNeedTranslation() == YesNo.YES) {
                return;
            }

            final CaseData caseData = event.getCaseData();
            final DocumentReference noticeOfHearing = event.getSelectedHearing().getNoticeOfHearing();

            final List<Recipient> recipients = sendDocumentService.getStandardRecipients(caseData);

            sendDocumentService.sendDocuments(caseData, List.of(noticeOfHearing), recipients);
        }
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
