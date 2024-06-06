package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences;
import uk.gov.hmcts.reform.fpl.events.SendNoticeOfHearingVacated;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Recipient;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.notify.RecipientsRequest;
import uk.gov.hmcts.reform.fpl.model.notify.hearing.HearingVacatedTemplate;
import uk.gov.hmcts.reform.fpl.service.LocalAuthorityRecipientsService;
import uk.gov.hmcts.reform.fpl.service.SendDocumentService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.HearingVacatedEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.representative.RepresentativeNotificationService;

import java.util.Collection;
import java.util.List;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.VACATE_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SendNoticeOfHearingVacatedHandler {
    private final NotificationService notificationService;
    private final HearingVacatedEmailContentProvider hearingVacatedEmailContentProvider;
    private static final List<RepresentativeServingPreferences> SERVING_PREFERENCES = List.of(EMAIL, DIGITAL_SERVICE);

    private final LocalAuthorityRecipientsService localAuthorityRecipients;
    private final RepresentativeNotificationService representativeNotificationService;
    private final SendDocumentService sendDocumentService;

    @Async
    @EventListener
    public void notifyLocalAuthority(final SendNoticeOfHearingVacated event) {
        final CaseData caseData = event.getCaseData();
        final RecipientsRequest recipientsRequest = RecipientsRequest.builder().caseData(caseData).build();
        final Collection<String> recipients = localAuthorityRecipients.getRecipients(recipientsRequest);

        notificationService.sendEmail(VACATE_HEARING, recipients,
            hearingVacatedEmailContentProvider.buildHearingVacatedNotification(caseData,
                event.getVacatedHearing(), event.isRelisted()),
            caseData.getId());
    }

    @Async
    @EventListener
    public void notifyRepresentatives(final SendNoticeOfHearingVacated event) {
        final CaseData caseData = event.getCaseData();

        HearingVacatedTemplate notifyData = hearingVacatedEmailContentProvider
            .buildHearingVacatedNotification(caseData, event.getVacatedHearing(), event.isRelisted());

        SERVING_PREFERENCES.forEach(servingPreference -> {
            representativeNotificationService.sendToRepresentativesExceptOthersByServedPreference(
                servingPreference, VACATE_HEARING, notifyData, caseData
            );
        });
    }

    @Async
    @EventListener
    public void sendNoticeOfHearingByPost(final SendNoticeOfHearingVacated event) {
        final CaseData caseData = event.getCaseData();
        final DocumentReference noticeOfHearingVacated = event.getVacatedHearing().getNoticeOfHearingVacated();

        final List<Recipient> recipients = sendDocumentService.getStandardRecipients(caseData);

        sendDocumentService.sendDocuments(caseData, List.of(noticeOfHearingVacated), recipients);
    }
}

