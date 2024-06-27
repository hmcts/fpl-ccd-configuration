package uk.gov.hmcts.reform.fpl.handlers;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.events.SendNoticeOfHearingVacated;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.notify.hearing.HearingVacatedTemplate;
import uk.gov.hmcts.reform.fpl.service.LocalAuthorityRecipientsService;
import uk.gov.hmcts.reform.fpl.service.cafcass.CafcassNotificationService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.HearingVacatedEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.representative.RepresentativeNotificationService;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.VACATE_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;

@ExtendWith(MockitoExtension.class)
public class SendNoticeOfHearingVacatedHandlerTest {

    private static final HearingVacatedTemplate HEARING_VACATED_EMAIL_CONTENT =
        mock(HearingVacatedTemplate.class);
    private static final HearingVacatedTemplate HEARING_VACATED_AND_RELISTED_EMAIL_CONTENT =
        mock(HearingVacatedTemplate.class);
    private static final Set<String> LA_RECIPIENTS = Set.of("la@email.com", "la2@email.com");
    private static final String CAFCASS_RECIPIENT = "cafcass@email.com";
    private static final HearingBooking VACATED_HEARING = mock(HearingBooking.class);
    private static final CafcassLookupConfiguration.Cafcass CAFCASS =
        new CafcassLookupConfiguration.Cafcass("Cafcass", CAFCASS_RECIPIENT);

    @Mock
    private HearingVacatedEmailContentProvider hearingVacatedEmailContentProvider;
    @Mock
    private LocalAuthorityRecipientsService localAuthorityRecipients;
    @Mock
    private NotificationService notificationService;
    @Mock
    private RepresentativeNotificationService representativeNotificationService;
    @Mock
    private CafcassNotificationService cafcassNotificationService;
    @Mock
    private CafcassLookupConfiguration cafcassLookupConfiguration;
    @InjectMocks
    private SendNoticeOfHearingVacatedHandler underTest;

    @Test
    void shouldNotifyLocalAuthorityWhenHearingIsVacatedAndRelisted() {
        CaseData caseData = CaseData.builder()
            .id(1L)
            .build();
        SendNoticeOfHearingVacated sendNoticeOfHearingVacatedEvent = new SendNoticeOfHearingVacated(caseData,
            VACATED_HEARING, true);

        when(hearingVacatedEmailContentProvider.buildHearingVacatedNotification(any(), any(), eq(true)))
            .thenReturn(HEARING_VACATED_AND_RELISTED_EMAIL_CONTENT);
        when(localAuthorityRecipients.getRecipients(any())).thenReturn(LA_RECIPIENTS);

        underTest.notifyLocalAuthority(sendNoticeOfHearingVacatedEvent);
        verify(notificationService).sendEmail(VACATE_HEARING, LA_RECIPIENTS, HEARING_VACATED_AND_RELISTED_EMAIL_CONTENT,
            caseData.getId());
    }

    @Test
    void shouldNotifyLocalAuthorityWhenHearingIsVacatedButNotRelisted() {
        CaseData caseData = CaseData.builder()
            .id(1L)
            .build();
        SendNoticeOfHearingVacated sendNoticeOfHearingVacatedEvent = new SendNoticeOfHearingVacated(caseData,
            VACATED_HEARING, false);

        when(hearingVacatedEmailContentProvider.buildHearingVacatedNotification(any(), any(), eq(false)))
            .thenReturn(HEARING_VACATED_EMAIL_CONTENT);
        when(localAuthorityRecipients.getRecipients(any())).thenReturn(LA_RECIPIENTS);

        underTest.notifyLocalAuthority(sendNoticeOfHearingVacatedEvent);
        verify(notificationService).sendEmail(VACATE_HEARING, LA_RECIPIENTS, HEARING_VACATED_EMAIL_CONTENT,
            caseData.getId());
    }

    @Test
    void shouldNotifyRepresentativesWhenHearingIsVacatedAndRelisted() {
        CaseData caseData = CaseData.builder()
            .id(1L)
            .build();
        SendNoticeOfHearingVacated sendNoticeOfHearingVacatedEvent = new SendNoticeOfHearingVacated(caseData,
            VACATED_HEARING, true);

        when(hearingVacatedEmailContentProvider.buildHearingVacatedNotification(any(), any(), eq(true)))
            .thenReturn(HEARING_VACATED_AND_RELISTED_EMAIL_CONTENT);

        underTest.notifyRepresentatives(sendNoticeOfHearingVacatedEvent);

        verify(representativeNotificationService)
            .sendToRepresentativesExceptOthersByServedPreference(EMAIL, VACATE_HEARING,
                HEARING_VACATED_AND_RELISTED_EMAIL_CONTENT, caseData);

        verify(representativeNotificationService)
            .sendToRepresentativesExceptOthersByServedPreference(DIGITAL_SERVICE, VACATE_HEARING,
                HEARING_VACATED_AND_RELISTED_EMAIL_CONTENT, caseData);
    }

    @Test
    void shouldNotifyRepresentativesWhenHearingIsVacatedButNotRelisted() {
        CaseData caseData = CaseData.builder()
            .id(1L)
            .build();
        SendNoticeOfHearingVacated sendNoticeOfHearingVacatedEvent = new SendNoticeOfHearingVacated(caseData,
            VACATED_HEARING, false);

        when(hearingVacatedEmailContentProvider.buildHearingVacatedNotification(any(), any(), eq(false)))
            .thenReturn(HEARING_VACATED_EMAIL_CONTENT);

        underTest.notifyRepresentatives(sendNoticeOfHearingVacatedEvent);

        verify(representativeNotificationService)
            .sendToRepresentativesExceptOthersByServedPreference(EMAIL, VACATE_HEARING,
                HEARING_VACATED_EMAIL_CONTENT, caseData);

        verify(representativeNotificationService)
            .sendToRepresentativesExceptOthersByServedPreference(DIGITAL_SERVICE, VACATE_HEARING,
                HEARING_VACATED_EMAIL_CONTENT, caseData);
    }
}
