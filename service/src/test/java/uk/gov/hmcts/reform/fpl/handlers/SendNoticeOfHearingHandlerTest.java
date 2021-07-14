package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences;
import uk.gov.hmcts.reform.fpl.events.SendNoticeOfHearing;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Recipient;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.notify.LocalAuthorityInboxRecipientsRequest;
import uk.gov.hmcts.reform.fpl.model.notify.hearing.NoticeOfHearingTemplate;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.SendDocumentService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.NoticeOfHearingEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.representative.RepresentativeNotificationService;

import java.util.List;
import java.util.Set;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.NOTICE_OF_NEW_HEARING_CHILD_NAME;
import static uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration.Cafcass;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.CAFCASS_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_CODE;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_EMAIL_ADDRESS;

@ExtendWith(MockitoExtension.class)
class SendNoticeOfHearingHandlerTest {
    private static final NoticeOfHearingTemplate DIGITAL_REP_NOTIFY_DATA = mock(NoticeOfHearingTemplate.class);
    private static final NoticeOfHearingTemplate EMAIL_REP_NOTIFY_DATA = mock(NoticeOfHearingTemplate.class);
    private static final HearingBooking HEARING = mock(HearingBooking.class);
    private static final CaseData CASE_DATA = mock(CaseData.class);
    private static final Long CASE_ID = 12345L;

    @Mock
    private InboxLookupService inboxLookup;
    @Mock
    private NotificationService notificationService;
    @Mock
    private NoticeOfHearingEmailContentProvider contentProvider;
    @Mock
    private RepresentativeNotificationService representativeNotificationService;
    @Mock
    private SendDocumentService sendDocumentService;
    @Mock
    private CafcassLookupConfiguration cafcassLookup;

    @InjectMocks
    private SendNoticeOfHearingHandler underTest;

    @Test
    void shouldSendNotificationToLAWhenNewHearingIsAdded() {
        given(CASE_DATA.getId()).willReturn(CASE_ID);
        given(inboxLookup.getRecipients(LocalAuthorityInboxRecipientsRequest.builder().caseData(CASE_DATA).build()))
            .willReturn(Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS));
        given(contentProvider.buildNewNoticeOfHearingNotification(CASE_DATA, HEARING, DIGITAL_SERVICE))
            .willReturn(DIGITAL_REP_NOTIFY_DATA);


        underTest.notifyLocalAuthority(new SendNoticeOfHearing(CASE_DATA, HEARING));

        verify(notificationService).sendEmail(
            NOTICE_OF_NEW_HEARING_CHILD_NAME, Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS), DIGITAL_REP_NOTIFY_DATA,
            CASE_ID.toString()
        );
    }

    @Test
    void shouldSendNotificationToCafcassWhenNewHearingIsAdded() {
        given(CASE_DATA.getId()).willReturn(CASE_ID);
        given(CASE_DATA.getCaseLocalAuthority()).willReturn(LOCAL_AUTHORITY_CODE);
        given(cafcassLookup.getCafcass(LOCAL_AUTHORITY_CODE)).willReturn(new Cafcass("", CAFCASS_EMAIL_ADDRESS));
        given(contentProvider.buildNewNoticeOfHearingNotification(CASE_DATA, HEARING, EMAIL))
            .willReturn(EMAIL_REP_NOTIFY_DATA);

        underTest.notifyCafcass(new SendNoticeOfHearing(CASE_DATA, HEARING));

        verify(notificationService).sendEmail(
            NOTICE_OF_NEW_HEARING_CHILD_NAME, CAFCASS_EMAIL_ADDRESS, EMAIL_REP_NOTIFY_DATA, CASE_ID
        );
    }

    @Test
    void shouldSendNotificationToRepresentativesWhenNewHearingIsAdded() {
        given(contentProvider.buildNewNoticeOfHearingNotification(CASE_DATA, HEARING, DIGITAL_SERVICE))
            .willReturn(DIGITAL_REP_NOTIFY_DATA);
        given(contentProvider.buildNewNoticeOfHearingNotification(CASE_DATA, HEARING, EMAIL))
            .willReturn(EMAIL_REP_NOTIFY_DATA);

        underTest.notifyRepresentatives(new SendNoticeOfHearing(CASE_DATA, HEARING));

        verify(representativeNotificationService).sendToRepresentativesByServedPreference(
            RepresentativeServingPreferences.EMAIL,
            NOTICE_OF_NEW_HEARING_CHILD_NAME,
            EMAIL_REP_NOTIFY_DATA,
            CASE_DATA
        );

        verify(representativeNotificationService).sendToRepresentativesByServedPreference(
            RepresentativeServingPreferences.DIGITAL_SERVICE,
            NOTICE_OF_NEW_HEARING_CHILD_NAME,
            DIGITAL_REP_NOTIFY_DATA,
            CASE_DATA
        );
    }

    @Test
    void shouldSendNoticeOfHearingToRepresentativesAndNotRepresentedRespondentsByPost() {
        Representative representative = mock(Representative.class);
        RespondentParty respondent = mock(RespondentParty.class);
        List<Recipient> recipients = List.of(representative, respondent);
        DocumentReference noticeOfHearing = mock(DocumentReference.class);

        given(sendDocumentService.getStandardRecipients(CASE_DATA)).willReturn(recipients);
        given(HEARING.getNoticeOfHearing()).willReturn(noticeOfHearing);

        underTest.sendNoticeOfHearingByPost(new SendNoticeOfHearing(CASE_DATA, HEARING));

        verify(sendDocumentService).sendDocuments(CASE_DATA, List.of(noticeOfHearing), recipients);
        verify(sendDocumentService).getStandardRecipients(CASE_DATA);

        verifyNoMoreInteractions(sendDocumentService);
        verifyNoInteractions(notificationService);
    }
}
