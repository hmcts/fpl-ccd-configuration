package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.CtscEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences;
import uk.gov.hmcts.reform.fpl.events.SendNoticeOfHearing;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Recipient;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.cafcass.NoticeOfHearingCafcassData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.notify.RecipientsRequest;
import uk.gov.hmcts.reform.fpl.model.notify.hearing.NoticeOfHearingNoOtherAddressTemplate;
import uk.gov.hmcts.reform.fpl.model.notify.hearing.NoticeOfHearingTemplate;
import uk.gov.hmcts.reform.fpl.service.LocalAuthorityRecipientsService;
import uk.gov.hmcts.reform.fpl.service.SendDocumentService;
import uk.gov.hmcts.reform.fpl.service.cafcass.CafcassNotificationService;
import uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.NoticeOfHearingEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.NoticeOfHearingNoOtherAddressEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.others.OtherRecipientsInbox;
import uk.gov.hmcts.reform.fpl.service.representative.RepresentativeNotificationService;
import uk.gov.hmcts.reform.fpl.service.translations.TranslationRequestService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.NOTICE_OF_NEW_HEARING;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.NOTICE_OF_NEW_HEARING_NO_OTHER_ADDRESS;
import static uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration.Cafcass;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.CAFCASS_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.CTSC_INBOX;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_CODE;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
class SendNoticeOfHearingHandlerTest {
    private static final NoticeOfHearingTemplate DIGITAL_REP_NOTIFY_DATA = mock(NoticeOfHearingTemplate.class);
    private static final NoticeOfHearingTemplate EMAIL_REP_NOTIFY_DATA = mock(NoticeOfHearingTemplate.class);
    private static final NoticeOfHearingNoOtherAddressTemplate NO_OTHER_ADDRESS_NOTIFY_DATA = mock(
        NoticeOfHearingNoOtherAddressTemplate.class);
    private static final HearingBooking HEARING = mock(HearingBooking.class);
    private static final Other OTHER = mock(Other.class);
    private static final CaseData CASE_DATA = mock(CaseData.class);
    private static final Long CASE_ID = 12345L;
    private static final LanguageTranslationRequirement TRANSLATION_REQUIREMENTS =
        LanguageTranslationRequirement.ENGLISH_TO_WELSH;
    private static final DocumentReference NOTICE_OF_HEARING = mock(DocumentReference.class);
    private static final LocalDateTime START_DATE = LocalDateTime.of(2012, 3, 1, 12, 3, 4);

    @Mock
    private LocalAuthorityRecipientsService localAuthorityRecipients;
    @Mock
    private NotificationService notificationService;
    @Mock
    private NoticeOfHearingEmailContentProvider contentProvider;
    @Mock
    private NoticeOfHearingNoOtherAddressEmailContentProvider noticeOfHearingNoOtherAddressEmailContentProvider;
    @Mock
    private RepresentativeNotificationService representativeNotificationService;
    @Mock
    private OtherRecipientsInbox otherRecipientsInbox;
    @Mock
    private SendDocumentService sendDocumentService;
    @Mock
    private CafcassLookupConfiguration cafcassLookup;
    @Mock
    private CtscEmailLookupConfiguration ctscEmailLookupConfiguration;
    @Mock
    private TranslationRequestService translationRequestService;
    @Mock
    private CafcassNotificationService cafcassNotificationService;
    @Captor
    private ArgumentCaptor<Set<DocumentReference>> setOfDocCap;

    @InjectMocks
    private SendNoticeOfHearingHandler underTest;

    @Test
    void shouldSendNotificationToLAWhenNewHearingIsAdded() {
        given(CASE_DATA.getId()).willReturn(CASE_ID);
        given(localAuthorityRecipients.getRecipients(RecipientsRequest.builder().caseData(CASE_DATA).build()))
            .willReturn(Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS));
        given(contentProvider.buildNewNoticeOfHearingNotification(CASE_DATA, HEARING, DIGITAL_SERVICE))
            .willReturn(DIGITAL_REP_NOTIFY_DATA);


        underTest.notifyLocalAuthority(new SendNoticeOfHearing(CASE_DATA, HEARING, false));

        verify(notificationService).sendEmail(
            NOTICE_OF_NEW_HEARING, Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS), DIGITAL_REP_NOTIFY_DATA, CASE_ID);
    }

    @Test
    void shouldSendNotificationToCafcassWhenNewHearingIsAdded() {
        given(CASE_DATA.getId()).willReturn(CASE_ID);
        given(CASE_DATA.getCaseLocalAuthority()).willReturn(LOCAL_AUTHORITY_CODE);
        given(CASE_DATA.getCaseLaOrRelatingLa()).willReturn(LOCAL_AUTHORITY_CODE);
        given(cafcassLookup.getCafcass(LOCAL_AUTHORITY_CODE)).willReturn(new Cafcass("", CAFCASS_EMAIL_ADDRESS));
        given(contentProvider.buildNewNoticeOfHearingNotification(CASE_DATA, HEARING, DIGITAL_SERVICE))
            .willReturn(EMAIL_REP_NOTIFY_DATA);
        given(cafcassLookup.getCafcassWelsh(LOCAL_AUTHORITY_CODE))
            .willReturn(Optional.of(
                new CafcassLookupConfiguration.Cafcass("Cafcass Cymru", CAFCASS_EMAIL_ADDRESS))
            );

        underTest.notifyCafcass(new SendNoticeOfHearing(CASE_DATA, HEARING, false));

        verify(notificationService).sendEmail(
            NOTICE_OF_NEW_HEARING, CAFCASS_EMAIL_ADDRESS, EMAIL_REP_NOTIFY_DATA, CASE_ID
        );
    }

    @Test
    void shouldSendNotificationToCafcassWhenNewHearingIsAddedEnglandLA() {
        DocumentReference documentReference = DocumentReference.builder().build();
        NoticeOfHearingCafcassData noticeOfHearingCafcassData = NoticeOfHearingCafcassData.builder().build();
        given(CASE_DATA.getId()).willReturn(CASE_ID);
        given(CASE_DATA.getCaseLaOrRelatingLa()).willReturn(LOCAL_AUTHORITY_CODE);
        given(contentProvider.buildNewNoticeOfHearingNotificationCafcassData(CASE_DATA, HEARING))
                .willReturn(noticeOfHearingCafcassData);
        given(cafcassLookup.getCafcassEngland(LOCAL_AUTHORITY_CODE))
                .willReturn(Optional.of(
                    new CafcassLookupConfiguration.Cafcass("Cafcass England", CAFCASS_EMAIL_ADDRESS))
            );
        given(HEARING.getNoticeOfHearing()).willReturn(documentReference);

        underTest.notifyCafcassSendGrid(new SendNoticeOfHearing(CASE_DATA, HEARING, false));

        verify(cafcassNotificationService).sendEmail(
                eq(CASE_DATA),
                setOfDocCap.capture(),
                same(CafcassRequestEmailContentProvider.NOTICE_OF_HEARING),
                eq(noticeOfHearingCafcassData)
        );
        assertThat(setOfDocCap.getValue()).contains(documentReference);
    }

    @Test
    void shouldSendNotificationToCtscWhenOtherDoesNotHaveRepresentationAndAddressWhenNewHearingIsAdded() {
        given(CASE_DATA.getId()).willReturn(CASE_ID);
        given(ctscEmailLookupConfiguration.getEmail()).willReturn(CTSC_INBOX);
        given(HEARING.getOthers()).willReturn(wrapElements(OTHER));
        given(OTHER.isRepresented()).willReturn(false);
        given(OTHER.hasAddressAdded()).willReturn(false);
        given(OTHER.getName()).willReturn("John");
        given(noticeOfHearingNoOtherAddressEmailContentProvider.buildNewNoticeOfHearingNoOtherAddressNotification(
            CASE_DATA, HEARING, OTHER)).willReturn(NO_OTHER_ADDRESS_NOTIFY_DATA);

        underTest.notifyCtsc(new SendNoticeOfHearing(CASE_DATA, HEARING, false));

        verify(notificationService).sendEmail(
            NOTICE_OF_NEW_HEARING_NO_OTHER_ADDRESS, CTSC_INBOX, NO_OTHER_ADDRESS_NOTIFY_DATA, CASE_ID);

    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldNotSendNotificationToCtscWhenOtherDoesNotHaveAddressAndNameIsNullOrEmpty(String otherName) {
        given(CASE_DATA.getId()).willReturn(CASE_ID);
        given(ctscEmailLookupConfiguration.getEmail()).willReturn(CTSC_INBOX);
        given(HEARING.getOthers()).willReturn(wrapElements(OTHER));
        given(OTHER.isRepresented()).willReturn(false);
        given(OTHER.hasAddressAdded()).willReturn(false);
        given(OTHER.getName()).willReturn(otherName);

        underTest.notifyCtsc(new SendNoticeOfHearing(CASE_DATA, HEARING, false));

        verifyNoInteractions(notificationService);
    }

    @ParameterizedTest
    @MethodSource("shouldNotSendOtherSource")
    void shouldNotSendNotificationToCtscWhenOtherHasRepresentationOrAddressWhenNewHearingIsAdded(
        boolean isRepresented, boolean hasAddressAdded) {

        given(CASE_DATA.getId()).willReturn(CASE_ID);
        given(ctscEmailLookupConfiguration.getEmail()).willReturn(CTSC_INBOX);
        given(HEARING.getOthers()).willReturn(wrapElements(OTHER));
        given(OTHER.isRepresented()).willReturn(isRepresented);
        given(OTHER.hasAddressAdded()).willReturn(hasAddressAdded);

        underTest.notifyCtsc(new SendNoticeOfHearing(CASE_DATA, HEARING, false));

        verify(notificationService, never()).sendEmail(
            NOTICE_OF_NEW_HEARING_NO_OTHER_ADDRESS, CTSC_INBOX, NO_OTHER_ADDRESS_NOTIFY_DATA, CASE_ID);

    }

    @Test
    void shouldSendNotificationToRepresentativesWhenNewHearingIsAdded() {
        given(contentProvider.buildNewNoticeOfHearingNotification(CASE_DATA, HEARING, DIGITAL_SERVICE))
            .willReturn(DIGITAL_REP_NOTIFY_DATA);
        given(contentProvider.buildNewNoticeOfHearingNotification(CASE_DATA, HEARING, EMAIL))
            .willReturn(EMAIL_REP_NOTIFY_DATA);

        underTest.notifyRepresentatives(new SendNoticeOfHearing(CASE_DATA, HEARING, false));

        verify(representativeNotificationService).sendToRepresentativesExceptOthersByServedPreference(
            RepresentativeServingPreferences.EMAIL,
            NOTICE_OF_NEW_HEARING,
            EMAIL_REP_NOTIFY_DATA,
            CASE_DATA
        );

        verify(representativeNotificationService).sendToRepresentativesExceptOthersByServedPreference(
            RepresentativeServingPreferences.DIGITAL_SERVICE,
            NOTICE_OF_NEW_HEARING,
            DIGITAL_REP_NOTIFY_DATA,
            CASE_DATA
        );
    }

    @Test
    void shouldSendNoticeOfHearingToRepresentativesAndNotRepresentedRespondentsByPost() {
        Representative representative = mock(Representative.class);
        RespondentParty respondent = mock(RespondentParty.class);
        List<Recipient> recipients = new ArrayList<>(List.of(representative, respondent));
        DocumentReference noticeOfHearing = mock(DocumentReference.class);

        given(sendDocumentService.getStandardRecipients(CASE_DATA)).willReturn(recipients);
        given(HEARING.getNoticeOfHearing()).willReturn(noticeOfHearing);

        underTest.sendNoticeOfHearingByPost(new SendNoticeOfHearing(CASE_DATA, HEARING, false));

        verify(sendDocumentService).sendDocuments(CASE_DATA, List.of(noticeOfHearing), recipients);
        verify(sendDocumentService).getStandardRecipients(CASE_DATA);

        verifyNoMoreInteractions(sendDocumentService);
        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldNotSendNoticeOfHearingToUnrepresentedOthersByPost() {
        DocumentReference noticeOfHearing = mock(DocumentReference.class);
        given(sendDocumentService.getStandardRecipients(CASE_DATA)).willReturn(new ArrayList<>());
        given(HEARING.getNoticeOfHearing()).willReturn(noticeOfHearing);

        underTest.sendNoticeOfHearingByPost(new SendNoticeOfHearing(CASE_DATA, HEARING, false));

        verify(sendDocumentService).sendDocuments(CASE_DATA, List.of(noticeOfHearing), List.of());

        verifyNoMoreInteractions(sendDocumentService);
        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldNotSendNoticeOfHearingByPostIfTranslationNeeded() {
        underTest.sendNoticeOfHearingByPost(new SendNoticeOfHearing(CASE_DATA, HearingBooking.builder()
            .translationRequirements(LanguageTranslationRequirement.WELSH_TO_ENGLISH)
            .build(), false));

        verifyNoInteractions(sendDocumentService, otherRecipientsInbox, notificationService);
    }


    @Test
    void shouldNotifyTranslationTeam() {
        underTest.notifyTranslationTeam(
            new SendNoticeOfHearing(CASE_DATA, HearingBooking.builder()
                .noticeOfHearing(NOTICE_OF_HEARING)
                .startDate(START_DATE)
                .translationRequirements(TRANSLATION_REQUIREMENTS)
                .build(), false)
        );

        verify(translationRequestService).sendRequest(CASE_DATA,
            Optional.of(TRANSLATION_REQUIREMENTS),
            NOTICE_OF_HEARING, "Notice of hearing - 1 March 2012");
    }

    @Test
    void shouldNotifyTranslationTeamIfLanguageRequirementDefaultsToEmpty() {
        underTest.notifyTranslationTeam(
            new SendNoticeOfHearing(CASE_DATA, HearingBooking.builder()
                .startDate(START_DATE)
                .noticeOfHearing(NOTICE_OF_HEARING)
                .build(), false)
        );

        verify(translationRequestService).sendRequest(CASE_DATA,
            Optional.of(LanguageTranslationRequirement.NO),
            NOTICE_OF_HEARING, "Notice of hearing - 1 March 2012");
    }

    @Test
    void shouldNotSendNotificationToRepresentativesWhenNewGatekeepingHearingIsAdded() {
        underTest.notifyRepresentatives(new SendNoticeOfHearing(CASE_DATA, HEARING, true));

        verifyNoInteractions(representativeNotificationService);
    }

    @Test
    void shouldNotSendNoticeOfHearingToRepresentativesAndNotRepresentedRespondentsByPost() {
        underTest.sendNoticeOfHearingByPost(new SendNoticeOfHearing(CASE_DATA, HEARING, true));

        verifyNoInteractions(sendDocumentService);
        verifyNoInteractions(notificationService);
    }

    private static Stream<Arguments> shouldNotSendOtherSource() {
        return Stream.of(
            Arguments.of(true, true),
            Arguments.of(true, false),
            Arguments.of(false, true)
        );
    }
}
