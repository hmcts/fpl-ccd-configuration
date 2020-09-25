package uk.gov.hmcts.reform.fpl.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences;
import uk.gov.hmcts.reform.fpl.events.NewHearingsAdded;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.hearing.NoticeOfHearingTemplate;
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
public class NewHearingsAddedHandler {

    private static final List<RepresentativeServingPreferences> SERVING_PREFERENCES = List.of(EMAIL, DIGITAL_SERVICE);

    private final NoticeOfHearingEmailContentProvider newHearingContent;
    private final ObjectMapper mapper;
    private final NotificationService notificationService;
    private final RepresentativeNotificationService representativeNotificationService;
    private final InboxLookupService inboxLookupService;
    private final CafcassLookupConfiguration cafcassLookupConfiguration;
    private final CoreCaseDataService coreCaseDataService;

    @Async
    @EventListener
    public void sendEmailToLA(final NewHearingsAdded event) {
        final CaseData caseData = event.getCaseData();
        Collection<String> emails = inboxLookupService.getRecipients(caseData);

        event.getNewHearings().forEach(hearing -> {
            NoticeOfHearingTemplate templateData = newHearingContent.buildNewNoticeOfHearingNotification(caseData,
                hearing.getValue(), DIGITAL_SERVICE);

            notificationService.sendEmail(NOTICE_OF_NEW_HEARING, emails, templateData, caseData.getId().toString());
        });
    }

    @Async
    @EventListener
    public void sendEmailToCafcass(final NewHearingsAdded event) {
        final CaseData caseData = event.getCaseData();
        String email = cafcassLookupConfiguration.getCafcass(caseData.getCaseLocalAuthority()).getEmail();

        event.getNewHearings().forEach(hearing -> {
            NoticeOfHearingTemplate templateData = newHearingContent.buildNewNoticeOfHearingNotification(caseData,
                hearing.getValue(), EMAIL);

            notificationService.sendEmail(NOTICE_OF_NEW_HEARING, email, templateData, caseData.getId().toString());
        });
    }

    @Async
    @EventListener
    public void sendEmailToRepresentatives(final NewHearingsAdded event) {
        final CaseData caseData = event.getCaseData();

        event.getNewHearings().forEach(hearing -> SERVING_PREFERENCES.forEach(
            servingPreference -> {
                NoticeOfHearingTemplate templateParameters =
                    newHearingContent.buildNewNoticeOfHearingNotification(
                        caseData, hearing.getValue(), servingPreference);

                representativeNotificationService
                    .sendToRepresentativesByServedPreference(servingPreference, NOTICE_OF_NEW_HEARING,
                        templateParameters.toMap(mapper), caseData);
            }
        ));
    }

    @Async
    @EventListener
    public void sendDocumentToRepresentatives(final NewHearingsAdded event) {
        event.getNewHearings().forEach(hearing ->
            coreCaseDataService.triggerEvent(
                JURISDICTION,
                CASE_TYPE,
                event.getCaseData().getId(),
                "internal-change-SEND_DOCUMENT",
                Map.of("documentToBeSent", hearing.getValue().getNoticeOfHearing())));
    }
}
